package org.weakref.jmx;

import org.testng.annotations.DataProvider;

public abstract class AbstractMbeanTest<T>
{
    protected abstract Object getObject(T t);

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
