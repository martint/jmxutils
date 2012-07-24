package org.weakref.jmx.guice;

import com.google.common.base.Throwables;
import com.google.inject.multibindings.Multibinder;
import org.weakref.jmx.ObjectNames;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

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
        ObjectNameFunction<T> objectNameFunction = new ObjectNameFunction<T>()
        {
            public ObjectName name(T object)
            {
                try {
                    String itemName = itemNamingFunction.name(object);
                    return new ObjectName(ObjectNames.generatedNameOf(clazz, itemName));
                }
                catch (MalformedObjectNameException e) {
                    throw Throwables.propagate(e);
                }
            }
        };

        binder.addBinding().toInstance(new SetMapping<T>(clazz, objectNameFunction));
    }

    public void withGeneratedName(final ObjectNameFunction<T> itemNamingFunction)
    {
        ObjectNameFunction<T> objectNameFunction = new ObjectNameFunction<T>()
        {
            public ObjectName name(T object)
            {
                return itemNamingFunction.name(object);
            }
        };

        binder.addBinding().toInstance(new SetMapping<T>(clazz, objectNameFunction));
    }
}
