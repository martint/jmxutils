package org.weakref.jmx.guice;

import javax.management.ObjectName;

public interface MapObjectNameFunction<K, V>
{
    ObjectName name(K key, V value);
}
