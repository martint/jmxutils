package mt.jmx;

// Overriden method, inherit annotation from public parent => A
public class TestInheritance1
    extends TestInheritanceBase
{
    public TestInheritance1()
    {
        super(B.class, A.class);
    }

    private static class A
    {
        @Managed(description="A")
        public Object method() { return null; }
    }

    private static class B
        extends A
    {
        public Object method() { return null; }
    }


}
