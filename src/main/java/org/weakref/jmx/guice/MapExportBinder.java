package org.weakref.jmx.guice;

import com.google.inject.multibindings.Multibinder;
import org.weakref.jmx.ObjectNameGenerator;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import java.util.Map.Entry;
import java.util.function.BiFunction;

public class MapExportBinder<K, V>
{
    protected final Multibinder<MapMapping<?, ?>> binder;
    protected final Class<K> keyClass;
    private final Class<V> valueClass;

    MapExportBinder(Multibinder<MapMapping<?, ?>> binder, Class<K> keyClass, Class<V> valueClass)
    {
        this.binder = binder;
        this.keyClass = keyClass;
        this.valueClass = valueClass;
    }

    public void withGeneratedName(NamingFunction<V> valueNamingFunction)
    {
        BiFunction<ObjectNameGenerator, Entry<K, V>, ObjectName> nameFactory = (factory, entry) -> {
            try {
                String itemName = valueNamingFunction.name(entry.getValue());
                return new ObjectName(factory.generatedNameOf(valueClass, itemName));
            }
            catch (MalformedObjectNameException e) {
                throw new RuntimeException(e);
            }
        };

        as(nameFactory);
    }

    public void withGeneratedName(ObjectNameFunction<V> valueNamingFunction)
    {
        as((factory, entry) -> valueNamingFunction.name(entry.getValue()));
    }

    public void withGeneratedName(MapNamingFunction<K, V> valueNamingFunction)
    {
        BiFunction<ObjectNameGenerator, Entry<K, V>, ObjectName> nameFactory = (factory, entry) -> {
            try {
                String itemName = valueNamingFunction.name(entry.getKey(), entry.getValue());
                return new ObjectName(factory.generatedNameOf(valueClass, itemName));
            }
            catch (MalformedObjectNameException e) {
                throw new RuntimeException(e);
            }
        };

        as(nameFactory);
    }

    public void withGeneratedName(MapObjectNameFunction<K, V> valueNamingFunction)
    {
        as((factory, entry) -> valueNamingFunction.name(entry.getKey(), entry.getValue()));
    }

    public void as(BiFunction<ObjectNameGenerator, Entry<K, V>, ObjectName> nameFactory)
    {
        binder.addBinding().toInstance(new MapMapping<>(keyClass, valueClass, nameFactory));
    }
}
