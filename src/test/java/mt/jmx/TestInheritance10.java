package mt.jmx;

// Annotation inherited from parent, covariant return type in child => A
public class TestInheritance10
        extends TestInheritanceBase
{
    public TestInheritance10()
    {
        super(B.class, A.class);
    }

    private static class A
    {
        @Managed(description="A")
        public Object method()
        {
            return null;
        }
    }

    private static class B
        extends A
    {
        @Override
        public Integer method()
        {
            return null;
        }
    }

}