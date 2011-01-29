package org.weakref.jmx;

import java.lang.management.ManagementFactory;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.weakref.jmx.JmxException.JmxCause;

public class TestExports
{
    private MBeanServer server = null;
    private MBeanExporter exporter = null;

    private ObjectName objectName = null;
    private String name = null;

    @BeforeMethod
    public void setUp()
    {
        Assert.assertNull(name);
        objectName = Util.getUniqueObjectName();
        name = objectName.getCanonicalName();
        server = ManagementFactory.getPlatformMBeanServer();
        exporter = new MBeanExporter(server);

        Assert.assertNotNull(server);
        Assert.assertNotNull(exporter);
        Assert.assertNotNull(objectName);
        Assert.assertNotNull(name);
    }

    @AfterMethod
    public void tearDown()
    {
        Assert.assertNotNull(name);
        Assert.assertNotNull(server);
        Assert.assertNotNull(exporter);
        Assert.assertNotNull(objectName);

        exporter.unexport(name);

        name = null;
        server = null;
        exporter = null;
        objectName = null;
    }

    @Test
    public void testExportOk() throws Exception
    {
        exporter.export(name, new TestBean());

        Assert.assertEquals("Hello!", server.getAttribute(objectName, "Hello"));
    }

    @Test(expectedExceptions = JmxException.class)
    public void testExportDouble() throws Throwable
    {
        exporter.export(name, new TestBean());

        Assert.assertEquals("Hello!", server.getAttribute(objectName, "Hello"));

        try {
            exporter.export(name, new TestBean());
        }
        catch (Throwable t) {
            Assert.assertTrue(t instanceof JmxException);
            Assert.assertEquals(((JmxException) t).getJmxCause(), JmxCause.INSTANCE_ALREADY_EXISTS);
            throw t;
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
