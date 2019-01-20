/**
 *  Copyright 2009 Martin Traverso
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.weakref.jmx.guice;

import com.google.inject.Inject;
import com.google.inject.Injector;
import org.weakref.jmx.MBeanExporter;
import org.weakref.jmx.ObjectNameGenerator;

import javax.management.ObjectName;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

class GuiceMBeanExporter
{
    @Inject
    public GuiceMBeanExporter(Set<Mapping> mappings,
            Set<SetMapping<?>> setMappings,
            Set<MapMapping<?, ?>> mapMappings,
            MBeanExporter exporter,
            Optional<ObjectNameGenerator> objectNameGenerator,
            Injector injector)
    {
        ObjectNameGenerator generator = objectNameGenerator.orElseGet(ObjectNameGenerator::defaultObjectNameGenerator);
        export(mappings, exporter, injector, generator);

        // cast to Object to get around Java's broken generics
        exportSets(castSetMapping(setMappings), exporter, injector, generator);
        exportMaps(castMapMappings(mapMappings), exporter, injector, generator);
    }

    @SuppressWarnings("unchecked")
    private static Set<MapMapping<Object, Object>> castMapMappings(Object mapMappings)
    {
        return (Set<MapMapping<Object,Object>>) mapMappings;
    }

    @SuppressWarnings("unchecked")
    private static Set<SetMapping<Object>> castSetMapping(Object setMappings)
    {
        return (Set<SetMapping<Object>>) setMappings;
    }

    private static <K, V> void exportMaps(Set<MapMapping<K, V>> mapMappings, MBeanExporter exporter, Injector injector, ObjectNameGenerator objectNameGenerator)
    {
        for (MapMapping<K, V> mapping : mapMappings) {
            BiFunction<ObjectNameGenerator, Entry<K, V>, ObjectName> namingFunction = mapping.getObjectNameFunction();

            Map<K, V> map = injector.getInstance(mapping.getKey());

            for (Map.Entry<K, V> entry : map.entrySet()) {
                ObjectName name = namingFunction.apply(objectNameGenerator, entry);
                exporter.export(name, entry.getValue());
            }
        }
    }

    private static <T> void exportSets(Set<SetMapping<T>> setMappings, MBeanExporter exporter, Injector injector, ObjectNameGenerator objectNameGenerator)
    {
        for (SetMapping<T> mapping : setMappings) {
            BiFunction<ObjectNameGenerator, T, ObjectName> namingFunction = mapping.getObjectNameFunction();

            Set<T> set = injector.getInstance(mapping.getKey());

            for (T instance : set) {
                ObjectName name = namingFunction.apply(objectNameGenerator, instance);
                exporter.export(name, instance);
            }
        }
    }

    private static void export(Set<Mapping> mappings, MBeanExporter exporter, Injector injector, ObjectNameGenerator objectNameGenerator)
    {
        for (Mapping mapping : mappings) {
            exporter.export(mapping.getName(objectNameGenerator), injector.getInstance(mapping.getKey()));
        }
    }
}
