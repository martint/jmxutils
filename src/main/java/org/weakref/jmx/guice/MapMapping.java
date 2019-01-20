package org.weakref.jmx.guice;

import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
import com.google.inject.Key;
import org.weakref.jmx.ObjectNameGenerator;

import javax.management.ObjectName;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;

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

    public Key<Map<K, V>> getKey()
    {
        return (Key<Map<K, V>>) Key.get(mapOf(keyClass, valueClass));
    }

    private static <K, V> Type mapOf(Class<K> keyClass, Class<V> valueClass)
    {
        return new TypeToken<Map<K, V>>() {}
                .where(new TypeParameter<K>() {}, TypeToken.of(keyClass))
                .where(new TypeParameter<V>() {}, TypeToken.of(valueClass))
                .getType();
    }

}
