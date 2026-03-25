package org.weakref.jmx;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.assertj.core.api.Assertions.assertThat;

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

        assertThat(server).isNotNull();
        assertThat(exporter).isNotNull();
    }

    @AfterEach
    public void tearDown()
    {
        assertThat(server).isNotNull();
        assertThat(exporter).isNotNull();

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

        assertThat(server.getAttribute(name, "Hello")).isEqualTo("Hello!");
        exporter.unexport(name.getCanonicalName());
        assertThatThrownBy(() -> server.getAttribute(name, "Hello"))
                .isInstanceOf(InstanceNotFoundException.class);
    }

    @Test
    public void testUnexportDouble() throws Throwable
    {
        ObjectName name = objectNames.get(0);

        assertThat(server.getAttribute(name, "Hello")).isEqualTo("Hello!");
        exporter.unexport(name.getCanonicalName());

        try {
            exporter.unexport(name.getCanonicalName());
        }
        catch (JmxException e) {
            assertThat(e.getReason()).isEqualTo(JmxException.Reason.INSTANCE_NOT_FOUND);
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
                throw new AssertionError(format("failed to unexport %s", name.getCanonicalName()));
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
                throw new AssertionError(format("failed to unexport %s", name.getCanonicalName()));
            }
            catch (InstanceNotFoundException e) {
                // success
            }
        }

        Map<String,Exception> errors = exporter.unexportAllAndReportMissing();
        assertThat(errors.isEmpty()).isTrue();
    }

    @Test
    public void testUnexportAllIgnoresMissing()
            throws IntrospectionException, ReflectionException, InstanceNotFoundException, MBeanRegistrationException
    {
        server.unregisterMBean(objectNames.get(0));

        Map<String,Exception> errors = exporter.unexportAllAndReportMissing();
        assertThat(errors.isEmpty()).isTrue();

        for (ObjectName name : objectNames) {
            try {
                server.getMBeanInfo(name);
                throw new AssertionError(format("failed to unexport %s", name.getCanonicalName()));
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
