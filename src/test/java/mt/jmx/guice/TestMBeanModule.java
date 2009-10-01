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
import mt.jmx.SimpleObject;
import mt.jmx.Child;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.testng.annotations.AfterTest;
import org.testng.Assert;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.MBeanServerConnection;
import javax.management.IntrospectionException;
import javax.management.InstanceNotFoundException;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.JMXConnector;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.rmi.registry.LocateRegistry;
import java.util.Collections;

public class TestMBeanModule
{
    private final int RMI_PORT = 3000;

    private JMXConnectorServer serverConnector;
    private JMXServiceURL url;
    private JMXConnector clientConnector;
    private MBeanServerConnection connection;

    @BeforeTest
    private void setup()
            throws IOException, MalformedObjectNameException
    {
        LocateRegistry.createRegistry(RMI_PORT);
        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://:" + RMI_PORT + "/jmxrmi");

        serverConnector = JMXConnectorServerFactory.newJMXConnectorServer(url, Collections.<String, Object>emptyMap(), ManagementFactory.getPlatformMBeanServer());
        serverConnector.start();

        clientConnector = JMXConnectorFactory.connect(url);
        connection = clientConnector.getMBeanServerConnection();
    }

    @AfterTest
    public void teardown()
            throws IOException
    {
        clientConnector.close();
        serverConnector.stop();
    }


    @Test
    public void testBasic()
            throws IOException, IntrospectionException, InstanceNotFoundException, ReflectionException, MalformedObjectNameException
    {
        final String name = "test:name=object";

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
                        export(SimpleObject.class).as(name);
                    }
                });

        injector.getInstance(MBeanServer.class);

        Assert.assertNotNull(connection.getMBeanInfo(new ObjectName(name)));
    }

    @Test
    public void testMultipleModules()
            throws IOException, IntrospectionException, InstanceNotFoundException, ReflectionException, MalformedObjectNameException
    {
        final String name1 = "test:name=object1";
        final String name2 = "test:name=object2";

        Injector injector = Guice.createInjector(new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind(SimpleObject.class).asEagerSingleton();
                bind(Child.class).asEagerSingleton();
                bind(MBeanServer.class).toInstance(ManagementFactory.getPlatformMBeanServer());
            }
        },
                new MBeanModule()
                {
                    @Override
                    protected void configureMBeans()
                    {
                        export(SimpleObject.class).as(name1);
                    }
                },
                new MBeanModule()
                {
                    @Override
                    protected void configureMBeans()
                    {
                        export(Child.class).as(name2);
                    }
                });

        injector.getInstance(MBeanServer.class);

        Assert.assertNotNull(connection.getMBeanInfo(new ObjectName(name1)));
        Assert.assertNotNull(connection.getMBeanInfo(new ObjectName(name2)));
    }

    @Test
    public void testAnnotation()
            throws IntrospectionException, InstanceNotFoundException, IOException, ReflectionException, MalformedObjectNameException
    {
        final String name1 = "test:name=object1";

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
                        export(SimpleObject.class).annotatedWith(TestAnnotation.class).as(name1);
                    }
                });

        injector.getInstance(MBeanServer.class);

        Assert.assertNotNull(connection.getMBeanInfo(new ObjectName(name1)));
    }

    @Test
    public void testNamedAnnotations()
            throws IOException, IntrospectionException, InstanceNotFoundException, ReflectionException, MalformedObjectNameException
    {
        final String name1 = "test:name=object1";
        final String name2 = "test:name=object2";

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
                        export(SimpleObject.class).annotatedWith(Names.named("1")).as(name1);
                        export(SimpleObject.class).annotatedWith(Names.named("2")).as(name2);
                    }
                });

        injector.getInstance(MBeanServer.class);

        Assert.assertNotNull(connection.getMBeanInfo(new ObjectName(name1)));
        Assert.assertNotNull(connection.getMBeanInfo(new ObjectName(name2)));
    }
}
