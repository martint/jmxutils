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

import java.util.Map;
import java.util.Set;

import org.weakref.jmx.MBeanExporter;

import com.google.inject.Inject;
import com.google.inject.Injector;

class GuiceMBeanExporter
{
    @Inject
    public GuiceMBeanExporter(Set<Mapping> mappings,
            Set<SetMapping<?>> setMappings,
            Set<MapMapping<?, ?>> mapMappings,
            MBeanExporter exporter,
            Injector injector)
    {
        export(mappings, exporter, injector);

        // cast to Object to get around Java's broken generics
        exportSets((Set<SetMapping<Object>>) (Object) setMappings, exporter, injector);
        exportMaps((Set<MapMapping<Object,Object>>) (Object) mapMappings, exporter, injector);
    }

    private <K, V> void exportMaps(Set<MapMapping<K, V>> mapMappings, MBeanExporter exporter, Injector injector)
    {
        for (MapMapping<K, V> mapping : mapMappings) {
            NamingFunction<Map.Entry<K, V>> namingFunction = mapping.getNamingFunction();

            Map<K, V> map = injector.getInstance(mapping.getKey());

            for (Map.Entry<K, V> entry : map.entrySet()) {
                String name = namingFunction.name(entry);
                exporter.export(name, entry.getValue());
            }
        }
    }

    private <T> void exportSets(Set<SetMapping<T>> setMappings, MBeanExporter exporter, Injector injector)
    {
        for (SetMapping<T> mapping : setMappings) {
            NamingFunction<T> namingFunction = mapping.getNamingFunction();

            Set<T> set = injector.getInstance(mapping.getKey());

            for (T instance : set) {
                String name = namingFunction.name(instance);
                exporter.export(name, instance);
            }
        }
    }

    private void export(Set<Mapping> mappings, MBeanExporter exporter, Injector injector)
    {
        for (Mapping mapping : mappings) {
            exporter.export(mapping.getName(), injector.getInstance(mapping.getKey()));
        }
    }
}
