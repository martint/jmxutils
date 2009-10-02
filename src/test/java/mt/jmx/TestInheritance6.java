package mt.jmx;

// Annotation inherited from parent class and interface => A
public class TestInheritance6
        extends TestInheritanceBase
{
    public TestInheritance6()
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
            implements C5
    {
        public Object method()
        {
            return null;
        }
    }

    private static interface C5
    {
        @Managed(description = "C5")
        Object method();
    }
}