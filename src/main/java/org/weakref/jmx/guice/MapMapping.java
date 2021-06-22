package org.weakref.jmx.guice;

import com.google.inject.Key;
import org.weakref.jmx.ObjectNameGenerator;

import javax.management.ObjectName;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;

import static com.google.inject.util.Types.mapOf;

class MapMapping<K, V>
{
    private final BiFunction<ObjectNameGenerator, Entry<K, V>, ObjectName> objectNameFunction;
    private final Class<K> keyClass;
    private final Class<V> valueClass;

    MapMapping(Class<K> keyClass, Class<V> valueClass, BiFunction<ObjectNameGenerator, Entry<K, V>, ObjectName> objectNameFunction)
    {
        this.keyClass = keyClass;
        this.valueClass = valueClass;
        this.objectNameFunction = objectNameFunction;
    }

    public BiFunction<ObjectNameGenerator, Entry<K, V>, ObjectName> getObjectNameFunction()
    {
        return objectNameFunction;
    }

    @SuppressWarnings("unchecked")
    public Key<Map<K, V>> getKey()
    {
        return (Key<Map<K, V>>) Key.get(mapOf(keyClass, valueClass));
    }
}
