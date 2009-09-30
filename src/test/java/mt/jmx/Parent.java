package mt.jmx;

public abstract class Parent
    implements Interface
{
    private int value;

    @Managed
    public void setValue(int value)
    {
        this.value = value;
    }

    @Managed
    public int getValue()
    {
        return value;
    }

    @Managed
    public Object getCovariant()
    {
        return null;
    }

    @Managed
    public Object getCovariant1()
    {
        return null;
    }
}
