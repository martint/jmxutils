package org.weakref.jmx.guice;

public interface MapNamingFunction<K, V>
{
    String name(K key, V value);
}
