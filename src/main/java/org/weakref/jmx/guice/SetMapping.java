package org.weakref.jmx.guice;

import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
import com.google.inject.Key;
import org.weakref.jmx.ObjectNameGenerator;

import javax.management.ObjectName;

import java.lang.reflect.Type;
import java.util.Set;
import java.util.function.BiFunction;

class SetMapping<T>
{
    private final BiFunction<ObjectNameGenerator, T, ObjectName> objectNameFunction;
    private final Class<T> clazz;

    SetMapping(Class<T> key, BiFunction<ObjectNameGenerator, T, ObjectName> objectNameFunction)
    {
        this.clazz = key;
        this.objectNameFunction = objectNameFunction;
    }

    public BiFunction<ObjectNameGenerator, T, ObjectName> getObjectNameFunction()
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
