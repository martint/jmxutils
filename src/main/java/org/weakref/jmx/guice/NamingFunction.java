package org.weakref.jmx.guice;

public interface NamingFunction<T>
{
    String name(T object);
}
