package org.weakref.jmx;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import javax.management.AttributeNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(PER_CLASS)
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

    @ParameterizedTest
    @MethodSource("getFixtures")
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
            assertThat(attributeInfo).describedAs("AttributeInfo for " + attributeName).isNotNull();
            assertThat(attributeInfo.getName()).describedAs("Attribute Name for " + attributeName).isEqualTo(attributeName);
            assertThat(attributeInfo.getType()).describedAs("Attribute type for " + attributeName).isEqualTo(setter.getParameterTypes()[0].getName());
            assertThat(attributeInfo.isIs()).describedAs("Attribute isIs for " + attributeName).isEqualTo(isIs);
            assertThat(attributeInfo.isReadable()).describedAs("Attribute Readable for " + attributeName).isTrue();
        }
    }

    @ParameterizedTest
    @MethodSource("getFixtures")
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
            assertThat(attributeInfo).describedAs("AttributeInfo for " + attributeName).isNotNull();
            assertThat(attributeInfo.getName()).describedAs("Attribute Name for " + attributeName).isEqualTo(attributeName);
            assertThat(attributeInfo.getType()).describedAs("Attribute Type for " + attributeName).isEqualTo(getter.getReturnType().getName());
            assertThat(attributeInfo.isWritable()).describedAs("Attribute Writable for " + attributeName).isTrue();
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
            assertThat(attributeInfo).describedAs("AttributeInfo for " + attributeName).isNull();
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
            assertThat(attributeInfo).describedAs("AttributeInfo for " + attributeName).isNotNull();
            assertThat(attributeInfo.getName()).describedAs("Attribute Name for " + attributeName).isEqualTo(attributeName);
            assertThat(attributeInfo.getType()).describedAs("Attribute Type for " + attributeName).isEqualTo("int");
            assertThat(attributeInfo.isReadable()).describedAs("Attribute Readable for " + attributeName).isTrue();
            assertThat(attributeInfo.isWritable()).describedAs("Attribute Writable for " + attributeName).isFalse();
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
            assertThat(attributeInfo).describedAs("AttributeInfo for " + attributeName).isNotNull();
            assertThat(attributeInfo.getName()).describedAs("Attribute Name for " + attributeName).isEqualTo(attributeName);
            assertThat(attributeInfo.getType()).describedAs("Attribute Type for " + attributeName).isEqualTo("int");
            assertThat(attributeInfo.isReadable()).describedAs("Attribute Readable for " + attributeName).isFalse();
            assertThat(attributeInfo.isWritable()).describedAs("Attribute Writable for " + attributeName).isTrue();
        }
    }

    private static MBeanAttributeInfo getAttributeInfo(MBeanInfo info, String attributeName)
    {
        for (MBeanAttributeInfo attributeInfo : info.getAttributes()) {
            if (attributeInfo.getName().equals(attributeName)) {
                return attributeInfo;
            }
        }
        return null;
    }

    @ParameterizedTest
    @MethodSource("getFixtures")
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

            assertThat(operationInfo).describedAs("OperationInfo for " + operationName).isNotNull();
            assertThat(operationInfo.getName()).describedAs("Operation Name for " + operationName).isEqualTo(operationName);
            assertThat(operationInfo.getImpact()).describedAs("Operation Impact for " + operationName).isEqualTo(MBeanOperationInfo.UNKNOWN);
            assertThat(operationInfo.getReturnType()).describedAs("Operation Return Type for " + operationName).isEqualTo(Object.class.getName());
            assertThat(operationInfo.getSignature().length).describedAs("Operation Parameter Length for " + operationName).isEqualTo(1);
            MBeanParameterInfo parameterInfo = operationInfo.getSignature()[0];
            assertThat(parameterInfo.getName()).describedAs("Operation Parameter[0] Name for " + operationName).isEqualTo("value");
            assertThat(parameterInfo.getType()).describedAs("Operation Parameter[0] Type for " + operationName).isEqualTo(Object.class.getName());
        }
    }

    @ParameterizedTest
    @MethodSource("getFixtures")
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

                assertThat(getAttribute(t, attributeName)).isEqualTo(value);
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getFixtures")
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

                assertThat(getter.invoke(simpleObject)).isEqualTo(value);
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
                throw new AssertionError("Should not allow setting unmanaged attribute");
            }
            catch (AttributeNotFoundException e) {
                // ignore
            }

            assertThat(simpleObject.getNotManaged()).isEqualTo(1);
        }
    }

    @Test
    public void testGetFailsOnNotManaged()
            throws Exception
    {

        for (T t : objects) {
            try {
                getAttribute(t, "NotManaged");
                throw new AssertionError("Should not allow getting unmanaged attribute");
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
                throw new AssertionError("Should not allow getting write-only attribute");
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
                throw new AssertionError("Should not allow setting read-only attribute");
            }
            catch (AttributeNotFoundException e) {
                // ignore
            }

            assertThat(simpleObject.getReadOnly()).isEqualTo(1);
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
                    assertThat(info.getDescription()).isEqualTo("epic description");
                    described = true;
                }
                else {
                    assertThat(info.getDescription()).isEqualTo("");
                }
            }
            assertThat(described).isTrue();
        }
    }

    @ParameterizedTest
    @MethodSource("getFixtures")
    public void testOperation(String attribute, boolean isIs, Object[] values, Class<?> clazz)
            throws Exception
    {
        for (T t : objects) {
            for (Object value : values) {
                String operationName = toFeatureName("echo", t);
                assertThat(invoke(t, value, operationName)).isEqualTo(value);
            }
        }
    }

    static Object[][] getFixtures()
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

    private String toFeatureName(String attribute, T t)
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

    private SimpleObject toSimpleObject(T t)
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
