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

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.weakref.jmx.MBeanExporter;
import org.weakref.jmx.SimpleObject;
import org.weakref.jmx.Util;

import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import java.io.IOException;
import java.lang.management.ManagementFactory;

import static com.google.inject.Stage.PRODUCTION;
import static com.google.inject.name.Names.named;
import static org.weakref.jmx.ObjectNames.generatedNameOf;

public class TestMBeanModule
{
    @Test
    public void testExportedInDevelopmentStageToo() 
    	throws IntrospectionException, InstanceNotFoundException, ReflectionException 
    {
    	final ObjectName name = Util.getUniqueObjectName();

        Injector injector = Guice.createInjector(new MBeanModule(), new AbstractModule()
        {
            @Override
            protected void configure()
            {
                binder().requireExplicitBindings();
                binder().disableCircularProxies();

                bind(SimpleObject.class).asEagerSingleton();
                bind(MBeanServer.class).toInstance(ManagementFactory.getPlatformMBeanServer());
                ExportBinder.newExporter(binder()).export(SimpleObject.class).as(name.getCanonicalName());
            }
        });
        
        MBeanServer server = injector.getInstance(MBeanServer.class);
        Assert.assertNotNull(server.getMBeanInfo(name));
    }

    @Test
    public void testBasic()
            throws IOException, IntrospectionException, InstanceNotFoundException, ReflectionException, MalformedObjectNameException, MBeanRegistrationException
    {
        final ObjectName name = Util.getUniqueObjectName();

        Injector injector = Guice.createInjector(PRODUCTION, new MBeanModule(), new AbstractModule()
        {
            @Override
            protected void configure()
            {
                binder().requireExplicitBindings();
                binder().disableCircularProxies();

                bind(SimpleObject.class).asEagerSingleton();
                bind(MBeanServer.class).toInstance(ManagementFactory.getPlatformMBeanServer());
                ExportBinder.newExporter(binder()).export(SimpleObject.class).as(name.getCanonicalName());
            }
        });

        MBeanServer server = injector.getInstance(MBeanServer.class);

        Assert.assertNotNull(server.getMBeanInfo(name));
        server.unregisterMBean(name);
    }

    @Test
    public void testNamespace()
            throws IOException, IntrospectionException, InstanceNotFoundException, ReflectionException, MalformedObjectNameException, MBeanRegistrationException
    {
        final ObjectName name = Util.getUniqueObjectName();
        final String namespace1 = "namespace-1";
        final String namespace2 = "namespace-2";

        Injector injector = Guice.createInjector(PRODUCTION, new MBeanModule(namespace1), new AbstractModule()
        {
            @Override
            protected void configure()
            {
                binder().requireExplicitBindings();
                binder().disableCircularProxies();

                bind(SimpleObject.class).asEagerSingleton();
                bind(MBeanServer.class).toInstance(ManagementFactory.getPlatformMBeanServer());
                ExportBinder.newExporter(binder()).export(SimpleObject.class).as(name.getCanonicalName());
            }
        });

        Injector injector2 = Guice.createInjector(PRODUCTION, new MBeanModule(namespace2), new AbstractModule()
        {
            @Override
            protected void configure()
            {
                binder().requireExplicitBindings();
                binder().disableCircularProxies();

                bind(SimpleObject.class).asEagerSingleton();
                bind(MBeanServer.class).toInstance(ManagementFactory.getPlatformMBeanServer());
                ExportBinder.newExporter(binder()).export(SimpleObject.class).as(name.getCanonicalName());
            }
        });

        MBeanServer server = injector.getInstance(MBeanServer.class);

        // Same class/name exported in separate namespace
        ObjectName exportedName1 = MBeanExporter.getExportedName(namespace1, name);
        Assert.assertNotNull(server.getMBeanInfo(exportedName1));
        server.unregisterMBean(exportedName1);
        ObjectName exportedName2 = MBeanExporter.getExportedName(namespace2, name);
        Assert.assertNotNull(server.getMBeanInfo(exportedName2));
        server.unregisterMBean(exportedName2);
    }

    @Test
    public void testGeneratedNames()
            throws IOException, IntrospectionException, InstanceNotFoundException, ReflectionException, MalformedObjectNameException, MBeanRegistrationException
    {
        final ObjectName name = new ObjectName(generatedNameOf(SimpleObject.class));

        Injector injector = Guice.createInjector(PRODUCTION, new MBeanModule(), new AbstractModule()
        {
            @Override
            protected void configure()
            {
                binder().requireExplicitBindings();
                binder().disableCircularProxies();

                bind(SimpleObject.class).asEagerSingleton();
                bind(MBeanServer.class).toInstance(ManagementFactory.getPlatformMBeanServer());
                ExportBinder.newExporter(binder()).export(SimpleObject.class).withGeneratedName();
            }
        });

        MBeanServer server = injector.getInstance(MBeanServer.class);

        Assert.assertNotNull(server.getMBeanInfo(name));
        server.unregisterMBean(name);
    }

    @Test
    public void testGeneratedNameOnNamedAnnotation()
            throws MalformedObjectNameException, IntrospectionException, InstanceNotFoundException, ReflectionException,
            MBeanRegistrationException
    {
        final ObjectName name = new ObjectName(generatedNameOf(SimpleObject.class, named("hello")));

        Injector injector = Guice.createInjector(PRODUCTION, new MBeanModule(), new AbstractModule()
        {
            @Override
            protected void configure()
            {
                binder().requireExplicitBindings();
                binder().disableCircularProxies();

                bind(SimpleObject.class).annotatedWith(named("hello")).toInstance(new SimpleObject());
                bind(MBeanServer.class).toInstance(ManagementFactory.getPlatformMBeanServer());
                ExportBinder.newExporter(binder()).export(SimpleObject.class).annotatedWith(named("hello")).withGeneratedName();
            }
        });

        MBeanServer server = injector.getInstance(MBeanServer.class);

        Assert.assertNotNull(server.getMBeanInfo(name));
        server.unregisterMBean(name);
    }
    
