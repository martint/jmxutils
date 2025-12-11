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
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.weakref.jmx.ObjectNameBuilder;
import org.weakref.jmx.ObjectNameGenerator;
import org.weakref.jmx.SimpleObject;
import org.weakref.jmx.Util;

import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import java.lang.management.ManagementFactory;
import java.util.Map;

import static com.google.inject.Stage.PRODUCTION;
import static com.google.inject.name.Names.named;
import static org.weakref.jmx.ObjectNames.generatedNameOf;

public class TestMBeanModule
{
    @Test
    public void testExportedInDevelopmentStageToo() 
    	throws Exception
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
            throws Exception
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
    public void testGeneratedNames()
            throws Exception
    {
        assertGeneratedNames(new ObjectName(generatedNameOf(SimpleObject.class)), binder -> {});
    }

    @Test
    public void testCustomGeneratedNames()
            throws Exception
    {
        assertGeneratedNames(
                new ObjectName("test:name=" + SimpleObject.class.getSimpleName()),
                binder -> binder.bind(ObjectNameGenerator.class).to(TestObjectNameGenerator.class));
    }

    private static void assertGeneratedNames(ObjectName name, Module additionalBindings)
            throws Exception
    {
        Injector injector = Guice.createInjector(PRODUCTION, new MBeanModule(), additionalBindings, new AbstractModule()
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
            throws Exception
    {
        assertGeneratedNameOnNamedAnnotation(new ObjectName(generatedNameOf(SimpleObject.class, named("hello"))), binder -> {});
    }

    @Test
    public void testCustomObjectNameGeneratorWithAnnotation()
            throws Exception
    {
        assertGeneratedNameOnNamedAnnotation(
                new ObjectName("test:name=hello,type=" + SimpleObject.class.getSimpleName()),
                binder -> binder.bind(ObjectNameGenerator.class).to(TestObjectNameGenerator.class));
    }

    private static void assertGeneratedNameOnNamedAnnotation(ObjectName name, Module additionalBindings)
            throws InstanceNotFoundException, IntrospectionException, ReflectionException, MBeanRegistrationException
    {
        Injector injector = Guice.createInjector(PRODUCTION, new MBeanModule(), additionalBindings, new AbstractModule()
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
            throws Exception
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
            throws Exception
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

                bind(SimpleObject.class).annotatedWith(named("1")).toInstance(new SimpleObject());
                bind(SimpleObject.class).annotatedWith(named("2")).toInstance(new SimpleObject());
                bind(MBeanServer.class).toInstance(ManagementFactory.getPlatformMBeanServer());

                ExportBinder exporter = ExportBinder.newExporter(binder());
                exporter.export(SimpleObject.class).annotatedWith(named("1")).as(objectName1.getCanonicalName());
                exporter.export(SimpleObject.class).annotatedWith(named("2")).as(objectName2.getCanonicalName());
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
            throws Exception
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
                bind(SimpleObject.class).annotatedWith(named("1")).toInstance(new SimpleObject());

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
        assertSet(
                new ObjectName(generatedNameOf(SimpleObject.class, "blue")),
                new ObjectName(generatedNameOf(SimpleObject.class, "red")),
                binder -> {});
    }

    @Test
    public void testCustomSet()
            throws Exception
    {
        final ObjectName name1 = new ObjectName("test:name=blue,type=" + SimpleObject.class.getSimpleName());
        final ObjectName name2 = new ObjectName("test:name=red,type=" + SimpleObject.class.getSimpleName());;

        assertSet(
                name1,
                name2,
                binder -> binder.bind(ObjectNameGenerator.class).to(TestObjectNameGenerator.class));
    }

    @Test
    public void testCustomNaming()
            throws Exception
    {
        Injector injector = Guice.createInjector(PRODUCTION, new MBeanModule(), new AbstractModule()
        {
            @Override
            protected void configure()
            {
                binder().requireExplicitBindings();
                binder().disableCircularProxies();

                bind(SimpleObject.class).annotatedWith(named("1")).toInstance(new SimpleObject());
                bind(SimpleObject.class).annotatedWith(named("2")).toInstance(new SimpleObject());
                bind(SimpleObject.class).annotatedWith(named("3")).toInstance(new SimpleObject());

                bind(MBeanServer.class).toInstance(ManagementFactory.getPlatformMBeanServer());
                ExportBinder.newExporter(binder()).export(Key.get(SimpleObject.class, named("1")))
                        .as(generator -> generator.generatedNameOf("org.example", "LegacyObject"));

                ExportBinder.newExporter(binder()).export(Key.get(SimpleObject.class, named("2")))
                        .as(generator -> generator.generatedNameOf(SimpleObject.class));

                ExportBinder.newExporter(binder()).export(Key.get(SimpleObject.class, named("3")))
                        .as(generator -> generator.generatedNameOf(TestMBeanModule.class.getPackage(), "AnotherObject"));
            }
        });

        ObjectName name1 = new ObjectName("org.example", "name", "LegacyObject");
        ObjectName name2 = new ObjectName(generatedNameOf(SimpleObject.class));
        ObjectName name3 = new ObjectName("org.weakref.jmx.guice", "name", "AnotherObject");

        MBeanServer server = injector.getInstance(MBeanServer.class);
        Assert.assertNotNull(server.getMBeanInfo(name1));
        Assert.assertNotNull(server.getMBeanInfo(name2));
        Assert.assertNotNull(server.getMBeanInfo(name3));

        server.unregisterMBean(name1);
        server.unregisterMBean(name2);
        server.unregisterMBean(name3);
    }

    private static void assertSet(ObjectName name1, ObjectName name2, Module additionalBindings)
            throws InstanceNotFoundException, IntrospectionException, ReflectionException, MBeanRegistrationException
    {
        Injector injector = Guice.createInjector(PRODUCTION, new MBeanModule(), additionalBindings, new AbstractModule()
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
                ExportBinder.newExporter(binder()).exportSet(SimpleObject.class).withGeneratedName(SimpleObject::getStringValue);
            }
        });

        MBeanServer server = injector.getInstance(MBeanServer.class);

        Assert.assertNotNull(server.getMBeanInfo(name1));
        Assert.assertNotNull(server.getMBeanInfo(name2));

        server.unregisterMBean(name1);
        server.unregisterMBean(name2);
    }

    public static final class TestObjectNameGenerator
            implements ObjectNameGenerator
    {
        @Override
        public String generatedNameOf(String packageName, Map<String, String> properties)
        {
            return new ObjectNameBuilder("test")
                    .withProperties(properties)
                    .build();
        }
    }
}
