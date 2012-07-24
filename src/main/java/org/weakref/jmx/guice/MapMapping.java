package org.weakref.jmx.guice;

import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
import com.google.inject.Key;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Map.Entry;

class MapMapping<K, V>
{
    private final ObjectNameFunction<Entry<K, V>> objectNameFunction;
    private final Class<K> keyClass;
    private final Class<V> valueClass;

    MapMapping(Class<K> keyClass, Class<V> valueClass, ObjectNameFunction<Map.Entry<K, V>> objectNameFunction)
    {
        this.keyClass = keyClass;
        this.valueClass = valueClass;
        this.objectNameFunction = objectNameFunction;
    }

    public ObjectNameFunction<Map.Entry<K, V>> getObjectNameFunction()
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
