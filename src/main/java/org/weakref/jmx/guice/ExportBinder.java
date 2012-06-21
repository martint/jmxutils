package org.weakref.jmx.guice;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.multibindings.Multibinder;

import static com.google.inject.multibindings.Multibinder.newSetBinder;

public class ExportBinder
{
    private final Multibinder<Mapping> binder;

    ExportBinder(Multibinder<Mapping> binder)
    {
        this.binder = binder;
    }

    public static ExportBinder newExporter(Binder binder)
    {
        return new ExportBinder(newSetBinder(binder, Mapping.class));
    }

    public AnnotatedExportBinder export(Class<?> clazz)
    {
        return new AnnotatedExportBinder(binder, Key.get(clazz));
    }

    public NamedExportBinder export(Key<?> key)
    {
        return new NamedExportBinder(binder, key);
    }
}
