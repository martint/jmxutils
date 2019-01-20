package org.weakref.jmx.guice;

import com.google.inject.multibindings.Multibinder;
import org.weakref.jmx.ObjectNameGenerator;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import java.util.function.BiFunction;

public class SetExportBinder<T>
{
    private final Multibinder<SetMapping<?>> binder;
    private final Class<T> clazz;

    SetExportBinder(Multibinder<SetMapping<?>> binder, Class<T> clazz)
    {
        this.binder = binder;
        this.clazz = clazz;
    }

    public void withGeneratedName(final NamingFunction<T> itemNamingFunction)
    {
        BiFunction<ObjectNameGenerator, T, ObjectName> nameFactory = (factory, object) -> {
            try {
                String itemName = itemNamingFunction.name(object);
                return new ObjectName(factory.generatedNameOf(clazz, itemName));
            }
            catch (MalformedObjectNameException e) {
                throw new RuntimeException(e);
            }
        };

        as(nameFactory);
    }

    public void withGeneratedName(final ObjectNameFunction<T> itemNamingFunction)
    {
        as((factory, object) -> itemNamingFunction.name(object));
    }

    public void as(BiFunction<ObjectNameGenerator, T, ObjectName> nameFactory)
    {
        binder.addBinding().toInstance(new SetMapping<>(clazz, nameFactory));
    }
}
