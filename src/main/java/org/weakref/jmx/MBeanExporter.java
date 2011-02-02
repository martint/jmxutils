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

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.weakref.jmx.JmxException.JmxCause;

import java.lang.management.ManagementFactory;

public class MBeanExporter
{
    private final MBeanServer server;

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

            server.registerMBean(mbean, objectName);
        }
        catch (MalformedObjectNameException mone) {
            throw new JmxException(JmxCause.MALFORMED_OBJECT_NAME, mone.getMessage());
        }
        catch (InstanceAlreadyExistsException iae) {
            throw new JmxException(JmxCause.INSTANCE_ALREADY_EXISTS, iae.getMessage());
        }
        catch (MBeanRegistrationException mre) {
            throw new JmxException(JmxCause.MBEAN_REGISTRATION, mre.getMessage(), mre.getCause());
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

            server.unregisterMBean(objectName);
        }
        catch (MalformedObjectNameException mone) {
            throw new JmxException(JmxCause.MALFORMED_OBJECT_NAME, mone.getMessage());
        }
        catch (MBeanRegistrationException mre) {
            throw new JmxException(JmxCause.MBEAN_REGISTRATION, mre.getMessage(), mre.getCause());
        }
        catch (InstanceNotFoundException infe) {
            throw new JmxException(JmxCause.INSTANCE_NOT_FOUND, infe.getMessage());
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