    @Test
    public void testAnnotation()
            throws IntrospectionException, InstanceNotFoundException, IOException, ReflectionException, MalformedObjectNameException, MBeanRegistrationException
    {
        final ObjectName objectName = Util.getUniqueObjectName();

        Injector injector = Guice.createInjector(PRODUCTION, new MBeanModule(), new AbstractModule()
        {
            @Override
            protected void configure()
            {
                binder().requireExplicitBindings();
                binder().disableCircularProxies();

                bind(SimpleObject.class).annotatedWith(TestAnnotation.class).toInstance(new SimpleObject());
                bind(MBeanServer.class).toInstance(ManagementFactory.getPlatformMBeanServer());
                ExportBinder.newExporter(binder()).export(SimpleObject.class).annotatedWith(TestAnnotation.class).as(objectName.getCanonicalName());
            }
        });

        MBeanServer server = injector.getInstance(MBeanServer.class);

        Assert.assertNotNull(server.getMBeanInfo(objectName));
        server.unregisterMBean(objectName);
    }

    @Test
    public void testNamedAnnotations()
            throws IOException, IntrospectionException, InstanceNotFoundException, ReflectionException, MalformedObjectNameException, MBeanRegistrationException
    {
        final ObjectName objectName1 = Util.getUniqueObjectName();
        final ObjectName objectName2 = Util.getUniqueObjectName();

        Injector injector = Guice.createInjector(PRODUCTION, new MBeanModule(), new AbstractModule()
        {
            @Override
            protected void configure()
            {
                binder().requireExplicitBindings();
                binder().disableCircularProxies();

                bind(SimpleObject.class).annotatedWith(Names.named("1")).toInstance(new SimpleObject());
                bind(SimpleObject.class).annotatedWith(Names.named("2")).toInstance(new SimpleObject());
                bind(MBeanServer.class).toInstance(ManagementFactory.getPlatformMBeanServer());

                ExportBinder exporter = ExportBinder.newExporter(binder());
                exporter.export(SimpleObject.class).annotatedWith(Names.named("1")).as(objectName1.getCanonicalName());
                exporter.export(SimpleObject.class).annotatedWith(Names.named("2")).as(objectName2.getCanonicalName());
            }
        });

        MBeanServer server = injector.getInstance(MBeanServer.class);

        Assert.assertNotNull(server.getMBeanInfo(objectName1));
        Assert.assertNotNull(server.getMBeanInfo(objectName2));

        server.unregisterMBean(objectName1);
        server.unregisterMBean(objectName2);
    }

    @Test
    public void testExportKey()
            throws IntrospectionException, InstanceNotFoundException, ReflectionException, MBeanRegistrationException
    {
        final ObjectName objectName1 = Util.getUniqueObjectName();
        final ObjectName objectName2 = Util.getUniqueObjectName();

        Injector injector = Guice.createInjector(PRODUCTION, new MBeanModule(), new AbstractModule()
        {
            @Override
            protected void configure()
            {
                binder().requireExplicitBindings();
                binder().disableCircularProxies();

                bind(MBeanServer.class).toInstance(ManagementFactory.getPlatformMBeanServer());
                bind(SimpleObject.class).toInstance(new SimpleObject());
                bind(SimpleObject.class).annotatedWith(Names.named("1")).toInstance(new SimpleObject());

                ExportBinder exporter = ExportBinder.newExporter(binder());
                exporter.export(Key.get(SimpleObject.class))
                        .as(objectName1.getCanonicalName());
                exporter.export(Key.get(SimpleObject.class, named("1")))
                        .as(objectName2.getCanonicalName());
            }
        });

        MBeanServer server = injector.getInstance(MBeanServer.class);

        Assert.assertNotNull(server.getMBeanInfo(objectName1));
        Assert.assertNotNull(server.getMBeanInfo(objectName2));

        server.unregisterMBean(objectName1);
        server.unregisterMBean(objectName2);
    }



    @Test
    public void testSet()
            throws Exception
    {
        final ObjectName name1 = new ObjectName(generatedNameOf(SimpleObject.class, "blue"));
        final ObjectName name2 = new ObjectName(generatedNameOf(SimpleObject.class, "red"));

        Injector injector = Guice.createInjector(PRODUCTION, new MBeanModule(), new AbstractModule()
        {
            @Override
            protected void configure()
            {
                binder().requireExplicitBindings();
                binder().disableCircularProxies();

                Multibinder<SimpleObject> multibinder = Multibinder.newSetBinder(binder(), SimpleObject.class);

                SimpleObject object1 = new SimpleObject();
                object1.setStringValue("blue");
                multibinder.addBinding().toInstance(object1);

                SimpleObject object2 = new SimpleObject();
                object2.setStringValue("red");
                multibinder.addBinding().toInstance(object2);

                bind(MBeanServer.class).toInstance(ManagementFactory.getPlatformMBeanServer());
                ExportBinder.newExporter(binder()).exportSet(SimpleObject.class).withGeneratedName(new NamingFunction<SimpleObject>()
                {
                    public String name(SimpleObject object)
                    {
                        return object.getStringValue();
                    }
                });
            }
        });

        MBeanServer server = injector.getInstance(MBeanServer.class);

        Assert.assertNotNull(server.getMBeanInfo(name1));
        Assert.assertNotNull(server.getMBeanInfo(name2));

        server.unregisterMBean(name1);
        server.unregisterMBean(name2);
    }

}
