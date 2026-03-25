package org.weakref.jmx;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import java.lang.management.ManagementFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestInstance(PER_CLASS)
public class TestExports
{
    private MBeanServer server;
    private MBeanExporter exporter;

    private ObjectName objectName;
    private String name;

    @BeforeEach
    public void setUp()
    {
        assertThat(name).isNull();
        objectName = Util.getUniqueObjectName();
        name = objectName.getCanonicalName();
        server = ManagementFactory.getPlatformMBeanServer();
        exporter = new MBeanExporter(server);

        assertThat(server).isNotNull();
        assertThat(exporter).isNotNull();
        assertThat(objectName).isNotNull();
        assertThat(name).isNotNull();
    }

    @AfterEach
    public void tearDown()
    {
        assertThat(name).isNotNull();
        assertThat(server).isNotNull();
        assertThat(exporter).isNotNull();
        assertThat(objectName).isNotNull();

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

        assertThat(server.getAttribute(objectName, "Hello")).isEqualTo("Hello!");
    }

    @Test
    public void testExportDouble() throws Throwable
    {
        exporter.export(name, new TestBean());

        assertThat(server.getAttribute(objectName, "Hello")).isEqualTo("Hello!");

        try {
            exporter.export(name, new TestBean());
        }
        catch (JmxException e) {
            assertThat(e.getReason()).isEqualTo(JmxException.Reason.INSTANCE_ALREADY_EXISTS);
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
