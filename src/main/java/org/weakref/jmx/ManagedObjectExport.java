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
package org.weakref.jmx;

import com.google.common.collect.ImmutableMap;

import javax.management.ObjectName;

import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Metadata for a managed object exported through an {@link MBeanExporter}.
 */
public final class ManagedObjectExport
{
    private final ObjectName objectName;
    private final Optional<Class<?>> exportedType;
    private final Optional<String> originalName;
    private final Map<String, String> originalProperties;
    private final ManagedClass managedClass;

    ManagedObjectExport(
            ObjectName objectName,
            Optional<Class<?>> exportedType,
            Optional<String> originalName,
            Map<String, String> originalProperties,
            ManagedClass managedClass)
    {
        this.objectName = requireNonNull(objectName, "objectName is null");
        this.exportedType = requireNonNull(exportedType, "exportedType is null");
        this.originalName = requireNonNull(originalName, "originalName is null");
        this.originalProperties = ImmutableMap.copyOf(requireNonNull(originalProperties, "originalProperties is null"));
        this.managedClass = requireNonNull(managedClass, "managedClass is null");
    }

    public ObjectName getObjectName()
    {
        return objectName;
    }

    /**
     * Original Java type supplied when the object was exported, if one was available.
     */
    public Optional<Class<?>> getExportedType()
    {
        return exportedType;
    }

    /**
     * Original name argument passed to the {@link ObjectNameGenerator}, if any.
     */
    public Optional<String> getOriginalName()
    {
        return originalName;
    }

    /**
     * Original properties map passed to the {@link ObjectNameGenerator}, if any.
     */
    public Map<String, String> getOriginalProperties()
    {
        return originalProperties;
    }

    public ManagedClass getManagedClass()
    {
        return managedClass;
    }
}
