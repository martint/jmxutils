package org.weakref.jmx.guice;

import com.google.inject.Key;
import org.weakref.jmx.MBeanExporter;
import org.weakref.jmx.ObjectNameGenerator;

import javax.management.ObjectName;

import java.util.Set;
import java.util.function.BiFunction;

import static com.google.inject.util.Types.setOf;

class SetMapping<T>
{
    private final Class<T> clazz;
    private final ExportAction<T> exportAction;

    SetMapping(Class<T> key, BiFunction<ObjectNameGenerator, T, ObjectName> objectNameFunction)
    {
        this(key, (exporter, objectNameGenerator, object) -> exporter.export(objectNameFunction.apply(objectNameGenerator, object), object, key));
    }

    private SetMapping(Class<T> key, ExportAction<T> exportAction)
    {
        this.clazz = key;
        this.exportAction = exportAction;
    }

    public static <T> SetMapping<T> generatedName(Class<T> key, NamingFunction<T> namingFunction)
    {
        return new SetMapping<>(key, (exporter, objectNameGenerator, object) -> {
            String name = namingFunction.name(object);
            ObjectName objectName = Mapping.createObjectName(objectNameGenerator.generatedNameOf(key, name));
            exporter.exportWithGeneratedName(objectName, object, key, name);
        });
    }

    @SuppressWarnings("unchecked")
    public Key<Set<T>> getKey()
    {
        return (Key<Set<T>>) Key.get(setOf(clazz));
    }

    public void export(MBeanExporter exporter, ObjectNameGenerator objectNameGenerator, T object)
    {
        exportAction.export(exporter, objectNameGenerator, object);
    }

    private interface ExportAction<T>
    {
        void export(MBeanExporter exporter, ObjectNameGenerator objectNameGenerator, T object);
    }
}
