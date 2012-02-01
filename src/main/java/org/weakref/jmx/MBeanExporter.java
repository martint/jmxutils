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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;

public class MBeanExporter
{
    private final MBeanServer server;
    private final Set<String> exportedObjectNames = new HashSet<String>();

    MBeanExporter()
    {
        this(ManagementFactory.getPlatformMBeanServer());
    }

    @Inject
    public MBeanExporter(MBeanServer server)
    {
        this.server = server;
    }

    public void export(String name, Object object)
    {
        ObjectName objectName;
        try {
            objectName = new ObjectName(name);
            MBeanBuilder builder = new MBeanBuilder(object);
            MBean mbean = builder.build();

            synchronized(exportedObjectNames) {
                exportedObjectNames.add(name);
                server.registerMBean(mbean, objectName);
            }
        }
        catch (MalformedObjectNameException e) {
            throw new JmxException(Reason.MALFORMED_OBJECT_NAME, e.getMessage());
        }
        catch (InstanceAlreadyExistsException e) {
            throw new JmxException(JmxException.Reason.INSTANCE_ALREADY_EXISTS, e.getMessage());
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

            synchronized(exportedObjectNames) {
                server.unregisterMBean(objectName);
                exportedObjectNames.remove(name);
            }
        }
        catch (MalformedObjectNameException e) {
            throw new JmxException(Reason.MALFORMED_OBJECT_NAME, e.getMessage());
        }
        catch (MBeanRegistrationException e) {
            throw new JmxException(JmxException.Reason.MBEAN_REGISTRATION, e.getMessage(), e.getCause());
        }
        catch (InstanceNotFoundException e) {
            throw new JmxException(JmxException.Reason.INSTANCE_NOT_FOUND, e.getMessage());
        }
    }

    /**
     * Unexports all MBeans that have been exported through this MBeanExporter.
     *
     * @return a map of object names that could not be exported and the corresponding exception.
     */
    public Map<String, Exception> unexportAll()
    {
        Map<String, Exception> errors = new HashMap<String, Exception>();

        synchronized(exportedObjectNames) {
            List<String> toRemove = new ArrayList<String>(exportedObjectNames.size());
            for (String objectName : exportedObjectNames) {
                try {
                    server.unregisterMBean(new ObjectName(objectName));
                    toRemove.add(objectName);
                }
                catch(InstanceNotFoundException e) {
                    // ignore ... mbean has already been unregistered elsewhere
                    toRemove.add(objectName);
                }
                catch (MalformedObjectNameException e) {
                    throw new IllegalStateException(format("Found a malformed object name [%s]. This should never happen", objectName), e);
                }
                catch (MBeanRegistrationException e) {
                    //noinspection ThrowableResultOfMethodCallIgnored
                    errors.put(objectName, e);
                }
            }

            exportedObjectNames.removeAll(toRemove);
        }

        return errors;
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
