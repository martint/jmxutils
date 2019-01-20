package org.weakref.jmx.guice;

import com.google.common.base.Throwables;
import com.google.inject.multibindings.Multibinder;
import org.weakref.jmx.ObjectNames;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import java.util.Map.Entry;

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
        ObjectNameFunction<Entry<String, V>> namingFunction = entry -> {
            try {
                return new ObjectName(ObjectNames.generatedNameOf(valueClass, entry.getKey()));
            }
            catch (MalformedObjectNameException e) {
                throw Throwables.propagate(e);
            }
        };

        binder.addBinding().toInstance(new MapMapping<>(String.class, valueClass, namingFunction));
    }
}
