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
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import org.weakref.jmx.MBeanExporter;
import org.weakref.jmx.ObjectNameGenerator;

import javax.management.MBeanServer;

import java.util.Optional;
import java.util.Set;

import static com.google.inject.multibindings.Multibinder.newSetBinder;
import static com.google.inject.multibindings.OptionalBinder.newOptionalBinder;
import static java.util.Objects.requireNonNull;

public final class MBeanModule
        extends AbstractModule
{

    public static MBeanModule withUnnamespacedObjectNames()
    {
        return new MBeanModule(ObjectNameGeneratorStrategy.NO_NAMESPACE);
    }

    public static MBeanModule forCustomObjectNameGenerator()
    {
        return new MBeanModule(ObjectNameGeneratorStrategy.EXPLICIT_NAMING);
    }

    private final ObjectNameGeneratorStrategy objectNameGeneratorStrategy;

    /**
     * @deprecated It should be explicit choice to export MBeans without namespacing, as this can easily lead to name collisions.
     * Use {@link #withUnnamespacedObjectNames()} if you want this behavior and use {@link #forCustomObjectNameGenerator()} if you
     * want to provide your own naming strategy perhaps for namespacing.
     */
    @Deprecated
    public MBeanModule()
    {
        this(ObjectNameGeneratorStrategy.LEGACY);
    }

    private MBeanModule(ObjectNameGeneratorStrategy objectNameGeneratorStrategy)
    {
        this.objectNameGeneratorStrategy = requireNonNull(objectNameGeneratorStrategy, "objectNameGeneratorStrategy is null");
    }

    @Override
    protected void configure()
    {
        switch (objectNameGeneratorStrategy) {
            case LEGACY -> {
                bind(GuiceMBeanExporter.class)
                        .toProvider(LegacyBindingGuiceMBeanExporterProvider.class)
                        .asEagerSingleton();
                bind(MBeanExporter.class)
                        .toProvider(LegacyBindingMBeanExporterProvider.class)
                        .in(Scopes.SINGLETON);
                newOptionalBinder(binder(), ObjectNameGenerator.class);
            }

            case NO_NAMESPACE -> {
                bind(GuiceMBeanExporter.class).asEagerSingleton();
                bind(MBeanExporter.class).in(Scopes.SINGLETON);
                bind(ObjectNameGenerator.class).toInstance(ObjectNameGenerator.defaultObjectNameGenerator());
            }

            case EXPLICIT_NAMING -> {
                bind(GuiceMBeanExporter.class).asEagerSingleton();
                bind(MBeanExporter.class).in(Scopes.SINGLETON);
                // require explicit binding
                bind(ObjectNameGenerator.class);
            }
        }

        newSetBinder(binder(), new TypeLiteral<Mapping>() {});
        newSetBinder(binder(), new TypeLiteral<SetMapping<?>>() {});
        newSetBinder(binder(), new TypeLiteral<MapMapping<?, ?>>() {});
    }

    enum ObjectNameGeneratorStrategy
    {
        /**
         * As it used to be - user can export mbeans with the {@link ObjectNameGenerator#defaultObjectNameGenerator() default strategy},
         * or bind their own generator to {@code ObjectNameGenerator.class} key (or via optional binder)
         *
         * @deprecated exists only to support deprecated legacy mode
         */
        @Deprecated
        LEGACY,

        /**
         * This strategy forces user can export mbeans with the {@link ObjectNameGenerator#defaultObjectNameGenerator() default strategy}.
         * If custom strategy is needed, {@link #EXPLICIT_NAMING} should be used.
         * This strategy is suitable when MBeanModule is used in top level Guice context of an application.
         */
        NO_NAMESPACE,

        /**
         * This strategy requires that {@link ObjectNameGenerator} is separately bound.
         * This strategy is suitable when MBeanModule is used in a library/module Guice context,
         * where lack of namespacing may lead to name collisions.
         */
        EXPLICIT_NAMING,
    }

    private static class LegacyBindingGuiceMBeanExporterProvider
            implements Provider<GuiceMBeanExporter>
    {
        private final Set<Mapping> mappings;
        private final Set<SetMapping<?>> setMappings;
        private final Set<MapMapping<?, ?>> mapMappings;
        private final MBeanExporter exporter;
        private final ObjectNameGenerator objectNameGenerator;
        private final Injector injector;

        @Inject
        public LegacyBindingGuiceMBeanExporterProvider(
                Set<Mapping> mappings,
                Set<SetMapping<?>> setMappings,
                Set<MapMapping<?, ?>> mapMappings,
                MBeanExporter exporter,
                Optional<ObjectNameGenerator> objectNameGenerator,
                Injector injector)
        {
            this.mappings = requireNonNull(mappings, "mappings is null");
            this.setMappings = requireNonNull(setMappings, "setMappings is null");
            this.mapMappings = requireNonNull(mapMappings, "mapMappings is null");
            this.exporter = requireNonNull(exporter, "exporter is null");
            this.objectNameGenerator = objectNameGenerator.orElseGet(ObjectNameGenerator::defaultObjectNameGenerator);
            this.injector = requireNonNull(injector, "injector is null");
        }

        @Override
        public GuiceMBeanExporter get()
        {
            return new GuiceMBeanExporter(mappings, setMappings, mapMappings, exporter, objectNameGenerator, injector);
        }
    }

    private static class LegacyBindingMBeanExporterProvider
            implements Provider<MBeanExporter>
    {

        private final MBeanServer server;
        private final ObjectNameGenerator objectNameGenerator;

        @Inject
        public LegacyBindingMBeanExporterProvider(MBeanServer server, Optional<ObjectNameGenerator> objectNameGenerator)
        {
            this.server = requireNonNull(server, "server is null");
            this.objectNameGenerator = objectNameGenerator.orElseGet(ObjectNameGenerator::defaultObjectNameGenerator);
        }

        @Override
        public MBeanExporter get()
        {
            return new MBeanExporter(server, objectNameGenerator);
        }
    }
}
