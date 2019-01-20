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

public class MBeanExporter
{
    private final MBeanServer server;
    private final Map<ObjectName, Object> exportedObjects;

    MBeanExporter()
    {
        this(ManagementFactory.getPlatformMBeanServer());
    }

    @Inject
    public MBeanExporter(MBeanServer server)
    {
        this.server = server;
        exportedObjects = new MapMaker().weakValues().makeMap();
    }

    public void export(String name, Object object)
    {
        ObjectName objectName;
        try {
            objectName = new ObjectName(name);
        }
        catch (MalformedObjectNameException e) {
            throw new JmxException(Reason.MALFORMED_OBJECT_NAME, e.getMessage());
        }

        export(objectName, object);
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

    public void unexport(String name)
    {
        ObjectName objectName;

        try {
            objectName = new ObjectName(name);
        }
        catch (MalformedObjectNameException e) {
            throw new JmxException(Reason.MALFORMED_OBJECT_NAME, e.getMessage());
        }

        unexport(objectName);
    }

    public void unexport(ObjectName objectName)
    {
        try {
            synchronized(exportedObjects) {
                server.unregisterMBean(objectName);
                exportedObjects.remove(objectName);
            }
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
                    //noinspection ThrowableResultOfMethodCallIgnored
                    errors.put(objectName.toString(), e);
                }
            }

            exportedObjects.keySet().removeAll(toRemove);
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
    
    /**
     * Get an MBeanExporter that uses the default platform mbean server
     *
     * @return an exporter
     */
    public static MBeanExporter withPlatformMBeanServer()
    {
        return new MBeanExporter(ManagementFactory.getPlatformMBeanServer());
    }
}
