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

    @Override
    protected Object getAttribute(Object object, String attributeName)
            throws AttributeNotFoundException, MBeanException, ReflectionException
    {
        return MBeanBuilder.from(object).build().getAttribute(attributeName);
    }

    @Override
    protected void setAttribute(Object object, String attributeName, Object value)
            throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException
    {
        MBeanBuilder.from(object).build().setAttribute(new javax.management.Attribute(attributeName, value));
    }

    @Override
    protected Object invoke(Object object, Object value, String operationName)
            throws MBeanException, ReflectionException
    {
        return MBeanBuilder.from(object).build().invoke(operationName, new Object[]{value},
                new String[]{Object.class.getName()});
    }
}
