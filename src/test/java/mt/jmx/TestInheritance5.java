package mt.jmx;

// Implemented method, overridden annotation from interface => B
public class TestInheritance5
    extends TestInheritanceBase
{
    public TestInheritance5()
    {
        super(B.class, B.class);
    }

    private static class A
    {
        @Managed(description="A")
        protected Object method() { return null; }
    }

    private static class B
        extends A
    {
        @Managed(description="B")
        public Object method() { return null; }
    }

}