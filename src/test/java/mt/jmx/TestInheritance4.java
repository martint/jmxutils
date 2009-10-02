package mt.jmx;

// Implemented method, inherit annotation from interface => A
public class TestInheritance4
    extends TestInheritanceBase
{
    public TestInheritance4()
    {
        super(B.class, A.class);
    }

    private static interface A
    {
        @Managed(description="A")
        Object method();
    }

    private static class B
        implements A
    {
        public Object method() { return null; }
    }


}