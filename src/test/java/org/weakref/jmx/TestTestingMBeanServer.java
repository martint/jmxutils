package org.weakref.jmx;

import org.junit.jupiter.api.Test;
import org.weakref.jmx.testing.TestingMBeanServer;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.Query;

import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptySet;
import static org.weakref.jmx.Assert.assertEquals;
import static org.weakref.jmx.Assert.assertTrue;

public class TestTestingMBeanServer
{
    @Test
    public void testUnsupportedQueryExp()
    {
        TestingMBeanServer server = new TestingMBeanServer();

        try {
            server.queryNames(makeObjectName("test", Map.of("type", "Test")), Query.eq(Query.attr("a"), Query.value("a")));
        }
        catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
        }
    }

    @Test
    public void testNonMatchingQuery()
            throws InstanceAlreadyExistsException
    {
        TestingMBeanServer server = new TestingMBeanServer();
        server.registerMBean(BEAN, makeObjectName("test", Map.of("type", "Other")));

        assertEquals(server.queryNames(makeObjectName("test", Map.of("type", "Test")), null), Set.of());
    }

    @Test
    public void testMatchingWildcard()
            throws InstanceAlreadyExistsException
    {
        TestingMBeanServer server = new TestingMBeanServer();
        server.registerMBean(BEAN, makeObjectName("test1", Map.of("type", "Other")));
        server.registerMBean(BEAN, makeObjectName("test2", Map.of("type", "Other")));
        server.registerMBean(BEAN, makeObjectName("test3", Map.of("type", "Other")));
        server.registerMBean(BEAN, makeObjectName("test4", Map.of("type", "Other")));

        assertEquals(server.queryNames(ObjectName.WILDCARD, null).size(), 4);
        assertEquals(server.queryNames(ObjectName.WILDCARD, null), Set.of(
               makeObjectName("test1", Map.of("type", "Other")),
               makeObjectName("test2", Map.of("type", "Other")),
               makeObjectName("test3", Map.of("type", "Other")),
               makeObjectName("test4", Map.of("type", "Other"))));
    }

    @Test
    public void testMatchingSpecificDomain()
            throws InstanceAlreadyExistsException, MalformedObjectNameException
    {
        TestingMBeanServer server = new TestingMBeanServer();
        server.registerMBean(BEAN, makeObjectName("test1", Map.of("type", "Other")));
        server.registerMBean(BEAN, makeObjectName("test1", Map.of("type", "Other2")));
        server.registerMBean(BEAN, makeObjectName("test2", Map.of("type", "Other")));
        server.registerMBean(BEAN, makeObjectName("test3", Map.of("type", "Other")));
        server.registerMBean(BEAN, makeObjectName("test4", Map.of("type", "Other")));
        server.registerMBean(BEAN, makeObjectName("test4", Map.of("type", "Other2")));

        assertEquals(server.queryNames(new ObjectName("test1:*"), null), Set.of(
                makeObjectName("test1", Map.of("type", "Other")),
                makeObjectName("test1", Map.of("type", "Other2"))));
    }

    @Test
    public void testMatchingSpecificDomainAndProperty()
            throws InstanceAlreadyExistsException, MalformedObjectNameException
    {
        TestingMBeanServer server = new TestingMBeanServer();
        server.registerMBean(BEAN, makeObjectName("test1", Map.of("type", "Other")));
        server.registerMBean(BEAN, makeObjectName("test1", Map.of("type", "Other2")));
        server.registerMBean(BEAN, makeObjectName("test2", Map.of("type", "Other")));
        server.registerMBean(BEAN, makeObjectName("test3", Map.of("type", "Other")));
        server.registerMBean(BEAN, makeObjectName("test4", Map.of("type", "Other")));
        server.registerMBean(BEAN, makeObjectName("test4", Map.of("type", "Other2")));

        assertEquals(server.queryNames(new ObjectName("test1:type=*"), null), Set.of(
                makeObjectName("test1", Map.of("type", "Other")),
                makeObjectName("test1", Map.of("type", "Other2"))));

        assertEquals(server.queryNames(
                new ObjectName("test1:type=Other2"), null),
                Set.of(makeObjectName("test1", Map.of("type", "Other2"))));
    }

    @Test
    public void testMatchingByProperty()
            throws InstanceAlreadyExistsException, MalformedObjectNameException
    {
        TestingMBeanServer server = new TestingMBeanServer();
        server.registerMBean(BEAN, makeObjectName("test1", Map.of("type", "Other")));
        server.registerMBean(BEAN, makeObjectName("test1", Map.of("type", "Other2")));
        server.registerMBean(BEAN, makeObjectName("test1", Map.of("type", "Another")));
        server.registerMBean(BEAN, makeObjectName("test1", Map.of("type", "Another2")));

        assertEquals(server.queryNames(new ObjectName("test1:type=*"), null), Set.of(
                makeObjectName("test1", Map.of("type", "Other")),
                makeObjectName("test1", Map.of("type", "Other2")),
                makeObjectName("test1", Map.of("type", "Another")),
                makeObjectName("test1", Map.of("type", "Another2"))));

        assertEquals(server.queryNames(new ObjectName("test1:type=Other"), null), Set.of(
                makeObjectName("test1", Map.of("type", "Other"))));

        assertEquals(server.queryNames(new ObjectName("test1:type=Other*"), null), Set.of(
                makeObjectName("test1", Map.of("type", "Other")),
                makeObjectName("test1", Map.of("type", "Other2"))));

        assertEquals(server.queryNames(new ObjectName("test1:type=Another*"), null), Set.of(
                makeObjectName("test1", Map.of("type", "Another")),
                makeObjectName("test1", Map.of("type", "Another2"))));

        assertEquals(server.queryNames(new ObjectName("test1:type=*ther*"), null), Set.of(
                makeObjectName("test1", Map.of("type", "Other")),
                makeObjectName("test1", Map.of("type", "Other2")),
                makeObjectName("test1", Map.of("type", "Another")),
                makeObjectName("test1", Map.of("type", "Another2"))));

        assertEquals(server.queryNames(new ObjectName("test1:type=*ther"), null), Set.of(
                makeObjectName("test1", Map.of("type", "Other")),
                makeObjectName("test1", Map.of("type", "Another"))));

        assertEquals(server.queryNames(new ObjectName("test1:type=*ther?"), null), Set.of(
                makeObjectName("test1", Map.of("type", "Other2")),
                makeObjectName("test1", Map.of("type", "Another2"))));
    }

    private static ObjectName makeObjectName(String domain, Map<String, String> properties)
    {
        try {
            return ObjectName.getInstance(new ObjectNameBuilder(domain)
                    .withProperties(properties)
                    .build());
        }
        catch (MalformedObjectNameException e) {
            throw new RuntimeException(e);
        }
    }

    private static MBean BEAN = new MBean("TestBean", "TestDescription", emptySet(), emptySet());
}
