package org.weakref.jmx;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public interface ObjectNameGenerator
{
    static ObjectNameGenerator defaultObjectNameGenerator()
    {
        return (domain, properties) -> new ObjectNameBuilder(domain)
                .withProperties(properties)
                .build();
    }

    default String generatedNameOf(Class<?> type)
    {
        return generatedNameOf(type, ImmutableMap.of("name", type.getSimpleName()));
    }

    default String generatedNameOf(Class<?> type, String name)
    {
        return generatedNameOf(type, ImmutableMap.<String, String>builder()
                .put("type", type.getSimpleName())
                .put("name", name)
                .build());
    }

    default String generatedNameOf(String packageName, String className)
    {
        return generatedNameOf(packageName, ImmutableMap.of("name", className));
    }

    default String generatedNameOf(String packageName, String className, String name)
    {
        return generatedNameOf(packageName, ImmutableMap.of("name", className, "type", name));
    }

    default String generatedNameOf(Package pkg, String className, String name)
    {
        return generatedNameOf(pkg.getName(), ImmutableMap.of("name", className, "type", name));
    }

    default String generatedNameOf(Package pkg, String className)
    {
        return generatedNameOf(pkg.getName(), ImmutableMap.of("name", className));
    }

    default String generatedNameOf(Class<?> type, Map<String, String> properties)
    {
        return generatedNameOf(type.getPackage().getName(), properties);
    }

    String generatedNameOf(String domain, Map<String, String> properties);
}
