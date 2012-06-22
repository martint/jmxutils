package org.weakref.jmx.guice;

import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
import com.google.inject.Key;

import java.lang.reflect.Type;
import java.util.Set;

class SetMapping<T>
{
    private final NamingFunction<T> namingFunction;
    private final Class<T> clazz;

    SetMapping(Class<T> key, NamingFunction<T> namingFunction)
    {
        this.clazz = key;
        this.namingFunction = namingFunction;
    }

    public NamingFunction<T> getNamingFunction()
    {
        return namingFunction;
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
