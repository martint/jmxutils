package org.weakref.jmx;

import com.google.inject.name.Names;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;

import static org.weakref.jmx.Assert.assertEquals;

public class TestObjectNameBuilder
{

    @interface Ann
    {
    }

    static class AnnImpl implements Ann
    {
        @Override
        public Class<? extends Annotation> annotationType()
        {
            return Ann.class;
        }
    }

    static class Inner
    {
    }

    @Test
    public void testObjectNameBuilder1()
    {
        assertEquals(
                ObjectNames.builder(SimpleObject.class).build(),
                "org.weakref.jmx:name=SimpleObject");
    }

    @Test
    public void testObjectNameBuilder2()
    {
        assertEquals(
                ObjectNames.builder(SimpleObject.class, Names.named("1")).build(),
                "org.weakref.jmx:type=SimpleObject,name=1");
    }

    @Test
    public void testObjectNameBuilder3()
    {
        assertEquals(
                ObjectNames.builder(SimpleObject.class, Ann.class).build(),
                "org.weakref.jmx:type=SimpleObject,name=Ann");
    }

    @Test
    public void testObjectNameBuilder4()
    {
        assertEquals(
                ObjectNames.builder(SimpleObject.class, new AnnImpl()).build(),
                "org.weakref.jmx:type=SimpleObject,name=Ann");
    }

    @Test
    public void testObjectNameBuilder5()
    {
        assertEquals(
                ObjectNames.builder(Inner.class).build(),
                "org.weakref.jmx:name=Inner");
    }

    @Test
    public void testObjectNameBuilderWithString()
    {
        assertEquals(
                ObjectNames.builder(SimpleObject.class, "foo").build(),
                "org.weakref.jmx:type=SimpleObject,name=foo");
    }

    @Test
    public void testObjectNameBuilderWithProperty()
    {
        assertEquals(
                ObjectNames.builder(SimpleObject.class).withProperty("id", "5").build(),
                "org.weakref.jmx:name=SimpleObject,id=5");
    }

    @Test
    public void testObjectNameBuilderQuotesPropertyNames()
    {
        assertEquals(
                ObjectNames.builder(SimpleObject.class).withProperty("foo", "bar,baz").build(),
                "org.weakref.jmx:name=SimpleObject,foo=\"bar,baz\"");
    }
}
