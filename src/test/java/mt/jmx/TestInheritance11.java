package mt.jmx;

import java.util.List;
import java.util.ArrayList;

// Annotation inherited from parent, covariant generic return type in child => A
public class TestInheritance11
        extends TestInheritanceBase
{
    public TestInheritance11()
    {
        super(B.class, A.class);
    }

    private static class A
    {
        @Managed(description="A")
        public List<Object> method()
        {
            return null;
        }
    }

    private static class B
        extends A
    {
        @Override
        public ArrayList<Object> method()
        {
            return null;
        }
    }

}