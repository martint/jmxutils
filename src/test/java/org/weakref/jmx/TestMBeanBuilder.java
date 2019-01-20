package org.weakref.jmx;

import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.ReflectionException;
import java.util.ArrayList;

public class TestMBeanBuilder extends AbstractMbeanTest<Object>
{
    public TestMBeanBuilder()
    {
        objects = new ArrayList<>();
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
