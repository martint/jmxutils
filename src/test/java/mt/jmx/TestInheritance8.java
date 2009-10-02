package mt.jmx;

// Annotation inherited from multiple interfaces => C
public class TestInheritance8
        extends TestInheritanceBase
{
    public TestInheritance8()
    {
        super(B.class, C.class);
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