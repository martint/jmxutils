package org.weakref.jmx.guice;

import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
import com.google.inject.Key;

import java.lang.reflect.Type;
import java.util.Set;

class SetMapping<T>
{
    private final ObjectNameFunction<T> objectNameFunction;
    private final Class<T> clazz;

    SetMapping(Class<T> key, ObjectNameFunction<T> objectNameFunction)
    {
        this.clazz = key;
        this.objectNameFunction = objectNameFunction;
    }

    public ObjectNameFunction<T> getObjectNameFunction()
    {
        return objectNameFunction;
    }

    public Key<Set<T>> getKey()
    {
        return (Key<Set<T>>) Key.get(setOf(clazz));
    }

    private static <K> Type setOf(Class<K> clazz)
    {
        return new TypeToken<Set<K>>() {}.where(new TypeParameter<K>() {}, TypeToken.of(clazz)).getType();
    }

}
