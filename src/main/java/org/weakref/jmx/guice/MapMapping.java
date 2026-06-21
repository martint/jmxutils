package org.weakref.jmx.guice;

import com.google.inject.Key;
import org.weakref.jmx.MBeanExporter;
import org.weakref.jmx.ObjectNameGenerator;

import javax.management.ObjectName;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;

import static com.google.inject.util.Types.mapOf;

class MapMapping<K, V>
{
    private final Class<K> keyClass;
    private final Class<V> valueClass;
    private final ExportAction<K, V> exportAction;

    MapMapping(Class<K> keyClass, Class<V> valueClass, BiFunction<ObjectNameGenerator, Entry<K, V>, ObjectName> objectNameFunction)
    {
        this(keyClass, valueClass, (exporter, objectNameGenerator, entry) -> exporter.export(objectNameFunction.apply(objectNameGenerator, entry), entry.getValue(), valueClass));
    }

    private MapMapping(Class<K> keyClass, Class<V> valueClass, ExportAction<K, V> exportAction)
    {
        this.keyClass = keyClass;
        this.valueClass = valueClass;
        this.exportAction = exportAction;
    }

    public static <K, V> MapMapping<K, V> generatedName(Class<K> keyClass, Class<V> valueClass, MapNamingFunction<K, V> namingFunction)
    {
        return new MapMapping<>(keyClass, valueClass, (exporter, objectNameGenerator, entry) -> {
            String name = namingFunction.name(entry.getKey(), entry.getValue());
            ObjectName objectName = Mapping.createObjectName(objectNameGenerator.generatedNameOf(valueClass, name));
            exporter.exportWithGeneratedName(objectName, entry.getValue(), valueClass, name);
        });
    }

    @SuppressWarnings("unchecked")
    public Key<Map<K, V>> getKey()
    {
        return (Key<Map<K, V>>) Key.get(mapOf(keyClass, valueClass));
    }

    public void export(MBeanExporter exporter, ObjectNameGenerator objectNameGenerator, Entry<K, V> entry)
    {
        exportAction.export(exporter, objectNameGenerator, entry);
    }

    private interface ExportAction<K, V>
    {
        void export(MBeanExporter exporter, ObjectNameGenerator objectNameGenerator, Entry<K, V> entry);
    }
}
