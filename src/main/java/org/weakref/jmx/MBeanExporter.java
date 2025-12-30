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
import com.google.common.collect.MapMaker;
import com.google.inject.Inject;
import org.weakref.jmx.JmxException.Reason;

import jakarta.annotation.PreDestroy;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.requireNonNull;

public class MBeanExporter
{
    private final MBeanServer server;
    private final Map<ObjectName, Object> exportedObjects;
    private final ObjectNameGenerator objectNameGenerator;
    private final Map<ObjectName, ManagedClass> exportedManagedClasses = new ConcurrentHashMap<>();

    public MBeanExporter(MBeanServer server)
    {
        this(server, Optional.empty());
    }

    @Inject
    public MBeanExporter(MBeanServer server, Optional<ObjectNameGenerator> objectNameGenerator)
    {
        this.server = server;
        this.objectNameGenerator = objectNameGenerator.orElseGet(ObjectNameGenerator::defaultObjectNameGenerator);
        exportedObjects = new MapMaker().weakValues().makeMap();
    }

    @PreDestroy
    public void destroy()
    {
        Map<String, Exception> errors = unexportAllAndReportMissing();
        if (!errors.isEmpty()) {
            RuntimeException exception = new RuntimeException("Failed to unexport MBeans: " + errors.keySet());
            errors.values().forEach(exception::addSuppressed);
            throw exception;
        }
    }

    public MBeanExport exportWithGeneratedName(Object object)
    {
        requireNonNull(object, "object is null");
        ObjectName objectName = createObjectName(objectNameGenerator.generatedNameOf(object.getClass()));
        export(objectName, object);
        return new MBeanExport(objectName, () -> unexport(objectName));
    }

    public MBeanExport exportWithGeneratedName(Object object, Class<?> type)
    {
        requireNonNull(object, "object is null");
        requireNonNull(type, "type is null");
        ObjectName objectName = createObjectName(objectNameGenerator.generatedNameOf(type));
        export(objectName, object);
        return new MBeanExport(objectName, () -> unexport(objectName));
    }

    public MBeanExport exportWithGeneratedName(Object object, Class<?> type, String name)
    {
        requireNonNull(object, "object is null");
        requireNonNull(type, "type is null");
        requireNonNull(name, "name is null");
        ObjectName objectName = createObjectName(objectNameGenerator.generatedNameOf(type, name));
        export(objectName, object);
        return new MBeanExport(objectName, () -> unexport(objectName));
    }

    public MBeanExport exportWithGeneratedName(Object object, Class<?> type, Map<String, String> properties)
    {
        requireNonNull(object, "object is null");
        requireNonNull(type, "type is null");
        requireNonNull(properties, "properties is null");
        ObjectName objectName = createObjectName(objectNameGenerator.generatedNameOf(type, properties));
        export(objectName, object);
        return new MBeanExport(objectName, () -> unexport(objectName));
    }

    public void export(String name, Object object)
    {
        export(createObjectName(name), object);
    }

    public void export(ObjectName objectName, Object object)
    {
        try {
            MBeanBuilder builder = new MBeanBuilder(object);
            MBean mbean = builder.build();

            synchronized(exportedObjects) {
                if(exportedObjects.containsKey(objectName)) {
                    throw new JmxException(Reason.INSTANCE_ALREADY_EXISTS, "key already exported: %s", objectName);
                }
                server.registerMBean(mbean, objectName);
                exportedObjects.put(objectName, object);
            }

            exportedManagedClasses.put(objectName, ManagedClass.fromExportedObject(object));
        }
        catch (InstanceAlreadyExistsException e) {
            throw new JmxException(Reason.INSTANCE_ALREADY_EXISTS, e.getMessage());
        }
        catch (MBeanRegistrationException e) {
            throw new JmxException(Reason.MBEAN_REGISTRATION, e.getMessage(), e.getCause());
        }
        catch (NotCompliantMBeanException e) {
            // MBeanBuilder should never construct invalid mbeans
            throw new AssertionError(e);
        }
    }

