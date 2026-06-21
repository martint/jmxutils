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
import com.google.inject.util.Modules;
import org.junit.jupiter.api.Test;
import org.weakref.jmx.ManagedObjectExport;
import org.weakref.jmx.MBeanExporter;
import org.weakref.jmx.ObjectNameBuilder;
import org.weakref.jmx.ObjectNameGenerator;
import org.weakref.jmx.SimpleObject;
import org.weakref.jmx.Util;
import org.weakref.jmx.testing.TestingMBeanServer;

import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.Optional;

import static com.google.inject.Stage.PRODUCTION;
import static com.google.inject.name.Names.named;
import static com.google.inject.multibindings.OptionalBinder.newOptionalBinder;
import static org.assertj.core.api.Assertions.assertThat;
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
        assertThat(server.getMBeanInfo(name)).isNotNull();
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

        assertThat(server.getMBeanInfo(name)).isNotNull();
        ManagedObjectExport managedObjectExport = injector.getInstance(MBeanExporter.class).getManagedObjectExports().get(name);
        assertThat(managedObjectExport.getObjectName()).isEqualTo(name);
        assertThat(managedObjectExport.getExportedType()).contains(SimpleObject.class);
        assertThat(managedObjectExport.getOriginalName()).isEmpty();
        assertThat(managedObjectExport.getOriginalProperties()).isEmpty();
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

        assertThat(server.getMBeanInfo(name)).isNotNull();
        ManagedObjectExport managedObjectExport = injector.getInstance(MBeanExporter.class).getManagedObjectExports().get(name);
        assertThat(managedObjectExport.getObjectName()).isEqualTo(name);
        assertThat(managedObjectExport.getExportedType()).contains(SimpleObject.class);
        assertThat(managedObjectExport.getOriginalName()).isEmpty();
        assertThat(managedObjectExport.getOriginalProperties()).isEmpty();
        server.unregisterMBean(name);
    }

    @Test
    public void testGeneratedNameOnAnnotationClassMetadata()
            throws Exception
    {
        ObjectName name = new ObjectName(generatedNameOf(SimpleObject.class, TestAnnotation.class));

        Injector injector = Guice.createInjector(PRODUCTION, new MBeanModule(), new AbstractModule()
        {
            @Override
            protected void configure()
            {
                binder().requireExplicitBindings();
                binder().disableCircularProxies();

                bind(SimpleObject.class).annotatedWith(TestAnnotation.class).toInstance(new SimpleObject());
                bind(MBeanServer.class).toInstance(ManagementFactory.getPlatformMBeanServer());
                ExportBinder.newExporter(binder()).export(SimpleObject.class).annotatedWith(TestAnnotation.class).withGeneratedName();
            }
        });

        MBeanServer server = injector.getInstance(MBeanServer.class);

        assertThat(server.getMBeanInfo(name)).isNotNull();
        ManagedObjectExport managedObjectExport = injector.getInstance(MBeanExporter.class).getManagedObjectExports().get(name);
        assertThat(managedObjectExport.getObjectName()).isEqualTo(name);
        assertThat(managedObjectExport.getExportedType()).contains(SimpleObject.class);
        assertThat(managedObjectExport.getOriginalName()).contains(TestAnnotation.class.getSimpleName());
        assertThat(managedObjectExport.getOriginalProperties()).isEmpty();
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

        assertThat(server.getMBeanInfo(name)).isNotNull();
        ManagedObjectExport managedObjectExport = injector.getInstance(MBeanExporter.class).getManagedObjectExports().get(name);
        assertThat(managedObjectExport.getObjectName()).isEqualTo(name);
        assertThat(managedObjectExport.getExportedType()).contains(SimpleObject.class);
        assertThat(managedObjectExport.getOriginalName()).contains("hello");
        assertThat(managedObjectExport.getOriginalProperties()).isEmpty();
        server.unregisterMBean(name);
    }

    @Test
    public void testGeneratedNameUsesConfiguredGenerator()
            throws Exception
    {
        MBeanServer server = new TestingMBeanServer();
        ObjectNameGenerator configuredGenerator = new DomainObjectNameGenerator("from.binding");
        ObjectNameGenerator exporterGenerator = new DomainObjectNameGenerator("from.exporter");

        Injector injector = Guice.createInjector(PRODUCTION,
                Modules.override(new MBeanModule()).with(new AbstractModule()
                {
                    @Override
                    protected void configure()
                    {
                        binder().requireExplicitBindings();
                        binder().disableCircularProxies();

                        bind(MBeanServer.class).toInstance(server);
                        newOptionalBinder(binder(), ObjectNameGenerator.class).setBinding().toInstance(configuredGenerator);
                        bind(MBeanExporter.class).toInstance(new MBeanExporter(server, Optional.of(exporterGenerator)));
                        bind(SimpleObject.class).annotatedWith(named("generated")).toInstance(new SimpleObject());
                        bind(SimpleObject.class).annotatedWith(named("custom")).toInstance(new SimpleObject());

                        ExportBinder exporter = ExportBinder.newExporter(binder());
                        exporter.export(SimpleObject.class).annotatedWith(named("generated")).withGeneratedName();
                        exporter.export(SimpleObject.class).annotatedWith(named("custom")).as(generator -> generator.generatedNameOf(SimpleObject.class, "custom"));
                    }
                }));

        ObjectName generatedName = new ObjectName("from.binding:type=SimpleObject,name=generated");
        ObjectName customName = new ObjectName("from.binding:type=SimpleObject,name=custom");
        ObjectName generatedNameFromExporter = new ObjectName("from.exporter:type=SimpleObject,name=generated");

        Map<ObjectName, ManagedObjectExport> exports = injector.getInstance(MBeanExporter.class).getManagedObjectExports();
        assertThat(exports).containsKeys(generatedName, customName);
        assertThat(exports).doesNotContainKey(generatedNameFromExporter);
        assertThat(exports.get(generatedName).getOriginalName()).contains("generated");
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

        assertThat(server.getMBeanInfo(objectName)).isNotNull();
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

        assertThat(server.getMBeanInfo(objectName1)).isNotNull();
        assertThat(server.getMBeanInfo(objectName2)).isNotNull();

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

        assertThat(server.getMBeanInfo(objectName1)).isNotNull();
        assertThat(server.getMBeanInfo(objectName2)).isNotNull();

        server.unregisterMBean(objectName1);
        server.unregisterMBean(objectName2);
    }

    @Test
    public void testNothingExported()
    {
        Injector injector = Guice.createInjector(
                new MBeanModule(),
                binder -> {
                    binder.requireExplicitBindings();
                    binder.disableCircularProxies();
                    binder.bind(MBeanServer.class).toInstance(ManagementFactory.getPlatformMBeanServer());
                });
        injector.getInstance(MBeanExporter.class);
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

        assertThat(server.getMBeanInfo(name1)).isNotNull();
        assertThat(server.getMBeanInfo(name2)).isNotNull();

        server.unregisterMBean(name1);
        server.unregisterMBean(name2);
    }

    public static final class TestObjectNameGenerator
            implements ObjectNameGenerator
    {
        @Override
        public String generatedNameOf(Class<?> type, Map<String, String> properties)
        {
            return new ObjectNameBuilder("test")
                    .withProperties(properties)
                    .build();
        }
    }

    private record DomainObjectNameGenerator(String domain)
            implements ObjectNameGenerator
    {
        @Override
        public String generatedNameOf(Class<?> type, Map<String, String> properties)
        {
            return new ObjectNameBuilder(domain)
                    .withProperties(properties)
                    .build();
        }
    }
}
