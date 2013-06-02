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
import org.testng.annotations.Test;
import org.weakref.jmx.testing.TestingMBeanServer;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.NotBoundException;
import java.util.ArrayList;
import java.util.List;

import static org.weakref.jmx.Util.getUniqueObjectName;

public class TestExporter extends AbstractMbeanTest<TestExporter.NamedObject>
{
    private MBeanServer server;

    private List<NamedObject> objects;

    static class NamedObject
    {
        final ObjectName objectName;
        final Object object;

        NamedObject(ObjectName objectName, Object object)
        {
            this.objectName = objectName;
            this.object = object;
        }

        static NamedObject of(ObjectName left, Object right)
        {
            return new NamedObject(left, right);
        }
    }

    @Override
    protected Object getObject(NamedObject namedObject)
    {
        return namedObject.object;
    }

    @BeforeMethod
    void setup()
            throws IOException, MalformedObjectNameException, NotBoundException
    {
        server = new TestingMBeanServer();

        objects = new ArrayList<NamedObject>(2);
        objects.add(NamedObject.of(getUniqueObjectName(), new SimpleObject()));
        objects.add(NamedObject.of(getUniqueObjectName(), new CustomAnnotationObject()));
        objects.add(NamedObject.of(getUniqueObjectName(), new FlattenObject()));
        objects.add(NamedObject.of(getUniqueObjectName(), new CustomFlattenAnnotationObject()));
        objects.add(NamedObject.of(getUniqueObjectName(), new NestedObject()));
        objects.add(NamedObject.of(getUniqueObjectName(), new CustomNestedAnnotationObject()));

        MBeanExporter exporter = new MBeanExporter(server);
        for (NamedObject namedObject : objects) {
            exporter.export(namedObject.objectName.getCanonicalName(), namedObject.object);
        }
    }

//    @AfterTest
//    public void teardown()
//            throws IOException, InstanceNotFoundException, MBeanRegistrationException
//    {
//        for (NamedObject pair : objects) {
//            server.unregisterMBean(pair.objectName);
//        }
//    }

//    @Test
//    public void testMBeanInfo()
//            throws IntrospectionException, InstanceNotFoundException, ReflectionException
//    {
//        for (NamedObject pair : objects) {
//            info.get
//        }
//    }

    @Test(dataProvider = "fixtures")
    public void testGetterAttributeInfo(String attribute, boolean isIs, Object[] values, Class<?> clazz)
            throws Exception
    {
        String methodName = "set" + attribute;
        for (NamedObject namedObject : objects) {
            String attributeName = toFeatureName(attribute, namedObject);
            SimpleObject simpleObject = toSimpleObject(namedObject);
            Method setter = simpleObject.getClass().getMethod(methodName, clazz);

            MBeanInfo info = server.getMBeanInfo(namedObject.objectName);
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

        for (NamedObject namedObject : objects) {
            String attributeName = toFeatureName(attribute, namedObject);
            SimpleObject simpleObject = toSimpleObject(namedObject);
            Method getter = simpleObject.getClass().getMethod(methodName);

            MBeanInfo info = server.getMBeanInfo(namedObject.objectName);
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

        for (NamedObject namedObject : objects) {
            MBeanInfo info = server.getMBeanInfo(namedObject.objectName);
            String attributeName = toFeatureName("NotManaged", namedObject);
            MBeanAttributeInfo attributeInfo = getAttributeInfo(info, attributeName);
            Assert.assertNull(attributeInfo, "AttributeInfo for " + attributeName);
        }
    }

    @Test
    public void testReadOnlyAttributeInfo()
            throws Exception
    {
        for (NamedObject namedObject : objects) {
            MBeanInfo info = server.getMBeanInfo(namedObject.objectName);
            String attributeName = toFeatureName("ReadOnly", namedObject);
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
        for (NamedObject namedObject : objects) {
            MBeanInfo info = server.getMBeanInfo(namedObject.objectName);
            String attributeName = toFeatureName("WriteOnly", namedObject);
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
        for (NamedObject namedObject : objects) {
            String operationName = toFeatureName("echo", namedObject);

            MBeanInfo beanInfo = server.getMBeanInfo(namedObject.objectName);
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
        for (NamedObject namedObject : objects) {
            String attributeName = toFeatureName(attribute, namedObject);
            SimpleObject simpleObject = toSimpleObject(namedObject);
            Method setter = simpleObject.getClass().getMethod(methodName, clazz);

            for (Object value : values) {
                setter.invoke(simpleObject, value);

                Assert.assertEquals(server.getAttribute(namedObject.objectName, attributeName), value);
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

        for (NamedObject namedObject : objects) {
            String attributeName = toFeatureName(attribute, namedObject);
            SimpleObject simpleObject = toSimpleObject(namedObject);
            Method getter = simpleObject.getClass().getMethod(methodName);

            for (Object value : values) {
                server.setAttribute(namedObject.objectName, new javax.management.Attribute(attributeName, value));

                Assert.assertEquals(getter.invoke(simpleObject), value);
            }
        }
    }

    @Test
    public void testSetFailsOnNotManaged()
            throws InstanceNotFoundException, IOException, InvalidAttributeValueException, ReflectionException,
            AttributeNotFoundException, MBeanException
    {
        for (NamedObject namedObject : objects) {
            SimpleObject simpleObject = toSimpleObject(namedObject);

            simpleObject.setNotManaged(1);
            try {
                server.setAttribute(namedObject.objectName, new javax.management.Attribute("NotManaged", 2));
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

        for (NamedObject namedObject : objects) {
            try {
                server.getAttribute(namedObject.objectName, "NotManaged");
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
        for (NamedObject namedObject : objects) {
            try {
                server.getAttribute(namedObject.objectName, "WriteOnly");
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
        for (NamedObject namedObject : objects) {
            SimpleObject simpleObject = toSimpleObject(namedObject);
            simpleObject.setReadOnly(1);
            try {
                server.setAttribute(namedObject.objectName, new javax.management.Attribute("ReadOnly", 2));
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
        for (NamedObject namedObject : objects) {
            boolean described = false;
            for (MBeanAttributeInfo info : server.getMBeanInfo(namedObject.objectName).getAttributes()) {
                String attributeName = toFeatureName("DescribedInt", namedObject);
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
        for (NamedObject namedObject : objects) {
            for (Object value : values) {
                String operationName = toFeatureName("echo", namedObject);
                Assert.assertEquals(server.invoke(namedObject.objectName, operationName, new Object[] { value },
                                                  new String[] { Object.class.getName() }), value);
            }
        }
    }
}


