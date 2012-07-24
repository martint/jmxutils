package org.weakref.jmx.guice;

import javax.management.ObjectName;

public interface ObjectNameFunction<T>
{
    ObjectName name(T object);
}
