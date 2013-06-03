package org.weakref.jmx;

import org.testng.Assert;
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

public class TestMBeanBuilder extends AbstractMbeanTest<Object>
{
    public TestMBeanBuilder()
    {
        objects = new ArrayList<Object>();
        objects.add(new SimpleObject());
        objects.add(new CustomAnnotationObject());
        objects.add(new FlattenObject());
        objects.add(new CustomFlattenAnnotationObject());
        objects.add(new NestedObject());
        objects.add(new CustomNestedAnnotationObject());
    }

    @Override
    protected Object getObject(Object o) {
        return o;
    }

    @Override
    protected MBeanInfo getMBeanInfo(Object object)
    {
        return MBeanBuilder.from(object).build().getMBeanInfo();
    }

    @Test(dataProvider = "fixtures")
    public void testOperationInfo(String attribute, boolean isIs, Object[] values, Class<?> clazz)
            throws Exception
    {
        for (Object object : objects) {
            String operationName = toFeatureName("echo", object);

            MBeanInfo beanInfo = getMBeanInfo(object);
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
            MBeanInfo mbeanInfo = getMBeanInfo(object);

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


}
