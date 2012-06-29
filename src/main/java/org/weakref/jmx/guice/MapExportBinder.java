package org.weakref.jmx.guice;

import com.google.inject.multibindings.Multibinder;
import org.weakref.jmx.ObjectNames;

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
        NamingFunction<Map.Entry<K, V>> entryNamingFunction = new NamingFunction<Map.Entry<K, V>>()
        {
            public String name(Map.Entry<K, V> entry)
            {
                String itemName = valueNamingFunction.name(entry.getValue());
                return ObjectNames.generatedNameOf(valueClass, itemName);
            }
        };

        binder.addBinding().toInstance(new MapMapping<K, V>(keyClass, valueClass, entryNamingFunction));
    }
}
