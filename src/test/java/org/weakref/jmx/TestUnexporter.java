package org.weakref.jmx;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestInstance(PER_CLASS)
public class TestUnexporter
{
    private MBeanServer server;
    private MBeanExporter exporter;

    private List<ObjectName> objectNames;

    @BeforeEach
    public void setUp()
    {
        server = ManagementFactory.getPlatformMBeanServer();
        exporter = new MBeanExporter(server);

        objectNames = new ArrayList<>();
        for (int i = 0; i < 10; ++i) {
            ObjectName name = Util.getUniqueObjectName();
            objectNames.add(name);
            exporter.export(name.getCanonicalName(), new TestBean());
        }

        Assert.assertNotNull(server);
        Assert.assertNotNull(exporter);
    }

    @AfterEach
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

    @Test
    public void testUnexportOk() throws Exception
    {
        ObjectName name = objectNames.get(0);

        Assert.assertEquals("Hello!", server.getAttribute(name, "Hello"));
        exporter.unexport(name.getCanonicalName());
        assertThatThrownBy(() -> server.getAttribute(name, "Hello"))
                .isInstanceOf(InstanceNotFoundException.class);
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
