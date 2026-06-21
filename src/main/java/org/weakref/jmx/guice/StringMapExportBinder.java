package org.weakref.jmx.guice;

import com.google.inject.multibindings.Multibinder;

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
        binder.addBinding().toInstance(MapMapping.generatedName(keyClass, valueClass, (key, value) -> key));
    }
}
