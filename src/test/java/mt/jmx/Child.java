package mt.jmx;

public class Child
    extends Parent
    implements Interface2
{
    private int value2;
    private int value3;
    private int covariant;
    private int covariant1;

    public int getValue2()
    {
        return value2;
    }

    public void setValue2(int value2)
    {
        this.value2 = value2;
    }

    public int getValue3()
    {
        return value3;
    }

    public void setValue3(int value3)
    {
        this.value3 = value3;
    }

    public void setCovariant(int covariant)
    {
        this.covariant = covariant;
    }

    @Override
    @Managed
    public Integer getCovariant()
    {
        return covariant;
    }

    @Override
    public Integer getCovariant1()
    {
        return covariant1;
    }

    public void setCovariant1(int covariant1)
    {
        this.covariant1 = covariant1;
    }
}
