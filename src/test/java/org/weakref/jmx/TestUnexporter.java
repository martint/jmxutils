package org.weakref.jmx;

import java.lang.management.ManagementFactory;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TestUnexporter
{
    @Test
    public void testUnexport()
            throws Exception
    {
        ObjectName objectName = Util.getUniqueObjectName();
        String name = objectName.getCanonicalName();
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        MBeanExporter exporter = new MBeanExporter(server);
        exporter.export(name, new TestBean());
        try {
            Assert.assertEquals("Hello!", server.getAttribute(objectName, "Hello"));
        }
        finally {
            exporter.unexport(name);
        }
        boolean caught = false;
        try {
            server.getAttribute(objectName, "Hello");
            Assert.fail();
        }
        catch (InstanceNotFoundException e) {
            caught = true;
        }
        Assert.assertTrue(caught);
        caught = false;
        try {
            exporter.unexport(name);
            Assert.fail();
        }
        catch (RuntimeException e) {
            Assert.assertTrue(e.getCause() instanceof InstanceNotFoundException);
            caught = true;
        }
        Assert.assertTrue(caught);
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
