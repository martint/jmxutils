package org.weakref.jmx.guice;

import com.google.inject.multibindings.Multibinder;
import org.weakref.jmx.ObjectNames;

import java.util.Map;

public class StringMapExportBinder<V>
    extends MapExportBinder<String, V>
{
    private final Class<V> valueClass;

    StringMapExportBinder(Multibinder<MapMapping<?, ?>> binder, Class<V> valueClass)
    {
        super(binder, String.class, valueClass);
        this.valueClass = valueClass;
    }

    public void withGeneratedName()
    {
        NamingFunction<Map.Entry<String, V>> namingFunction = new NamingFunction<Map.Entry<String, V>>()
        {
            public String name(Map.Entry<String, V> entry)
            {
                return ObjectNames.generatedNameOf(valueClass, entry.getKey());
            }
        };

        binder.addBinding().toInstance(new MapMapping<String, V>(String.class, valueClass, namingFunction));
    }
}
