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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.weakref.jmx.testing.TestingMBeanServer;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.weakref.jmx.Util.getUniqueObjectName;

public class TestExporter extends AbstractMbeanTest<TestExporter.NamedObject>
{
    private MBeanServer server;
    private MBeanExporter exporter;

    static class NamedObject
    {
        final ObjectName objectName;
        final Object object;

        NamedObject(ObjectName objectName, Object object)
        {
            this.objectName = objectName;
            this.object = object;
        }

        static NamedObject of(ObjectName left, Object right)
        {
            return new NamedObject(left, right);
        }
    }

    @Override
    protected Object getObject(NamedObject namedObject)
    {
        return namedObject.object;
    }

    @Override
    protected MBeanInfo getMBeanInfo(NamedObject namedObject)
            throws Exception
    {
        return server.getMBeanInfo(namedObject.objectName);
    }

    @Override
    protected Object getAttribute(NamedObject namedObject, String attributeName)
            throws MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException
    {
        return server.getAttribute(namedObject.objectName, attributeName);
    }

    @Override
    protected void setAttribute(NamedObject namedObject, String attributeName, Object value)
            throws InstanceNotFoundException, AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException
    {
        server.setAttribute(namedObject.objectName, new javax.management.Attribute(attributeName, value));
    }

    @Override
    protected Object invoke(NamedObject namedObject, Object value, String operationName)
            throws InstanceNotFoundException, MBeanException, ReflectionException
    {
        return server.invoke(namedObject.objectName, operationName, new Object[] { value },
                                          new String[] { Object.class.getName() });
    }

    @BeforeEach
    void setup()
    {
        server = new TestingMBeanServer();

        objects = new ArrayList<>(2);
        objects.add(NamedObject.of(getUniqueObjectName(), new SimpleObject()));
        objects.add(NamedObject.of(getUniqueObjectName(), new CustomAnnotationObject()));
        objects.add(NamedObject.of(getUniqueObjectName(), new FlattenObject()));
        objects.add(NamedObject.of(getUniqueObjectName(), new CustomFlattenAnnotationObject()));
        objects.add(NamedObject.of(getUniqueObjectName(), new NestedObject()));
        objects.add(NamedObject.of(getUniqueObjectName(), new CustomNestedAnnotationObject()));

        exporter = new MBeanExporter(server);
        for (NamedObject namedObject : objects) {
            exporter.export(namedObject.objectName.getCanonicalName(), namedObject.object);
        }
    }

    @Test
    void testManagedClasses()
    {
        Map<String, ManagedClass> managedClasses = exporter.getManagedClasses();
        for(NamedObject namedObject : objects) {
            String name = namedObject.objectName.getCanonicalName();

            assertThat(managedClasses.containsKey(name)).isTrue();

            ManagedClass managedClass = managedClasses.get(name);
            assertThat(managedClass.getTarget()).isEqualTo(namedObject.object);

            if(namedObject.object instanceof NestedObject || namedObject.object instanceof FlattenObject) {
                assertThat(managedClass.getChildren().size()).isEqualTo(1);
                assertThat(managedClass.getChildren().get("SimpleObject").getTargetClass()).isEqualTo(SimpleObject.class);
            }
        }
    }

    @Test
    void testManagedObjectExportsForManualExports()
    {
        Map<ObjectName, ManagedObjectExport> managedObjectExports = exporter.getManagedObjectExports();
        for (NamedObject namedObject : objects) {
            ManagedObjectExport managedObjectExport = managedObjectExports.get(namedObject.objectName);

            assertThat(managedObjectExport).isNotNull();
            assertThat(managedObjectExport.getObjectName()).isEqualTo(namedObject.objectName);
            assertThat(managedObjectExport.getExportedType()).isEmpty();
            assertThat(managedObjectExport.getOriginalName()).isEmpty();
            assertThat(managedObjectExport.getOriginalProperties()).isEmpty();
            assertThat(managedObjectExport.getManagedClass().getTarget()).isEqualTo(namedObject.object);
        }
    }

