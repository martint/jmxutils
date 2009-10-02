package mt.jmx;

// Annotation inherited from parent of interface and direct interface => E
public class TestInheritance9
        extends TestInheritanceBase
{
    public TestInheritance9()
    {
        super(B.class, E.class);
    }

    private static class B
            implements C, D
    {
        public Object method()
        {
            return null;
        }
    }

    private static interface C
        extends E
    {
        Object method();
    }

    private static interface E
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