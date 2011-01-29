package org.weakref.jmx;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestRecordingMBeanExporter
{
    private MBeanServer server = null;
    private RecordingMBeanExporter exporter = null;

    @BeforeMethod
    public void setUp()
    {
        Assert.assertNull(exporter);

        server = ManagementFactory.getPlatformMBeanServer();
        Assert.assertNotNull(server);
    }

    @AfterMethod
    public void tearDown()
    {
        Assert.assertNotNull(server);
        Assert.assertNotNull(exporter);

        exporter.unexportAll();

        server = null;
        exporter = null;
    }

    @Test
    public void testUnexportOk() throws Exception
    {
        exporter = new RecordingMBeanExporter(server);

        List<ObjectName> objectNames = new ArrayList<ObjectName>();
        for (int i = 0; i < 100; i++) {
            final ObjectName objectName = Util.getUniqueObjectName();
            objectNames.add(objectName);
            final String name = objectName.getCanonicalName();

            exporter.export(name, new TestBean());
        }

        for (ObjectName objectName : objectNames) {
            Assert.assertEquals("Hello!", server.getAttribute(objectName, "Hello"));
        }

        exporter.unexportAll();

        for (ObjectName objectName : objectNames) {
            try {
                Assert.assertEquals("Hello!", server.getAttribute(objectName, "Hello"));
                Assert.fail(objectName.getCanonicalName() + " was not unexported!");
            }
            catch (InstanceNotFoundException infe) {
                // do nothing
            }
        }
    }

    @Test
    public void testDoubleUnexport() throws Exception
    {
        testUnexportOk();

        Assert.assertNotNull(exporter);

        exporter.unexportAll();
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
