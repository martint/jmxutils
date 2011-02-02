package org.weakref.jmx;

import java.lang.management.ManagementFactory;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestUnexporter
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

        exporter.export(name, new TestBean());

        Assert.assertNotNull(server);
        Assert.assertNotNull(exporter);
        Assert.assertNotNull(objectName);
        Assert.assertNotNull(name);
    }

    @AfterMethod
    public void tearDown()
    {
        Assert.assertNotNull(server);
        Assert.assertNotNull(exporter);
        Assert.assertNotNull(objectName);

        if (name != null) {
            try {
                exporter.unexport(name);
            }
            catch (JmxException je) {
                Assert.fail("Could not unexport mbean", je);
            }
            name = null;
        }

        server = null;
        exporter = null;
        objectName = null;
    }

    @Test(expectedExceptions = InstanceNotFoundException.class)
    public void testUnexportOk() throws Exception
    {
        Assert.assertEquals("Hello!", server.getAttribute(objectName, "Hello"));
        exporter.unexport(name);
        name = null;
        server.getAttribute(objectName, "Hello");
    }

    @Test(expectedExceptions = JmxException.class)
    public void testUnexportDouble() throws Throwable
    {
        Assert.assertEquals("Hello!", server.getAttribute(objectName, "Hello"));
        exporter.unexport(name);

        try {
            exporter.unexport(name);
        }
        catch (Throwable t) {
            Assert.assertTrue(t instanceof JmxException);
            Assert.assertEquals(((JmxException) t).getReason(), JmxException.Reason.INSTANCE_NOT_FOUND);
            throw t;
        }
        finally {
            name = null;
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
