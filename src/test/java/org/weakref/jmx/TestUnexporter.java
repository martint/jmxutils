package org.weakref.jmx;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

public class TestUnexporter
{
    private MBeanServer server = null;
    private MBeanExporter exporter = null;

    private List<ObjectName> objectNames;

    @BeforeMethod
    public void setUp()
    {
        server = ManagementFactory.getPlatformMBeanServer();
        exporter = new MBeanExporter(server);

        objectNames = new ArrayList<ObjectName>();
        for (int i = 0; i < 10; ++i) {
            ObjectName name = Util.getUniqueObjectName();
            objectNames.add(name);
            exporter.export(name.getCanonicalName(), new TestBean());
        }

        Assert.assertNotNull(server);
        Assert.assertNotNull(exporter);
    }

    @AfterMethod
    public void tearDown()
    {
        Assert.assertNotNull(server);
        Assert.assertNotNull(exporter);

        for (ObjectName name : objectNames) {
            try {
                exporter.unexport(name.getCanonicalName());
            }
            catch (JmxException e) {
                // ignore
            }
        }
    }

    @Test(expectedExceptions = InstanceNotFoundException.class)
    public void testUnexportOk() throws Exception
    {
        ObjectName name = objectNames.get(0);

        Assert.assertEquals("Hello!", server.getAttribute(name, "Hello"));
        exporter.unexport(name.getCanonicalName());
        server.getAttribute(name, "Hello");
    }

    @Test
    public void testUnexportDouble() throws Throwable
    {
        ObjectName name = objectNames.get(0);

        Assert.assertEquals("Hello!", server.getAttribute(name, "Hello"));
        exporter.unexport(name.getCanonicalName());

        try {
            exporter.unexport(name.getCanonicalName());
        }
        catch (JmxException e) {
            Assert.assertEquals(e.getReason(), JmxException.Reason.INSTANCE_NOT_FOUND);
        }
    }

    @Test
    public void testUnexportAll()
            throws IntrospectionException, ReflectionException
    {
        exporter.unexportAllAndReportMissing();

        for (ObjectName name : objectNames) {
            try {
                server.getMBeanInfo(name);
                Assert.fail(format("failed to unexport %s", name.getCanonicalName()));
            }
            catch (InstanceNotFoundException e) {
                // success
            }
        }
    }

    @Test
    public void testUnexportAllIdempotent()
            throws IntrospectionException, ReflectionException
    {
        exporter.unexportAllAndReportMissing();

        for (ObjectName name : objectNames) {
            try {
                server.getMBeanInfo(name);
                Assert.fail(format("failed to unexport %s", name.getCanonicalName()));
            }
            catch (InstanceNotFoundException e) {
                // success
            }
        }

        Map<String,Exception> errors = exporter.unexportAllAndReportMissing();
        Assert.assertTrue(errors.isEmpty());
    }

    @Test
    public void testUnexportAllIgnoresMissing()
            throws IntrospectionException, ReflectionException, InstanceNotFoundException, MBeanRegistrationException
    {
        server.unregisterMBean(objectNames.get(0));

        Map<String,Exception> errors = exporter.unexportAllAndReportMissing();
        Assert.assertTrue(errors.isEmpty());

        for (ObjectName name : objectNames) {
            try {
                server.getMBeanInfo(name);
                Assert.fail(format("failed to unexport %s", name.getCanonicalName()));
            }
            catch (InstanceNotFoundException e) {
                // success
            }
        }
    }


    public static class TestBean
    {
        @Managed
        public String getHello()
        {
            return "Hello!";
        }
    }
}
