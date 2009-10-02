package mt.jmx;

import java.util.List;
import java.util.ArrayList;

// Annotation inherited from parent, covariant generic parameter in return type in child => A
public class TestInheritance12
        extends TestInheritanceBase
{
    public TestInheritance12()
    {
        super(B.class, A.class);
    }

    private static class A
    {
        @Managed(description="A")
        public List<? extends Number> method()
        {
            return null;
        }
    }

    private static class B
        extends A
    {
        @Override
        public List<Integer> method()
        {
            return null;
        }
    }

}