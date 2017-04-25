package org.weakref.jmx;

import com.google.common.collect.Iterables;
import org.testng.annotations.Test;

import javax.management.Attribute;

import java.util.Collection;

import static org.testng.Assert.assertEquals;

public class TestExpiration
{
    private static final long CACHE_DURATION_MILLIS = 100;
    private static final String DEFAULT_VALUE = "DEFAULT_VALUE";
    private static final String UPDATED_VALUE = "UPDATED_VALUE";

    public static class SingleValueObject
    {
        private String value = DEFAULT_VALUE;

        @Managed
        public String getValue()
        {
            return value;
        }

        @Managed
        public void setValue(String value)
        {
            this.value = value;
        }
    }

    public static class NestedWithExpiration
    {
        @Nested(cacheDurationMillis = CACHE_DURATION_MILLIS)
        public SingleValueObject getSingleValueObject()
        {
            return new SingleValueObject();
        }
    }

    public static class FlattenWithExpiration
    {
        @Flatten(cacheDurationMillis = CACHE_DURATION_MILLIS)
        public SingleValueObject getSingleValueObject()
        {
            return new SingleValueObject();
        }
    }

    @Test
    public void testFlatterExpiration()
            throws Exception
    {
        testExpiration(new FlattenWithExpiration());
    }

    @Test
    public void testNestedExpiration()
            throws Exception
    {
        testExpiration(new NestedWithExpiration());
    }

    private void testExpiration(Object object)
            throws Exception
    {

        MBean mBean = MBeanBuilder.from(object).build();
        Collection<MBeanAttribute> attributes = mBean.getAttributes();
        String attributeName = Iterables.getOnlyElement(attributes).getName();

        assertEquals(mBean.getAttribute(attributeName), DEFAULT_VALUE);

        mBean.setAttribute(new Attribute(attributeName, UPDATED_VALUE));
        assertEquals(mBean.getAttribute(attributeName), UPDATED_VALUE);

        Thread.sleep(CACHE_DURATION_MILLIS + 1);

        assertEquals(mBean.getAttribute(attributeName), DEFAULT_VALUE);
    }
}
