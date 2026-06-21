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

import com.google.inject.Key;
import org.weakref.jmx.MBeanExporter;
import org.weakref.jmx.ObjectNameGenerator;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import java.util.Optional;
import java.util.function.Function;

class Mapping
{
    private final Key<?> key;
    private final ExportAction exportAction;

    Mapping(Function<ObjectNameGenerator, String> nameFactory, Key<?> key)
    {
        this(key, (exporter, objectNameGenerator, object) -> exporter.export(nameFactory.apply(objectNameGenerator), object, key.getTypeLiteral().getRawType()));
    }

    private Mapping(Key<?> key, ExportAction exportAction)
    {
        this.key = key;
        this.exportAction = exportAction;
    }

    public static Mapping generatedName(Key<?> key, Optional<String> generatedName)
    {
        return new Mapping(key, (exporter, objectNameGenerator, object) -> {
            Class<?> type = key.getTypeLiteral().getRawType();
            if (generatedName.isPresent()) {
                String name = generatedName.get();
                ObjectName objectName = createObjectName(objectNameGenerator.generatedNameOf(type, name));
                exporter.exportWithGeneratedName(objectName, object, type, name);
            }
            else {
                ObjectName objectName = createObjectName(objectNameGenerator.generatedNameOf(type));
                exporter.exportWithGeneratedName(objectName, object, type);
            }
        });
    }

    public Key<?> getKey()
    {
        return key;
    }

    public void export(MBeanExporter exporter, ObjectNameGenerator objectNameGenerator, Object object)
    {
        exportAction.export(exporter, objectNameGenerator, object);
    }

    private interface ExportAction
    {
        void export(MBeanExporter exporter, ObjectNameGenerator objectNameGenerator, Object object);
    }

    static ObjectName createObjectName(String name)
    {
        try {
            return new ObjectName(name);
        }
        catch (MalformedObjectNameException e) {
            throw new RuntimeException(e);
        }
    }
}
