package mt.jmx;

// Overriden method, overridden annotation from parent => B
public class TestInheritance3
    extends TestInheritanceBase
{
    public TestInheritance3()
    {
        super(B.class, B.class);
    }

    private static class A
    {
        @Managed(description = "A")
        protected Object method()
        {
            return null;
        }
    }

    private static class B
            extends A
    {
        @Managed(description = "B")
        public Object method()
        {
            return null;
        }
    }

}
