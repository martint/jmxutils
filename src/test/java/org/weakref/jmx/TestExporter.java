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

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.NotBoundException;
import java.util.ArrayList;
import java.util.List;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestExporter
{
    private MBeanServer server;
    
    private List<Pair<ObjectName, SimpleObject>> objects;

    static class Pair<L, R> {
      final L left;
      final R right;
      
      Pair(L left, R right) {
        this.left = left;
        this.right = right;
      }
      static <L, R> Pair<L, R> of (L left, R right) {
        return new Pair<L, R>(left, right);
      }
    }
    
    @BeforeTest
    private void setup()
            throws IOException, MalformedObjectNameException, NotBoundException
    {
        server = ManagementFactory.getPlatformMBeanServer();

        objects = new ArrayList<Pair<ObjectName,SimpleObject>>(2);
        objects.add(new Pair<ObjectName, SimpleObject>(Util.getUniqueObjectName(), new SimpleObject()));
        objects.add(new Pair<ObjectName, SimpleObject>(Util.getUniqueObjectName(), new CustomAnnotationObject()));

        MBeanExporter exporter = new MBeanExporter(ManagementFactory.getPlatformMBeanServer());
        for (Pair<ObjectName, SimpleObject> pair : objects) {
          exporter.export(pair.left.getCanonicalName(), pair.right);
        }
    }

    @AfterTest
    public void teardown()
            throws IOException, InstanceNotFoundException, MBeanRegistrationException
    {
        for (Pair<ObjectName, SimpleObject> pair : objects) {
          server.unregisterMBean(pair.left);
        }
    }

    @Test(dataProvider = "fixtures")
    public void testGet(String attribute, boolean isIs, Object[] values, Class clazz)
            throws MalformedObjectNameException, InstanceNotFoundException, IOException, ReflectionException, AttributeNotFoundException, MBeanException, NoSuchMethodException, InvocationTargetException, IllegalAccessException
    {
        String methodName = "set" + attribute;
        for (Pair<ObjectName, SimpleObject> pair : objects) {
            Method setter = pair.right.getClass().getMethod(methodName, clazz);

            for (Object value : values) {
                setter.invoke(pair.right, value);

                Assert.assertEquals(server.getAttribute(pair.left, attribute), value);
            }
        }
    }


    @Test(dataProvider = "fixtures")
    public void testSet(String attribute, boolean isIs, Object[] values, Class clazz)
            throws MalformedObjectNameException, InstanceNotFoundException, IOException, ReflectionException, AttributeNotFoundException, MBeanException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InvalidAttributeValueException
    {
        String methodName = (isIs ? "is" : "get") + attribute;

        for (Pair<ObjectName, SimpleObject> pair : objects) {
          Method getter = pair.right.getClass().getMethod(methodName);

          for (Object value : values) {
            server.setAttribute(pair.left, new javax.management.Attribute(attribute, value));

            Assert.assertEquals(getter.invoke(pair.right), value);
          }
        }
    }

    @Test
    public void testSetFailsOnNotManaged() throws InstanceNotFoundException, IOException, InvalidAttributeValueException, ReflectionException, AttributeNotFoundException, MBeanException
    {
        for (Pair<ObjectName, SimpleObject> pair : objects) {
            pair.right.setNotManaged(1);
            try {
                server.setAttribute(pair.left, new javax.management.Attribute("NotManaged", 2));
                Assert.fail("Should not allow setting unmanaged attribute");
            }
            catch (AttributeNotFoundException e) {
                // ignore
            }

            Assert.assertEquals(pair.right.getNotManaged(), 1);
        }
    }

    @Test
    public void testGetFailsOnNotManaged() throws InstanceNotFoundException, IOException, InvalidAttributeValueException, ReflectionException, AttributeNotFoundException, MBeanException
    {

        for (Pair<ObjectName, SimpleObject> pair : objects) {
            try {
                server.getAttribute(pair.left, "NotManaged");
                Assert.fail("Should not allow getting unmanaged attribute");
            }
            catch (AttributeNotFoundException e) {
                // ignore
            }
        }
    }

    @Test
    public void testGetFailsOnWriteOnly() throws InstanceNotFoundException, IOException, ReflectionException, MBeanException
    {
        for (Pair<ObjectName, SimpleObject> pair : objects) {
            try {
                server.getAttribute(pair.left, "WriteOnly");
                Assert.fail("Should not allow getting write-only attribute");
            }
            catch (AttributeNotFoundException e) {
                // ignore
            }
        }
    }

    @Test
    public void testSetFailsOnReadOnly() throws InstanceNotFoundException, IOException, ReflectionException, MBeanException, InvalidAttributeValueException
    {
        for (Pair<ObjectName, SimpleObject> pair : objects) {
            pair.right.setReadOnly(1);
            try {
                server.setAttribute(pair.left, new javax.management.Attribute("ReadOnly", 2));
                Assert.fail("Should not allow setting read-only attribute");
            }
            catch (AttributeNotFoundException e) {
                // ignore
            }

            Assert.assertEquals(pair.right.getReadOnly(), 1);
        }
    }

    @Test
    public void testDescription() throws IntrospectionException, InstanceNotFoundException, ReflectionException
    {
      for (Pair<ObjectName, SimpleObject> pair : objects) {
        boolean described = false;
        for (MBeanAttributeInfo info : server.getMBeanInfo(pair.left).getAttributes()) {
          if (info.getName().equals("DescribedInt")) {
            Assert.assertEquals("epic description", info.getDescription());
            described = true;
          } else {
            Assert.assertEquals("", info.getDescription());
          }
        }
        Assert.assertTrue(described);
      }
    }

    @Test(dataProvider = "fixtures")
    public void testOperation(String attribute, boolean isIs, Object[] values, Class clazz)
            throws InstanceNotFoundException, IOException, ReflectionException, MBeanException
    {
        for (Pair<ObjectName, SimpleObject> pair : objects) {
            for (Object value : values) {
                Assert.assertEquals(server.invoke(pair.left, "echo", new Object[]{value},
                      new String[]{Object.class.getName()}), value);
            }
        }
    }

    @DataProvider(name = "fixtures")
    private Object[][] getFixtures()
    {
        return new Object[][]{

                new Object[]{"BooleanValue", true, new Object[]{true, false}, Boolean.TYPE},
                new Object[]{"BooleanBoxedValue", true, new Object[]{true, false, null}, Boolean.class},
                new Object[]{"ByteValue", false, new Object[]{Byte.MAX_VALUE, Byte.MIN_VALUE, (byte) 0}, Byte.TYPE},
                new Object[]{"ByteBoxedValue", false, new Object[]{Byte.MAX_VALUE, Byte.MIN_VALUE, (byte) 0, null}, Byte.class},

                new Object[]{"ShortValue", false, new Object[]{Short.MAX_VALUE, Short.MIN_VALUE, (short) 0}, Short.TYPE},
                new Object[]{"ShortBoxedValue", false, new Object[]{Short.MAX_VALUE, Short.MIN_VALUE, (short) 0, null}, Short.class},

                new Object[]{"IntegerValue", false, new Object[]{Integer.MAX_VALUE, Integer.MIN_VALUE, 0}, Integer.TYPE},
                new Object[]{"IntegerBoxedValue", false, new Object[]{Integer.MAX_VALUE, Integer.MIN_VALUE, 0, null}, Integer.class},

                new Object[]{"LongValue", false, new Object[]{Long.MAX_VALUE, Long.MIN_VALUE, 0L}, Long.TYPE},
                new Object[]{"LongBoxedValue", false, new Object[]{Long.MAX_VALUE, Long.MIN_VALUE, 0L, null}, Long.class},

                new Object[]{"FloatValue", false, new Object[]{-Float.MIN_VALUE, -Float.MAX_VALUE, Float.MAX_VALUE, Float.MIN_VALUE, 0f, Float.NaN}, Float.TYPE},
                new Object[]{"FloatBoxedValue", false, new Object[]{-Float.MIN_VALUE, -Float.MAX_VALUE, Float.MAX_VALUE, Float.MIN_VALUE, 0f, Float.NaN, null}, Float.class},

                new Object[]{"DoubleValue", false, new Object[]{-Double.MIN_VALUE, -Double.MAX_VALUE, Double.MAX_VALUE, Double.MIN_VALUE, 0.0, Double.NaN}, Double.TYPE},
                new Object[]{"DoubleBoxedValue", false, new Object[]{-Double.MIN_VALUE, -Double.MAX_VALUE, Double.MAX_VALUE, Double.MIN_VALUE, 0.0, Double.NaN}, Double.class},

                new Object[]{"StringValue", false, new Object[]{null, "hello there"}, String.class},

                new Object[]{"ObjectValue", false, new Object[]{"random object", 1, true}, Object.class}

        };
    }
}


