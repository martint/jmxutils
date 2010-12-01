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
    exporter.unexport("test:name=X");

# Guice support

## Subclassing MBeanModule
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

## Exporting from custom modules
    Injector injector = Guice.createInjector(
        new MBeanModule(), // used to trigger registration of mbeans exported via ExportBuilder
	    new AbstractModule() {
                @Override
                protected void configure() {
                   // MBeanModule expects an MBeanServer to be bound
                   binder().bind(MBeanServer.class).toInstance(ManagementFactory.getPlatformMBeanServer());

                   ExportBuilder builder = MBeanModule.newExporter(binder());
                   builder.export(AnotherManagedObject.class).as("test:name="Z");
                   
                   // You can use a standardized naming scheme for singletons if you wish.
                   // See ObjectNames.generatedNameOf(Class<?>) for the naming scheme.
                   builder.export(ManagedSingleton.class).withGeneratedName();
                }
        }, ...);


# Custom annotations

You can use your own annotations instead of @Managed to tag methods. To do so, you need to tag the annotation with
the org.weakref.jmx.ManagedAnnotation meta annotation.

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.METHOD })
    @ManagedAnnotation
    public @interface CustomAnnotation
    {
        String description() default "";
        String type() default "";
    }

    class ManagedObject
    {
        @CustomAnnotation(description="foo", type="counter")
        public int getValue()
        {
           ...
        }
    }

If the custom annotation has an attribute "description" of type String, it will be used as the description of the
jmx method or attribute.

# Advanced Usage

jmxutils has advanced support for nested managed objects.  This first example demonstrates the @Nested annotation:

    public class NestedExample
    {
       private final NestedObject nestedObject = new NestedObject();

       @Managed @Nested
       public NestedObject getNestedObject()
       {
           return nestedObject;
       }

       public static final class NestedObject {
           @Managed
           public String getValue() {
               return "someValue";
           }
       }
    }

When the @Nested annotation is applied to a managed getter, jmxutils simply retrieves the value from the getter and exposes all managed attributes and operations prefixed with the getter name.  In the example above, the exposed MBean will have an attribute named "NestedObject.Value" and an operation named "NestedObject.doSomething". 

Next the example demonstrates the @Flatten annotation.

    public class FlattenedExample
    {
       private final FlattenedObject flattenedObject = new FlattenedObject();

       @Managed @Flatten
       public FlattenedObject getFlattenedObject()
       {
           return flattenedObject;
       }

       public static final class FlattenedObject {
           @Managed
           public String getValue() {
               return "someValue";
           }
           @Managed
           public void doSomething() {
               System.out.println("something");
           }
       }
    }

The @Flatten annotation works like the @Nested annotation except the exposed attributes and operation are not prefixed with the getter name.  In the example above, the exposed MBean will simply have an attribute named "Value" and an operation named "doSomething" without any prefix.


# Maven dependency

To use jmxutils in maven projects:

    <dependency>
        <groupId>org.weakref</groupId>
        <artifactId>jmxutils</artifactId>
        <version>1.6</version>
    </dependency>

# License

Licensed under the Apache License, Version 2.0 (the "License")

You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
