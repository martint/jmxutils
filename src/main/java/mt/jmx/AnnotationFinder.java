package mt.jmx;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

class AnnotationFinder
{
    public Map<Method, Managed> findAnnotatedMethods(Class<?> clazz)
    {
        Map<Method, Managed> result = new HashMap<Method, Managed>();

        // gather all publicly available methods
        for (Method method : clazz.getMethods()) {
            if (method.isSynthetic() || method.isBridge()) {
                continue;
            }

            Managed annotation = findAnnotation(clazz, method.getName(), method.getParameterTypes());
            if (annotation != null) {
                result.put(method, annotation);
            }
        }

        return result;
    }

    private Managed findAnnotation(Class<?> clazz, String methodName, Class<?>[] paramTypes)
    {
        Method method;
        try {
            method = clazz.getDeclaredMethod(methodName, paramTypes);
        }
        catch (NoSuchMethodException e) {
            return null;
        }

        Managed annotation = method.getAnnotation(Managed.class);

        if (annotation == null && clazz.getSuperclass() != null) {
            annotation = findAnnotation(clazz.getSuperclass(), methodName, paramTypes);
        }

        if (annotation == null) {
            for (Class iface : clazz.getInterfaces()) {
                annotation = findAnnotation(iface, methodName, paramTypes);
                if (annotation != null) {
                    break;
                }
            }
        }

        return annotation;
    }
}
