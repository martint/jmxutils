package mt.jmx;

// Overriden method, inherit annotation from protected parent => A
public class TestInheritance2
    extends TestInheritanceBase
{
    public TestInheritance2()
    {
        super(B.class, A.class);
    }

    private static class A
    {
        @Managed(description="A")
        protected Object method() { return null; }
    }

    private static class B
        extends A
    {
        public Object method() { return null; }
    }


}
