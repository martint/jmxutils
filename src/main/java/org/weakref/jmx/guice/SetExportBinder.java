package org.weakref.jmx.guice;

import com.google.inject.multibindings.Multibinder;
import org.weakref.jmx.ObjectNames;

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
        NamingFunction<T> namingFunction = new NamingFunction<T>()
        {
            public String name(T object)
            {
                String itemName = itemNamingFunction.name(object);
                return ObjectNames.generatedNameOf(clazz, itemName);
            }
        };

        binder.addBinding().toInstance(new SetMapping<T>(clazz, namingFunction));
    }
}
