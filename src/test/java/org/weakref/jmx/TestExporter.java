/**
 *  Copyright 2009 Martin Traverso
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.weakref.jmx;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.weakref.jmx.testing.TestingMBeanServer;

import static org.weakref.jmx.Util.getUniqueObjectName;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.NotBoundException;
import java.util.ArrayList;
import java.util.List;

public class TestExporter
{
    private MBeanServer server;

    private List<Pair<ObjectName, ?>> objects;

    static class Pair<L, R>
    {
        final L left;
        final R right;

        Pair(L left, R right)
        {
            this.left = left;
            this.right = right;
        }

        static <L, R> Pair<L, R> of(L left, R right)
        {
            return new Pair<L, R>(left, right);
        }
    }

    @BeforeMethod
    void setup()
            throws IOException, MalformedObjectNameException, NotBoundException
    {
        server = new TestingMBeanServer();

        objects = new ArrayList<Pair<ObjectName, ?>>(2);
        objects.add(Pair.of(getUniqueObjectName(), new SimpleObject()));
        objects.add(Pair.of(getUniqueObjectName(), new CustomAnnotationObject()));
        objects.add(Pair.of(getUniqueObjectName(), new FlattenObject()));
        objects.add(Pair.of(getUniqueObjectName(), new CustomFlattenAnnotationObject()));
        objects.add(Pair.of(getUniqueObjectName(), new NestedObject()));
        objects.add(Pair.of(getUniqueObjectName(), new CustomNestedAnnotationObject()));

        MBeanExporter exporter = new MBeanExporter(server);
        for (Pair<ObjectName, ?> pair : objects) {
            exporter.export(pair.left.getCanonicalName(), pair.right);
        }
    }

//    @AfterTest
//    public void teardown()
//            throws IOException, InstanceNotFoundException, MBeanRegistrationException
//    {
//        for (Pair<ObjectName, ?> pair : objects) {
//            server.unregisterMBean(pair.left);
//        }
//    }

//    @Test
//    public void testMBeanInfo()
//            throws IntrospectionException, InstanceNotFoundException, ReflectionException
//    {
//        for (Pair<ObjectName, ?> pair : objects) {
//            info.get
//        }
//    }

    @Test(dataProvider = "fixtures")
    public void testGetterAttributeInfo(String attribute, boolean isIs, Object[] values, Class<?> clazz)
            throws Exception
    {
        String methodName = "set" + attribute;
        for (Pair<ObjectName, ?> pair : objects) {
            String attributeName = toFeatureName(attribute, pair);
            SimpleObject simpleObject = toSimpleObject(pair);
            Method setter = simpleObject.getClass().getMethod(methodName, clazz);

            MBeanInfo info = server.getMBeanInfo(pair.left);
            MBeanAttributeInfo attributeInfo = getAttributeInfo(info, attributeName);
            Assert.assertNotNull(attributeInfo, "AttributeInfo for " + attributeName);
            Assert.assertEquals(attributeInfo.getName(), attributeName, "Attribute Name for " + attributeName);
            Assert.assertEquals(attributeInfo.getType(), setter.getParameterTypes()[0].getName(), "Attribute type for " + attributeName);
            Assert.assertEquals(attributeInfo.isIs(), isIs, "Attribute isIs for " + attributeName);
            Assert.assertTrue(attributeInfo.isReadable(), "Attribute Readable for " + attributeName);
        }
    }

    @Test(dataProvider = "fixtures")
    public void testSetterAttributeInfo(String attribute, boolean isIs, Object[] values, Class<?> clazz)
            throws Exception
    {
        String methodName = (isIs ? "is" : "get") + attribute;

        for (Pair<ObjectName, ?> pair : objects) {
            String attributeName = toFeatureName(attribute, pair);
            SimpleObject simpleObject = toSimpleObject(pair);
            Method getter = simpleObject.getClass().getMethod(methodName);

            MBeanInfo info = server.getMBeanInfo(pair.left);
            MBeanAttributeInfo attributeInfo = getAttributeInfo(info, attributeName);
            Assert.assertNotNull(attributeInfo, "AttributeInfo for " + attributeName);
            Assert.assertEquals(attributeInfo.getName(), attributeName, "Attribute Name for " + attributeName);
            Assert.assertEquals(attributeInfo.getType(), getter.getReturnType().getName(), "Attribute Type for " + attributeName);
            Assert.assertTrue(attributeInfo.isWritable(), "Attribute Writable for " + attributeName);
        }
    }

    @Test
    public void testNotManagedAttributeInfo()
            throws Exception
    {

        for (Pair<ObjectName, ?> pair : objects) {
            MBeanInfo info = server.getMBeanInfo(pair.left);
            String attributeName = toFeatureName("NotManaged", pair);
            MBeanAttributeInfo attributeInfo = getAttributeInfo(info, attributeName);
            Assert.assertNull(attributeInfo, "AttributeInfo for " + attributeName);
        }
    }

    @Test
    public void testReadOnlyAttributeInfo()
            throws Exception
    {
        for (Pair<ObjectName, ?> pair : objects) {
            MBeanInfo info = server.getMBeanInfo(pair.left);
            String attributeName = toFeatureName("ReadOnly", pair);
            MBeanAttributeInfo attributeInfo = getAttributeInfo(info, attributeName);
            Assert.assertNotNull(attributeInfo, "AttributeInfo for " + attributeName);
            Assert.assertEquals(attributeInfo.getName(), attributeName, "Attribute Name for " + attributeName);
            Assert.assertEquals(attributeInfo.getType(), "int", "Attribute Type for " + attributeName);
            Assert.assertTrue(attributeInfo.isReadable(), "Attribute Readable for " + attributeName);
            Assert.assertFalse(attributeInfo.isWritable(), "Attribute Writable for " + attributeName);
        }
    }

    @Test
    public void testWriteOnlyAttributeInfo()
            throws Exception
    {
        for (Pair<ObjectName, ?> pair : objects) {
            MBeanInfo info = server.getMBeanInfo(pair.left);
            String attributeName = toFeatureName("WriteOnly", pair);
            MBeanAttributeInfo attributeInfo = getAttributeInfo(info, attributeName);
            Assert.assertNotNull(attributeInfo, "AttributeInfo for " + attributeName);
            Assert.assertEquals(attributeInfo.getName(), attributeName, "Attribute Name for " + attributeName);
            Assert.assertEquals(attributeInfo.getType(), "int", "Attribute Type for " + attributeName);
            Assert.assertFalse(attributeInfo.isReadable(), "Attribute Readable for " + attributeName);
            Assert.assertTrue(attributeInfo.isWritable(), "Attribute Writable for " + attributeName);
        }
    }

    private MBeanAttributeInfo getAttributeInfo(MBeanInfo info, String attributeName)
    {
        for (MBeanAttributeInfo attributeInfo : info.getAttributes()) {
            if (attributeInfo.getName().equals(attributeName)) {
                return attributeInfo;
            }
        }
        return null;
    }

    @Test(dataProvider = "fixtures")
    public void testOperationInfo(String attribute, boolean isIs, Object[] values, Class<?> clazz)
            throws Exception
    {
        for (Pair<ObjectName, ?> pair : objects) {
            String operationName = toFeatureName("echo", pair);

            MBeanInfo beanInfo = server.getMBeanInfo(pair.left);
            MBeanOperationInfo operationInfo = null;
            for (MBeanOperationInfo info : beanInfo.getOperations()) {
                if (info.getName().equals(operationName)) {
                    operationInfo = info;
                }
            }

            Assert.assertNotNull(operationInfo, "OperationInfo for " + operationName);
            Assert.assertEquals(operationInfo.getName(), operationName, "Operation Name for " + operationName);
            Assert.assertEquals(operationInfo.getImpact(), MBeanOperationInfo.UNKNOWN, "Operation Impact for " + operationName);
            Assert.assertEquals(operationInfo.getReturnType(), Object.class.getName(), "Operation Return Type for " + operationName);
            Assert.assertEquals(operationInfo.getSignature().length, 1, "Operation Parameter Length for " + operationName);
            MBeanParameterInfo parameterInfo = operationInfo.getSignature()[0];
            Assert.assertEquals(parameterInfo.getName(), "value", "Operation Parameter[0] Name for " + operationName);
            Assert.assertEquals(parameterInfo.getType(), Object.class.getName(), "Operation Parameter[0] Type for " + operationName);
        }
    }

    @Test(dataProvider = "fixtures")
    public void testGet(String attribute, boolean isIs, Object[] values, Class<?> clazz)
            throws MalformedObjectNameException, InstanceNotFoundException, IOException, ReflectionException,
            AttributeNotFoundException, MBeanException, NoSuchMethodException, InvocationTargetException,
            IllegalAccessException
    {
        String methodName = "set" + attribute;
        for (Pair<ObjectName, ?> pair : objects) {
            String attributeName = toFeatureName(attribute, pair);
            SimpleObject simpleObject = toSimpleObject(pair);
            Method setter = simpleObject.getClass().getMethod(methodName, clazz);

            for (Object value : values) {
                setter.invoke(simpleObject, value);

                Assert.assertEquals(server.getAttribute(pair.left, attributeName), value);
            }
        }
    }

    @Test(dataProvider = "fixtures")
    public void testSet(String attribute, boolean isIs, Object[] values, Class<?> clazz)
            throws MalformedObjectNameException, InstanceNotFoundException, IOException, ReflectionException,
            AttributeNotFoundException, MBeanException, NoSuchMethodException, InvocationTargetException,
            IllegalAccessException, InvalidAttributeValueException
    {
        String methodName = (isIs ? "is" : "get") + attribute;

        for (Pair<ObjectName, ?> pair : objects) {
            String attributeName = toFeatureName(attribute, pair);
            SimpleObject simpleObject = toSimpleObject(pair);
            Method getter = simpleObject.getClass().getMethod(methodName);

            for (Object value : values) {
                server.setAttribute(pair.left, new javax.management.Attribute(attributeName, value));

                Assert.assertEquals(getter.invoke(simpleObject), value);
            }
        }
    }

    @Test
    public void testSetFailsOnNotManaged()
            throws InstanceNotFoundException, IOException, InvalidAttributeValueException, ReflectionException,
            AttributeNotFoundException, MBeanException
    {
        for (Pair<ObjectName, ?> pair : objects) {
            SimpleObject simpleObject = toSimpleObject(pair);

            simpleObject.setNotManaged(1);
            try {
                server.setAttribute(pair.left, new javax.management.Attribute("NotManaged", 2));
                Assert.fail("Should not allow setting unmanaged attribute");
            }
            catch (AttributeNotFoundException e) {
                // ignore
            }

            Assert.assertEquals(simpleObject.getNotManaged(), 1);
        }
    }

    @Test
    public void testGetFailsOnNotManaged()
            throws InstanceNotFoundException, IOException, InvalidAttributeValueException, ReflectionException,
            AttributeNotFoundException, MBeanException
    {

        for (Pair<ObjectName, ?> pair : objects) {
            try {
                server.getAttribute(pair.left, "NotManaged");
                Assert.fail("Should not allow getting unmanaged attribute");
            }
            catch (AttributeNotFoundException e) {
                // ignore
            }
        }
    }

    @Test
    public void testGetFailsOnWriteOnly()
            throws InstanceNotFoundException, IOException, ReflectionException, MBeanException
    {
        for (Pair<ObjectName, ?> pair : objects) {
            try {
                server.getAttribute(pair.left, "WriteOnly");
                Assert.fail("Should not allow getting write-only attribute");
            }
            catch (AttributeNotFoundException e) {
                // ignore
            }
        }
    }

    @Test
    public void testSetFailsOnReadOnly()
            throws InstanceNotFoundException, IOException, ReflectionException, MBeanException,
            InvalidAttributeValueException
    {
        for (Pair<ObjectName, ?> pair : objects) {
            SimpleObject simpleObject = toSimpleObject(pair);
            simpleObject.setReadOnly(1);
            try {
                server.setAttribute(pair.left, new javax.management.Attribute("ReadOnly", 2));
                Assert.fail("Should not allow setting read-only attribute");
            }
            catch (AttributeNotFoundException e) {
                // ignore
            }

            Assert.assertEquals(simpleObject.getReadOnly(), 1);
        }
    }

    @Test
    public void testDescription()
            throws IntrospectionException, InstanceNotFoundException, ReflectionException
    {
        for (Pair<ObjectName, ?> pair : objects) {
            boolean described = false;
            for (MBeanAttributeInfo info : server.getMBeanInfo(pair.left).getAttributes()) {
                String attributeName = toFeatureName("DescribedInt", pair);
                if (info.getName().equals(attributeName)) {
                    Assert.assertEquals("epic description", info.getDescription());
                    described = true;
                }
                else {
                    Assert.assertEquals("", info.getDescription());
                }
            }
            Assert.assertTrue(described);
        }
    }

    @Test(dataProvider = "fixtures")
    public void testOperation(String attribute, boolean isIs, Object[] values, Class<?> clazz)
            throws InstanceNotFoundException, IOException, ReflectionException, MBeanException
    {
        for (Pair<ObjectName, ?> pair : objects) {
            for (Object value : values) {
                String operationName = toFeatureName("echo", pair);
                Assert.assertEquals(server.invoke(pair.left, operationName, new Object[] { value },
                                                  new String[] { Object.class.getName() }), value);
            }
        }
    }

    @DataProvider(name = "fixtures")
    Object[][] getFixtures()
    {
        return new Object[][] {

                new Object[] { "BooleanValue", true, new Object[] { true, false }, Boolean.TYPE },
                new Object[] { "BooleanBoxedValue", true, new Object[] { true, false, null }, Boolean.class },
                new Object[] { "ByteValue", false, new Object[] { Byte.MAX_VALUE, Byte.MIN_VALUE, (byte) 0 },
                               Byte.TYPE },
                new Object[] { "ByteBoxedValue", false, new Object[] { Byte.MAX_VALUE, Byte.MIN_VALUE, (byte) 0, null },
                               Byte.class },

                new Object[] { "ShortValue", false, new Object[] { Short.MAX_VALUE, Short.MIN_VALUE, (short) 0 },
                               Short.TYPE },
                new Object[] { "ShortBoxedValue", false,
                               new Object[] { Short.MAX_VALUE, Short.MIN_VALUE, (short) 0, null }, Short.class },

                new Object[] { "IntegerValue", false, new Object[] { Integer.MAX_VALUE, Integer.MIN_VALUE, 0 },
                               Integer.TYPE },
                new Object[] { "IntegerBoxedValue", false,
                               new Object[] { Integer.MAX_VALUE, Integer.MIN_VALUE, 0, null }, Integer.class },

                new Object[] { "LongValue", false, new Object[] { Long.MAX_VALUE, Long.MIN_VALUE, 0L }, Long.TYPE },
                new Object[] { "LongBoxedValue", false, new Object[] { Long.MAX_VALUE, Long.MIN_VALUE, 0L, null },
                               Long.class },

                new Object[] { "FloatValue", false,
                               new Object[] { -Float.MIN_VALUE, -Float.MAX_VALUE, Float.MAX_VALUE, Float.MIN_VALUE, 0.0f,
                                              Float.NaN }, Float.TYPE },
                new Object[] { "FloatBoxedValue", false,
                               new Object[] { -Float.MIN_VALUE, -Float.MAX_VALUE, Float.MAX_VALUE, Float.MIN_VALUE, 0.0f,
                                              Float.NaN, null }, Float.class },

                new Object[] { "DoubleValue", false,
                               new Object[] { -Double.MIN_VALUE, -Double.MAX_VALUE, Double.MAX_VALUE, Double.MIN_VALUE,
                                              0.0, Double.NaN }, Double.TYPE },
                new Object[] { "DoubleBoxedValue", false,
                               new Object[] { -Double.MIN_VALUE, -Double.MAX_VALUE, Double.MAX_VALUE, Double.MIN_VALUE,
                                              0.0, Double.NaN }, Double.class },

                new Object[] { "StringValue", false, new Object[] { null, "hello there" }, String.class },

                new Object[] { "ObjectValue", false, new Object[] { "random object", 1, true }, Object.class }

        };
    }

    private String toFeatureName(String attribute, Pair<ObjectName, ?> pair)
    {
        String attributeName;
        if (pair.right instanceof NestedObject) {
            attributeName = "SimpleObject." + attribute;
        }
        else {
            attributeName = attribute;
        }
        return attributeName;
    }

    private SimpleObject toSimpleObject(Pair<ObjectName, ?> pair)
    {
        Object object = pair.right;
        SimpleObject simpleObject;
        if (object instanceof SimpleObject) {
            simpleObject = (SimpleObject) object;
        }
        else if (object instanceof FlattenObject) {
            simpleObject = ((FlattenObject) object).getSimpleObject();
        }
        else if (object instanceof NestedObject) {
            simpleObject = ((NestedObject) object).getSimpleObject();
        }
        else {
            throw new IllegalArgumentException("Expected objects of type SimpleObject or FlattenObject but got " + object.getClass().getName());
        }
        return simpleObject;
    }
}


