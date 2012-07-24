package org.weakref.jmx.guice;

import com.google.common.base.Throwables;
import com.google.inject.multibindings.Multibinder;
import org.weakref.jmx.ObjectNames;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.util.Map;

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

    public void withGeneratedName(final NamingFunction<V> valueNamingFunction)
    {
        ObjectNameFunction<Map.Entry<K, V>> objectNameFunction = new ObjectNameFunction<Map.Entry<K, V>>()
        {
            public ObjectName name(Map.Entry<K, V> entry)
            {
                try {
                    String itemName = valueNamingFunction.name(entry.getValue());
                    return new ObjectName(ObjectNames.generatedNameOf(valueClass, itemName));
                }
                catch (MalformedObjectNameException e) {
                    throw Throwables.propagate(e);
                }
            }
        };

        binder.addBinding().toInstance(new MapMapping<K, V>(keyClass, valueClass, objectNameFunction));
    }

    public void withGeneratedName(final ObjectNameFunction<V> valueNamingFunction)
    {
        ObjectNameFunction<Map.Entry<K, V>> objectNameFunction = new ObjectNameFunction<Map.Entry<K, V>>()
        {
            public ObjectName name(Map.Entry<K, V> entry)
            {
                return valueNamingFunction.name(entry.getValue());
            }
        };

        binder.addBinding().toInstance(new MapMapping<K, V>(keyClass, valueClass, objectNameFunction));
    }

    public void withGeneratedName(final MapNamingFunction<K, V> valueNamingFunction)
    {
        ObjectNameFunction<Map.Entry<K, V>> objectNameFunction = new ObjectNameFunction<Map.Entry<K, V>>()
        {
            public ObjectName name(Map.Entry<K, V> entry)
            {
                try {
                    String itemName = valueNamingFunction.name(entry.getKey(), entry.getValue());
                    return new ObjectName(ObjectNames.generatedNameOf(valueClass, itemName));
                }
                catch (MalformedObjectNameException e) {
                    throw Throwables.propagate(e);
                }
            }
        };

        binder.addBinding().toInstance(new MapMapping<K, V>(keyClass, valueClass, objectNameFunction));
    }

    public void withGeneratedName(final MapObjectNameFunction<K, V> valueNamingFunction)
    {
        ObjectNameFunction<Map.Entry<K, V>> objectNameFunction = new ObjectNameFunction<Map.Entry<K, V>>()
        {
            public ObjectName name(Map.Entry<K, V> entry)
            {
                return valueNamingFunction.name(entry.getKey(), entry.getValue());
            }
        };

        binder.addBinding().toInstance(new MapMapping<K, V>(keyClass, valueClass, objectNameFunction));
    }
}
