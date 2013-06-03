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

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.weakref.jmx.testing.TestingMBeanServer;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.NotBoundException;
import java.util.ArrayList;
import java.util.List;

import static org.weakref.jmx.Util.getUniqueObjectName;

public class TestExporter extends AbstractMbeanTest<TestExporter.NamedObject>
{
    private MBeanServer server;

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

    @BeforeMethod
    void setup()
            throws IOException, MalformedObjectNameException, NotBoundException
    {
        server = new TestingMBeanServer();

        objects = new ArrayList<NamedObject>(2);
        objects.add(NamedObject.of(getUniqueObjectName(), new SimpleObject()));
        objects.add(NamedObject.of(getUniqueObjectName(), new CustomAnnotationObject()));
        objects.add(NamedObject.of(getUniqueObjectName(), new FlattenObject()));
        objects.add(NamedObject.of(getUniqueObjectName(), new CustomFlattenAnnotationObject()));
        objects.add(NamedObject.of(getUniqueObjectName(), new NestedObject()));
        objects.add(NamedObject.of(getUniqueObjectName(), new CustomNestedAnnotationObject()));

        MBeanExporter exporter = new MBeanExporter(server);
        for (NamedObject namedObject : objects) {
            exporter.export(namedObject.objectName.getCanonicalName(), namedObject.object);
        }
    }

//    @AfterTest
//    public void teardown()
//            throws IOException, InstanceNotFoundException, MBeanRegistrationException
//    {
//        for (NamedObject pair : objects) {
//            server.unregisterMBean(pair.objectName);
//        }
//    }

//    @Test
//    public void testMBeanInfo()
//            throws IntrospectionException, InstanceNotFoundException, ReflectionException
//    {
//        for (NamedObject pair : objects) {
//            info.get
//        }
//    }

}