    @Test
    void testManagedObjectExportsForTypedManualExports()
    {
        MBeanExporter exporter = new MBeanExporter(server);
        ObjectName objectName = getUniqueObjectName();
        SimpleObject object = new SimpleObject();

        exporter.export(objectName, object, TestExporter.class);

        ManagedObjectExport managedObjectExport = exporter.getManagedObjectExports().get(objectName);
        assertThat(managedObjectExport.getObjectName()).isEqualTo(objectName);
        assertThat(managedObjectExport.getExportedType()).contains(TestExporter.class);
        assertThat(managedObjectExport.getOriginalName()).isEmpty();
        assertThat(managedObjectExport.getOriginalProperties()).isEmpty();
        assertThat(managedObjectExport.getManagedClass().getTarget()).isEqualTo(object);
    }

    @Test
    void testManagedObjectExportsForGeneratedNames()
    {
        MBeanExporter exporter = new MBeanExporter(server);

        SimpleObject defaultObject = new SimpleObject();
        MBeanExport defaultExport = exporter.exportWithGeneratedName(defaultObject, SimpleObject.class);
        ManagedObjectExport defaultManagedObjectExport = exporter.getManagedObjectExports().get(defaultExport.getObjectName());
        assertThat(defaultManagedObjectExport.getObjectName()).isEqualTo(defaultExport.getObjectName());
        assertThat(defaultManagedObjectExport.getExportedType()).contains(SimpleObject.class);
        assertThat(defaultManagedObjectExport.getOriginalName()).isEmpty();
        assertThat(defaultManagedObjectExport.getOriginalProperties()).isEmpty();
        assertThat(defaultManagedObjectExport.getManagedClass().getTarget()).isEqualTo(defaultObject);

        SimpleObject namedObject = new SimpleObject();
        MBeanExport namedExport = exporter.exportWithGeneratedName(namedObject, TestExporter.class, "queued");
        ManagedObjectExport namedManagedObjectExport = exporter.getManagedObjectExports().get(namedExport.getObjectName());
        assertThat(namedManagedObjectExport.getObjectName()).isEqualTo(namedExport.getObjectName());
        assertThat(namedManagedObjectExport.getExportedType()).contains(TestExporter.class);
        assertThat(namedManagedObjectExport.getOriginalName()).contains("queued");
        assertThat(namedManagedObjectExport.getOriginalProperties()).isEmpty();
        assertThat(namedManagedObjectExport.getManagedClass().getTarget()).isEqualTo(namedObject);

        Map<String, String> properties = new HashMap<>();
        properties.put("segment", "x");
        SimpleObject propertyObject = new SimpleObject();
        MBeanExport propertyExport = exporter.exportWithGeneratedName(propertyObject, TestExporter.class, properties);
        properties.put("segment", "changed");

        ManagedObjectExport propertyManagedObjectExport = exporter.getManagedObjectExports().get(propertyExport.getObjectName());
        assertThat(propertyManagedObjectExport.getObjectName()).isEqualTo(propertyExport.getObjectName());
        assertThat(propertyManagedObjectExport.getExportedType()).contains(TestExporter.class);
        assertThat(propertyManagedObjectExport.getOriginalName()).isEmpty();
        assertThat(propertyManagedObjectExport.getOriginalProperties()).containsExactly(Map.entry("segment", "x"));
        assertThat(propertyManagedObjectExport.getManagedClass().getTarget()).isEqualTo(propertyObject);

        namedExport.unexport();
        assertThat(exporter.getManagedObjectExports()).doesNotContainKey(namedExport.getObjectName());
    }

    @Test
    void testDuplicateKey()
    {
        MBeanExporter exporter = new MBeanExporter(server);

        exporter.export("test:test=test", new SimpleObject());
        try {
            exporter.export("test:test=test", new SimpleObject());
        }
        catch(JmxException e) {
            // do nothing
        }
        assertThat(exporter.getExportedObjects().size()).isEqualTo(1);
    }
}
