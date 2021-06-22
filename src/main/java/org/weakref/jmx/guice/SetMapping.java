package org.weakref.jmx.guice;

import com.google.inject.Key;
import org.weakref.jmx.ObjectNameGenerator;

import javax.management.ObjectName;

import java.util.Set;
import java.util.function.BiFunction;

import static com.google.inject.util.Types.setOf;

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

    @SuppressWarnings("unchecked")
    public Key<Set<T>> getKey()
    {
        return (Key<Set<T>>) Key.get(setOf(clazz));
    }
}
