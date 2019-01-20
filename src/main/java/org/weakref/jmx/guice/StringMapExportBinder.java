package org.weakref.jmx.guice;

import com.google.inject.multibindings.Multibinder;
import org.weakref.jmx.ObjectNameGenerator;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import java.util.Map.Entry;
import java.util.function.BiFunction;

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
        BiFunction<ObjectNameGenerator, Entry<String, V>, ObjectName> nameFactory = (factory, entry) -> {
            try {
                return new ObjectName(factory.generatedNameOf(valueClass, entry.getKey()));
            }
            catch (MalformedObjectNameException e) {
                throw new RuntimeException(e);
            }
        };

        as(nameFactory);
    }
}
