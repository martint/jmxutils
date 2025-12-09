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
    private final Multibinder<MapMapping<?, ?>> mapBinder;

    ExportBinder(
            Multibinder<Mapping> binder,
            Multibinder<SetMapping<?>> collectionBinder,
            Multibinder<MapMapping<?, ?>> mapBinder)
    {
        this.binder = binder;
        this.collectionBinder = collectionBinder;
        this.mapBinder = mapBinder;
    }

    public static ExportBinder newExporter(Binder binder)
    {
        Multibinder<SetMapping<?>> collectionBinder = newSetBinder(binder, new TypeLiteral<>() {});
        Multibinder<MapMapping<?, ?>> mapBinder = newSetBinder(binder, new TypeLiteral<>() {});

        return new ExportBinder(newSetBinder(binder, Mapping.class), collectionBinder, mapBinder);
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
        return new SetExportBinder<>(collectionBinder, clazz);
    }

    public <V> StringMapExportBinder<V> exportMap(Class<V> valueClass)
    {
        return new StringMapExportBinder<>(mapBinder, valueClass);
    }

    public <K, V> MapExportBinder<K, V> exportMap(Class<K> keyClass, Class<V> valueClass)
    {
        return new MapExportBinder<>(mapBinder, keyClass, valueClass);
    }

}
