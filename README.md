# Example

class ManagedObject
{
    @Managed
    public int getValue()
    {
       ...
    }

    @Managed
    public void setValue(int value)
    {
       ...
    }

    @Managed
    public void operation()
    {
       ...
    }
}


MBeanExporter exporter = new MBeanExporter(ManagementFactory.getPlatformMBeanServer());
exporter.export("test:name=X", new ManagedObject());

