package org.weakref.jmx;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MalformedObjectNameException;
import javax.management.ReflectionException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class TestMBeanBuilder
{
    private final List<Object> objects = new ArrayList<Object>();

    public TestMBeanBuilder()
    {
        objects.add(new SimpleObject());
        objects.add(new CustomAnnotationObject());
        objects.add(new FlattenObject());
        objects.add(new CustomFlattenAnnotationObject());
        objects.add(new NestedObject());
        objects.add(new CustomNestedAnnotationObject());
    }

    @Test(dataProvider = "fixtures")
    public void testGetterAttributeInfo(String attribute, boolean isIs, Object[] values, Class<?> clazz)
            throws Exception
    {
        String methodName = "set" + attribute;
        for (Object object : objects) {
            MBeanInfo info = MBeanBuilder.from(object).build().getMBeanInfo();

            String attributeName = toFeatureName(attribute, object);
            SimpleObject simpleObject = toSimpleObject(object);
            Method setter = simpleObject.getClass().getMethod(methodName, clazz);

            MBeanAttributeInfo attributeInfo = getAttributeInfo(info, attributeName);
            assertNotNull(attributeInfo, "AttributeInfo for " + attributeName);
            assertEquals(attributeInfo.getName(), attributeName, "Attribute Name for " + attributeName);
            assertEquals(attributeInfo.getType(), setter.getParameterTypes()[0].getName(), "Attribute type for " + attributeName);
            assertEquals(attributeInfo.isIs(), isIs, "Attribute isIs for " + attributeName);
            assertTrue(attributeInfo.isReadable(), "Attribute Readable for " + attributeName);
        }
    }

    @Test(dataProvider = "fixtures")
    public void testSetterAttributeInfo(String attribute, boolean isIs, Object[] values, Class<?> clazz)
            throws Exception
    {
        String methodName = (isIs ? "is" : "get") + attribute;

        for (Object object : objects) {
            String attributeName = toFeatureName(attribute, object);
            SimpleObject simpleObject = toSimpleObject(object);
            Method getter = simpleObject.getClass().getMethod(methodName);

            MBeanInfo info = MBeanBuilder.from(object).build().getMBeanInfo();
            MBeanAttributeInfo attributeInfo = getAttributeInfo(info, attributeName);
            assertNotNull(attributeInfo, "AttributeInfo for " + attributeName);
            assertEquals(attributeInfo.getName(), attributeName, "Attribute Name for " + attributeName);
            assertEquals(attributeInfo.getType(), getter.getReturnType().getName(), "Attribute Type for " + attributeName);
            assertTrue(attributeInfo.isWritable(), "Attribute Writable for " + attributeName);
        }
    }

    @Test
    public void testNotManagedAttributeInfo()
            throws Exception
    {
        for (Object object : objects) {
            MBeanInfo info = MBeanBuilder.from(object).build().getMBeanInfo();

            String attributeName = toFeatureName("NotManaged", object);
            MBeanAttributeInfo attributeInfo = getAttributeInfo(info, attributeName);
            Assert.assertNull(attributeInfo, "AttributeInfo for " + attributeName);
        }
    }

    @Test
    public void testReadOnlyAttributeInfo()
            throws Exception
    {
        for (Object object : objects) {
            MBeanInfo info = MBeanBuilder.from(object).build().getMBeanInfo();

            String attributeName = toFeatureName("ReadOnly", object);
            MBeanAttributeInfo attributeInfo = getAttributeInfo(info, attributeName);
            assertNotNull(attributeInfo, "AttributeInfo for " + attributeName);
            assertEquals(attributeInfo.getName(), attributeName, "Attribute Name for " + attributeName);
            assertEquals(attributeInfo.getType(), "int", "Attribute Type for " + attributeName);
            assertTrue(attributeInfo.isReadable(), "Attribute Readable for " + attributeName);
            assertFalse(attributeInfo.isWritable(), "Attribute Writable for " + attributeName);
        }
    }

    @Test
    public void testWriteOnlyAttributeInfo()
            throws Exception
    {
        for (Object object : objects) {
            MBeanInfo info = MBeanBuilder.from(object).build().getMBeanInfo();

            String attributeName = toFeatureName("WriteOnly", object);
            MBeanAttributeInfo attributeInfo = getAttributeInfo(info, attributeName);
            assertNotNull(attributeInfo, "AttributeInfo for " + attributeName);
            assertEquals(attributeInfo.getName(), attributeName, "Attribute Name for " + attributeName);
            assertEquals(attributeInfo.getType(), "int", "Attribute Type for " + attributeName);
            assertFalse(attributeInfo.isReadable(), "Attribute Readable for " + attributeName);
            assertTrue(attributeInfo.isWritable(), "Attribute Writable for " + attributeName);
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
        for (Object object : objects) {
            String operationName = toFeatureName("echo", object);

            MBeanInfo beanInfo = MBeanBuilder.from(object).build().getMBeanInfo();
            MBeanOperationInfo operationInfo = null;
            for (MBeanOperationInfo info : beanInfo.getOperations()) {
                if (info.getName().equals(operationName)) {
                    operationInfo = info;
                }
            }

            assertNotNull(operationInfo, "OperationInfo for " + operationName);
            assertEquals(operationInfo.getName(), operationName, "Operation Name for " + operationName);
            assertEquals(operationInfo.getImpact(), MBeanOperationInfo.UNKNOWN, "Operation Impact for " + operationName);
            assertEquals(operationInfo.getReturnType(), Object.class.getName(), "Operation Return Type for " + operationName);
            assertEquals(operationInfo.getSignature().length, 1, "Operation Parameter Length for " + operationName);
            MBeanParameterInfo parameterInfo = operationInfo.getSignature()[0];
            assertEquals(parameterInfo.getName(), "value", "Operation Parameter[0] Name for " + operationName);
            assertEquals(parameterInfo.getType(), Object.class.getName(), "Operation Parameter[0] Type for " + operationName);
        }
    }

    @Test(dataProvider = "fixtures")
    public void testGet(String attribute, boolean isIs, Object[] values, Class<?> clazz)
            throws MalformedObjectNameException, InstanceNotFoundException, IOException, ReflectionException,
            AttributeNotFoundException, MBeanException, NoSuchMethodException, InvocationTargetException,
            IllegalAccessException
    {
        String methodName = "set" + attribute;
        for (Object object : objects) {
            String attributeName = toFeatureName(attribute, object);
            SimpleObject simpleObject = toSimpleObject(object);
            Method setter = simpleObject.getClass().getMethod(methodName, clazz);

            MBean mbean = MBeanBuilder.from(object).build();
            for (Object value : values) {
                setter.invoke(simpleObject, value);

                assertEquals(mbean.getAttribute(attributeName), value);
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

        for (Object object : objects) {
            String attributeName = toFeatureName(attribute, object);
            SimpleObject simpleObject = toSimpleObject(object);
            Method getter = simpleObject.getClass().getMethod(methodName);

            MBean mbean = MBeanBuilder.from(object).build();
            for (Object value : values) {
                mbean.setAttribute(new javax.management.Attribute(attributeName, value));

                assertEquals(getter.invoke(simpleObject), value);
            }
        }
    }

    @Test
    public void testSetFailsOnNotManaged()
            throws InstanceNotFoundException, IOException, InvalidAttributeValueException, ReflectionException,
            AttributeNotFoundException, MBeanException
    {
        for (Object object : objects) {
            SimpleObject simpleObject = toSimpleObject(object);

            simpleObject.setNotManaged(1);

            MBean mbean = MBeanBuilder.from(object).build();
            try {
                mbean.setAttribute(new javax.management.Attribute("NotManaged", 2));
                Assert.fail("Should not allow setting unmanaged attribute");
            }
            catch (AttributeNotFoundException e) {
                // ignore
            }

            assertEquals(simpleObject.getNotManaged(), 1);
        }
    }

    @Test
    public void testGetFailsOnNotManaged()
            throws InstanceNotFoundException, IOException, InvalidAttributeValueException, ReflectionException,
            AttributeNotFoundException, MBeanException
    {

        for (Object object : objects) {
            try {
                MBean mbean = MBeanBuilder.from(object).build();
                mbean.getAttribute("NotManaged");
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
        for (Object object : objects) {
            try {
                MBean mbean = MBeanBuilder.from(object).build();
                mbean.getAttribute("WriteOnly");
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
        for (Object object : objects) {
            SimpleObject simpleObject = toSimpleObject(object);
            simpleObject.setReadOnly(1);
            try {
                MBean mbean = MBeanBuilder.from(object).build();
                mbean.setAttribute(new javax.management.Attribute("ReadOnly", 2));
                Assert.fail("Should not allow setting read-only attribute");
            }
            catch (AttributeNotFoundException e) {
                // ignore
            }

            assertEquals(simpleObject.getReadOnly(), 1);
        }
    }

    @Test
    public void testDescription()
            throws IntrospectionException, InstanceNotFoundException, ReflectionException
    {
        for (Object object : objects) {
            boolean described = false;
            MBeanInfo mbeanInfo = MBeanBuilder.from(object).build().getMBeanInfo();

            for (MBeanAttributeInfo info : mbeanInfo.getAttributes()) {
                String attributeName = toFeatureName("DescribedInt", object);
                if (info.getName().equals(attributeName)) {
                    assertEquals("epic description", info.getDescription());
                    described = true;
                }
                else {
                    assertEquals("", info.getDescription());
                }
            }
            assertTrue(described);
        }
    }

    @Test(dataProvider = "fixtures")
    public void testOperation(String attribute, boolean isIs, Object[] values, Class<?> clazz)
            throws InstanceNotFoundException, IOException, ReflectionException, MBeanException
    {
        for (Object object : objects) {
            MBean mbean = MBeanBuilder.from(object).build();

            for (Object value : values) {
                String operationName = toFeatureName("echo", object);
                assertEquals(mbean.invoke(operationName, new Object[] { value },
                                          new String[] { Object.class.getName() }), value);
            }
        }
    }

    @DataProvider(name = "fixtures")
    private Object[][] getFixtures()
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

    private String toFeatureName(String attribute, Object object)
    {
        String attributeName;
        if (object instanceof NestedObject) {
            attributeName = "SimpleObject." + attribute;
        }
        else {
            attributeName = attribute;
        }
        return attributeName;
    }

    private SimpleObject toSimpleObject(Object object)
    {
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
