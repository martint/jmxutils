package org.weakref.jmx;

import com.google.inject.name.Names;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(ObjectNames.builder(SimpleObject.class).build())
                .isEqualTo("org.weakref.jmx:name=SimpleObject");
    }

    @Test
    public void testObjectNameBuilder2()
    {
        assertThat(ObjectNames.builder(SimpleObject.class, Names.named("1")).build())
                .isEqualTo("org.weakref.jmx:type=SimpleObject,name=1");
    }

    @Test
    public void testObjectNameBuilder3()
    {
        assertThat(ObjectNames.builder(SimpleObject.class, Ann.class).build())
                .isEqualTo("org.weakref.jmx:type=SimpleObject,name=Ann");
    }

    @Test
    public void testObjectNameBuilder4()
    {
        assertThat(ObjectNames.builder(SimpleObject.class, new AnnImpl()).build())
                .isEqualTo("org.weakref.jmx:type=SimpleObject,name=Ann");
    }

    @Test
    public void testObjectNameBuilder5()
    {
        assertThat(ObjectNames.builder(Inner.class).build())
                .isEqualTo("org.weakref.jmx:name=Inner");
    }

    @Test
    public void testObjectNameBuilderWithString()
    {
        assertThat(ObjectNames.builder(SimpleObject.class, "foo").build())
                .isEqualTo("org.weakref.jmx:type=SimpleObject,name=foo");
    }

    @Test
    public void testObjectNameBuilderWithProperty()
    {
        assertThat(ObjectNames.builder(SimpleObject.class).withProperty("id", "5").build())
                .isEqualTo("org.weakref.jmx:name=SimpleObject,id=5");
    }

    @Test
    public void testObjectNameBuilderQuotesPropertyNames()
    {
        assertThat(ObjectNames.builder(SimpleObject.class).withProperty("foo", "bar,baz").build())
                .isEqualTo("org.weakref.jmx:name=SimpleObject,foo=\"bar,baz\"");
    }
}
