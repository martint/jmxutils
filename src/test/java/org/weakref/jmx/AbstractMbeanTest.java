package org.weakref.jmx;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
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
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public abstract class AbstractMbeanTest<T>
{
    protected List<T> objects;

    protected abstract Object getObject(T t);

    protected abstract MBeanInfo getMBeanInfo(T t)
            throws Exception;

    protected abstract Object getAttribute(T t, String attributeName)
            throws Exception;

    protected abstract void setAttribute(T t, String attributeName, Object value)
            throws Exception;

    protected abstract Object invoke(T t, Object value, String operationName)
            throws Exception;

    @Test(dataProvider = "fixtures")
    public void testGetterAttributeInfo(String attribute, boolean isIs, Object[] values, Class<?> clazz)
            throws Exception
    {
        String methodName = "set" + attribute;
        for (T t : objects) {
            String attributeName = toFeatureName(attribute, t);
            SimpleObject simpleObject = toSimpleObject(t);
            Method setter = simpleObject.getClass().getMethod(methodName, clazz);

            MBeanInfo info = getMBeanInfo(t);
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

        for (T t : objects) {
            String attributeName = toFeatureName(attribute, t);
            SimpleObject simpleObject = toSimpleObject(t);
            Method getter = simpleObject.getClass().getMethod(methodName);

            MBeanInfo info = getMBeanInfo(t);
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

        for (T t : objects) {
            MBeanInfo info = getMBeanInfo(t);
            String attributeName = toFeatureName("NotManaged", t);
            MBeanAttributeInfo attributeInfo = getAttributeInfo(info, attributeName);
            Assert.assertNull(attributeInfo, "AttributeInfo for " + attributeName);
        }
    }

    @Test
    public void testReadOnlyAttributeInfo()
            throws Exception
    {
        for (T t : objects) {
            MBeanInfo info = getMBeanInfo(t);

            String attributeName = toFeatureName("ReadOnly", t);
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
        for (T t : objects) {
            MBeanInfo info = getMBeanInfo(t);
            String attributeName = toFeatureName("WriteOnly", t);
            MBeanAttributeInfo attributeInfo = getAttributeInfo(info, attributeName);
            Assert.assertNotNull(attributeInfo, "AttributeInfo for " + attributeName);
            Assert.assertEquals(attributeInfo.getName(), attributeName, "Attribute Name for " + attributeName);
            Assert.assertEquals(attributeInfo.getType(), "int", "Attribute Type for " + attributeName);
            Assert.assertFalse(attributeInfo.isReadable(), "Attribute Readable for " + attributeName);
            Assert.assertTrue(attributeInfo.isWritable(), "Attribute Writable for " + attributeName);
        }
    }

    protected MBeanAttributeInfo getAttributeInfo(MBeanInfo info, String attributeName)
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
        for (T t : objects) {
            String operationName = toFeatureName("echo", t);

            MBeanInfo beanInfo = getMBeanInfo(t);
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
            throws Exception
    {
        String methodName = "set" + attribute;
        for (T t : objects) {
            String attributeName = toFeatureName(attribute, t);
            SimpleObject simpleObject = toSimpleObject(t);
            Method setter = simpleObject.getClass().getMethod(methodName, clazz);

            for (Object value : values) {
                setter.invoke(simpleObject, value);

                Assert.assertEquals(getAttribute(t, attributeName), value);
            }
        }
    }

    @Test(dataProvider = "fixtures")
    public void testSet(String attribute, boolean isIs, Object[] values, Class<?> clazz)
            throws Exception
    {
        String methodName = (isIs ? "is" : "get") + attribute;

        for (T t : objects) {
            String attributeName = toFeatureName(attribute, t);
            SimpleObject simpleObject = toSimpleObject(t);
            Method getter = simpleObject.getClass().getMethod(methodName);

            for (Object value : values) {
                setAttribute(t, attributeName, value);

                Assert.assertEquals(getter.invoke(simpleObject), value);
            }
        }
    }

    @Test
    public void testSetFailsOnNotManaged()
            throws Exception
    {
        for (T t : objects) {
            SimpleObject simpleObject = toSimpleObject(t);

            simpleObject.setNotManaged(1);
            try {
                setAttribute(t, "NotManaged", 2);
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
            throws Exception
    {

        for (T t : objects) {
            try {
                getAttribute(t, "NotManaged");
                Assert.fail("Should not allow getting unmanaged attribute");
            }
            catch (AttributeNotFoundException e) {
                // ignore
            }
        }
    }

    @Test
    public void testGetFailsOnWriteOnly()
            throws Exception
    {
        for (T t : objects) {
            try {
                getAttribute(t, "WriteOnly");
                Assert.fail("Should not allow getting write-only attribute");
            }
            catch (AttributeNotFoundException e) {
                // ignore
            }
        }
    }

    @Test
    public void testSetFailsOnReadOnly()
            throws Exception
    {
        for (T t : objects) {
            SimpleObject simpleObject = toSimpleObject(t);
            simpleObject.setReadOnly(1);
            try {
                setAttribute(t, "ReadOnly", 2);
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
            throws Exception
    {
        for (T t : objects) {
            boolean described = false;
            for (MBeanAttributeInfo info : getMBeanInfo(t).getAttributes()) {
                String attributeName = toFeatureName("DescribedInt", t);
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
            throws Exception
    {
        for (T t : objects) {
            for (Object value : values) {
                String operationName = toFeatureName("echo", t);
                Assert.assertEquals(invoke(t, value, operationName), value);
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

    protected String toFeatureName(String attribute, T t)
    {
        String attributeName;
        if (getObject(t) instanceof NestedObject) {
            attributeName = "SimpleObject." + attribute;
        }
        else {
            attributeName = attribute;
        }
        return attributeName;
    }

    protected SimpleObject toSimpleObject(T t)
    {
        SimpleObject simpleObject;
        if (getObject(t) instanceof SimpleObject) {
            simpleObject = (SimpleObject) getObject(t);
        }
        else if (getObject(t) instanceof FlattenObject) {
            simpleObject = ((FlattenObject) getObject(t)).getSimpleObject();
        }
        else if (getObject(t) instanceof NestedObject) {
            simpleObject = ((NestedObject) getObject(t)).getSimpleObject();
        }
        else {
            throw new IllegalArgumentException("Expected objects of type SimpleObject or FlattenObject but got " + getObject(t).getClass().getName());
        }
        return simpleObject;
    }
}
