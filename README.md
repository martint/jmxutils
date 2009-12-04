# Example

    class ManagedObject
    {
        @Managed
        public int getValue()
        {
           ...
        }

        @Managed
        public void setValue(int value)
        {
           ...
        }

        @Managed(description="do the operation")
        public void operation()
        {
           ...
        }
    }

    ...
    MBeanExporter exporter = new MBeanExporter(ManagementFactory.getPlatformMBeanServer());
    exporter.export("test:name=X", new ManagedObject());

# Guice support

    Injector injector = Guice.createInjector(
        new AbstractModule() {
            @Override
            protected void configure() {
                // MBeanModules expect an MBeanServer to be bound
                binder().bind(MBeanServer.class).toInstance(ManagementFactory.getPlatformMBeanServer());
            }
        },
        new MBeanModule() {
            @Override
            protected void configureMBeans()
            {
                export(ManagedObject.class).as("test:name=X");
                export(ManagedObject.class).annotatedWith(SomeAnnotation.class).as("test:name=Y");
            }
        }, ...); 


    Injector injector = Guice.createInjector(
        new MBeanModule(), // used to trigger registration of mbeans exported via ExportBuilder
	    new AbstractModule() {
                @Override
                protected void configure() {
                   // MBeanModule expects an MBeanServer to be bound
                   binder().bind(MBeanServer.class).toInstance(ManagementFactory.getPlatformMBeanServer());

                   ExportBuilder builder = MBeanModule.newExporter(binder());
                   builder.export(AnotherManagedObject.class).as("test:name="Z");
                }
        }, ...);

# License

Licensed under the Apache License, Version 2.0 (the "License")

You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
