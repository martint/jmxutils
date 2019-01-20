package org.weakref.jmx;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public interface ObjectNameGenerator
{
    static ObjectNameGenerator defaultObjectNameGenerator()
    {
        return (type, properties) -> new ObjectNameBuilder(type.getPackage().getName())
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

    String generatedNameOf(Class<?> type, Map<String, String> properties);
}