    public void unexportWithGeneratedName(Class<?> type)
    {
        requireNonNull(type, "type is null");
        unexport(objectNameGenerator.generatedNameOf(type));
    }

    public void unexportWithGeneratedName(Class<?> type, String name)
    {
        requireNonNull(type, "type is null");
        requireNonNull(name, "name is null");
        unexport(objectNameGenerator.generatedNameOf(type, name));
    }

    public void unexportWithGeneratedName(Class<?> type, Map<String, String> properties)
    {
        requireNonNull(type, "type is null");
        requireNonNull(properties, "properties is null");
        unexport(objectNameGenerator.generatedNameOf(type, properties));
    }

    public void unexport(String name)
    {
        unexport(createObjectName(name));
    }

    public void unexport(ObjectName objectName)
    {
        try {
            synchronized(exportedObjects) {
                server.unregisterMBean(objectName);
                exportedObjects.remove(objectName);
            }

            exportedManagedClasses.remove(objectName);
        }
        catch (MBeanRegistrationException e) {
            throw new JmxException(Reason.MBEAN_REGISTRATION, e.getMessage(), e.getCause());
        }
        catch (InstanceNotFoundException e) {
            throw new JmxException(Reason.INSTANCE_NOT_FOUND, e.getMessage());
        }
    }

    /**
     * @deprecated Backwards compatible to 1.11. For new code, use {@link MBeanExporter#unexportAllAndReportMissing()}.
     */
    @Deprecated
    public void unexportAll()
    {
        unexportAllAndReportMissing();
    }

    /**
     * Unexports all MBeans that have been exported through this MBeanExporter.
     *
     * @return a map of object names that could not be exported and the corresponding exception.
     */
    public Map<String, Exception> unexportAllAndReportMissing()
    {
        Map<String, Exception> errors = new HashMap<>();

        synchronized(exportedObjects) {
            List<ObjectName> toRemove = new ArrayList<>(exportedObjects.size());
            for (ObjectName objectName : exportedObjects.keySet()) {
                try {
                    server.unregisterMBean(objectName);
                    toRemove.add(objectName);
                }
                catch(InstanceNotFoundException e) {
                    // ignore ... mbean has already been unregistered elsewhere
                    toRemove.add(objectName);
                }
                catch (MBeanRegistrationException e) {
                    errors.put(objectName.toString(), e);
                }
            }

            exportedObjects.keySet().removeAll(toRemove);

            exportedManagedClasses.keySet().removeAll(toRemove);
        }

        return errors;
    }

    public Map<String, Object> getExportedObjects()
    {
        synchronized (exportedObjects) {
            ImmutableMap.Builder<String,Object> builder = ImmutableMap.builder();
            for (Entry<ObjectName, Object> entry : exportedObjects.entrySet()) {
                builder.put(entry.getKey().toString(), entry.getValue());
            }
            return builder.build();
        }
    }

    public Map<String, ManagedClass> getManagedClasses()
    {
        ImmutableMap.Builder<String, ManagedClass> builder = ImmutableMap.builder();
        for (Entry<ObjectName, ManagedClass> entry : exportedManagedClasses.entrySet()) {
            builder.put(entry.getKey().toString(), entry.getValue());
        }
        return builder.build();
    }

    public Optional<Object> getExportedObject(ObjectName objectName)
    {
        synchronized (exportedObjects) {
            return Optional.ofNullable(exportedObjects.get(objectName));
        }
    }
    
    /**
     * Get an MBeanExporter that uses the default platform mbean server
     *
     * @return an exporter
     */
    public static MBeanExporter withPlatformMBeanServer()
    {
        return new MBeanExporter(ManagementFactory.getPlatformMBeanServer());
    }

    private static ObjectName createObjectName(String name)
    {
        ObjectName objectName;
        try {
            objectName = new ObjectName(name);
        }
        catch (MalformedObjectNameException e) {
            throw new JmxException(Reason.MALFORMED_OBJECT_NAME, e.getMessage());
        }
        return objectName;
    }
}
