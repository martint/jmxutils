package mt.jmx;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.Map;

public abstract class TestInheritanceBase
{
    private final Class target;
    private final Class source;

    /**
     * @param target class to resolve
     * @param source class providing the annotation
     */
    TestInheritanceBase(Class target, Class source)
    {
        this.target = target;
        this.source = source;
    }

    public Class getTargetClass()
    {
        return target;
    }

    public Method getTargetMethod() throws NoSuchMethodException
    {
        return target.getMethod("method");
    }

    public Managed expected() throws NoSuchMethodException
    {
        return source.getDeclaredMethod("method").getAnnotation(Managed.class);
    }

    @Test
    public void testResolver() throws NoSuchMethodException
    {
        AnnotationFinder resolver = new AnnotationFinder();
        Map<Method, Managed> map = resolver.findAnnotatedMethods(getTargetClass());
        Managed annotation = map.get(getTargetMethod());
        Assert.assertEquals(annotation, expected());
    }
}
