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
package mt.jmx.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import com.google.inject.name.Named;
import mt.jmx.SimpleObject;
import mt.jmx.Util;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import java.io.IOException;
import java.lang.management.ManagementFactory;

public class TestMBeanModule
{
    private MBeanServer server;

    @BeforeTest
    private void setup()
            throws IOException, MalformedObjectNameException
    {
        server = ManagementFactory.getPlatformMBeanServer();
    }

    @Test
    public void testBasic()
            throws IOException, IntrospectionException, InstanceNotFoundException, ReflectionException, MalformedObjectNameException, MBeanRegistrationException
    {
        final ObjectName name = Util.getUniqueObjectName();

        Injector injector = Guice.createInjector(new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind(SimpleObject.class).asEagerSingleton();
                bind(MBeanServer.class).toInstance(ManagementFactory.getPlatformMBeanServer());
            }
        },
                new MBeanModule()
                {
                    @Override
                    protected void configureMBeans()
                    {
                        export(SimpleObject.class).as(name.getCanonicalName());
                    }
                });

        injector.getInstance(MBeanServer.class);

        Assert.assertNotNull(server.getMBeanInfo(name));
        server.unregisterMBean(name);
    }

    @Test
    public void testMultipleModules()
            throws IOException, IntrospectionException, InstanceNotFoundException, ReflectionException, MalformedObjectNameException, MBeanRegistrationException
    {
        final ObjectName objectName1 = Util.getUniqueObjectName();
        final ObjectName objectName2 = Util.getUniqueObjectName();

        Injector injector = Guice.createInjector(new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind(SimpleObject.class).annotatedWith(Names.named("1")).toInstance(new SimpleObject());
                bind(SimpleObject.class).annotatedWith(Names.named("2")).toInstance(new SimpleObject());
                bind(MBeanServer.class).toInstance(ManagementFactory.getPlatformMBeanServer());
            }
        },
                new MBeanModule()
                {
                    @Override
                    protected void configureMBeans()
                    {
                        export(SimpleObject.class).annotatedWith(Names.named("1")).as(objectName1.getCanonicalName());
                    }
                },
                new MBeanModule()
                {
                    @Override
                    protected void configureMBeans()
                    {
                        export(SimpleObject.class).annotatedWith(Names.named("2")).as(objectName2.getCanonicalName());
                    }
                });

        injector.getInstance(MBeanServer.class);

        Assert.assertNotNull(server.getMBeanInfo(objectName1));
        Assert.assertNotNull(server.getMBeanInfo(objectName2));

        server.unregisterMBean(objectName1);
        server.unregisterMBean(objectName2);
    }

    @Test
    public void testAnnotation()
            throws IntrospectionException, InstanceNotFoundException, IOException, ReflectionException, MalformedObjectNameException, MBeanRegistrationException
    {
        final ObjectName objectName = Util.getUniqueObjectName();

        Injector injector = Guice.createInjector(new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind(SimpleObject.class).annotatedWith(TestAnnotation.class).toInstance(new SimpleObject());
                bind(MBeanServer.class).toInstance(ManagementFactory.getPlatformMBeanServer());
            }
        },
                new MBeanModule()
                {
                    @Override
                    protected void configureMBeans()
                    {
                        export(SimpleObject.class).annotatedWith(TestAnnotation.class).as(objectName.getCanonicalName());
                    }
                });

        injector.getInstance(MBeanServer.class);

        Assert.assertNotNull(server.getMBeanInfo(objectName));
        server.unregisterMBean(objectName);
    }

    @Test
    public void testNamedAnnotations()
            throws IOException, IntrospectionException, InstanceNotFoundException, ReflectionException, MalformedObjectNameException, MBeanRegistrationException
    {
        final ObjectName objectName1 = Util.getUniqueObjectName();
        final ObjectName objectName2 = Util.getUniqueObjectName();

        Injector injector = Guice.createInjector(new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind(SimpleObject.class).annotatedWith(Names.named("1")).toInstance(new SimpleObject());
                bind(SimpleObject.class).annotatedWith(Names.named("2")).toInstance(new SimpleObject());
                bind(MBeanServer.class).toInstance(ManagementFactory.getPlatformMBeanServer());
            }
        },
                new MBeanModule()
                {
                    @Override
                    protected void configureMBeans()
                    {
                        export(SimpleObject.class).annotatedWith(Names.named("1")).as(objectName1.getCanonicalName());
                        export(SimpleObject.class).annotatedWith(Names.named("2")).as(objectName2.getCanonicalName());
                    }
                });

        injector.getInstance(MBeanServer.class);

        Assert.assertNotNull(server.getMBeanInfo(objectName1));
        Assert.assertNotNull(server.getMBeanInfo(objectName2));

        server.unregisterMBean(objectName1);
        server.unregisterMBean(objectName2);
    }
}
