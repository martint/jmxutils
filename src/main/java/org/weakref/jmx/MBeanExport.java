package org.weakref.jmx;

import javax.management.ObjectName;

import static java.util.Objects.requireNonNull;

public class MBeanExport
{
    private final ObjectName objectName;
    private final Runnable unexport;

    public MBeanExport(ObjectName objectName, Runnable unexport)
    {
        this.objectName = requireNonNull(objectName, "objectName is null");
        this.unexport = requireNonNull(unexport, "unexport is null");
    }

    public ObjectName getObjectName()
    {
        return objectName;
    }

    public void unexport()
    {
        unexport.run();
    }

    @Override
    public String toString()
    {
        return objectName.toString();
    }
}
