package mt.jmx;

// Annotation inherited from parent class and multiple interfaces => A
public class TestInheritance7
        extends TestInheritanceBase
{
    public TestInheritance7()
    {
        super(B.class, A.class);
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
            implements C, D
    {
        public Object method()
        {
            return null;
        }
    }

    private static interface C
    {
        @Managed(description = "C")
        Object method();
    }

    private static interface D
    {
        @Managed(description = "D")
        Object method();
    }

}