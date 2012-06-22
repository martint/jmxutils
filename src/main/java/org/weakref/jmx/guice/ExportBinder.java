package org.weakref.jmx.guice;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;

import static com.google.inject.multibindings.Multibinder.newSetBinder;

public class ExportBinder
{
    private final Multibinder<Mapping> binder;
    private final Multibinder<SetMapping<?>> collectionBinder;

    ExportBinder(Multibinder<Mapping> binder, Multibinder<SetMapping<?>> collectionBinder)
    {
        this.binder = binder;
        this.collectionBinder = collectionBinder;
    }

    public static ExportBinder newExporter(Binder binder)
    {
        Multibinder<SetMapping<?>> collectionBinder = newSetBinder(binder, new TypeLiteral<SetMapping<?>>() {});

        return new ExportBinder(newSetBinder(binder, Mapping.class), collectionBinder);
    }

    public AnnotatedExportBinder export(Class<?> clazz)
    {
        return new AnnotatedExportBinder(binder, Key.get(clazz));
    }

    public NamedExportBinder export(Key<?> key)
    {
        return new NamedExportBinder(binder, key);
    }

    public <T> SetExportBinder<T> exportSet(Class<T> clazz)
    {
        return new SetExportBinder<T>(collectionBinder, clazz);
    }
}
