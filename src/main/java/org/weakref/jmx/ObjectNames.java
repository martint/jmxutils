package org.weakref.jmx;

import com.google.inject.name.Named;

import java.lang.annotation.Annotation;

/**
 * Generate JMX object names.
 */
public final class ObjectNames
{
    private ObjectNames() {}

    /**
     * Produce a standardized JMX object name.
     *
     * @return JMX object name of the form "[package_name]:name=[class_name]"
     */
    public static String generatedNameOf(Class<?> clazz)
    {
        return builder(clazz).build();
    }

    /**
     * Produce a generated JMX object name.
     *
     * @return JMX object name of the form "[package_name]:type=[class_name],name=[ann_class_name]"
     */
    public static String generatedNameOf(Class<?> clazz, Annotation annotation)
    {
        return builder(clazz, annotation).build();
    }

    /**
     * Produce a generated JMX object name.
     *
     * @return JMX object name of the form "[package_name]:type=[class_name],name=[ann_class_name]"
     */
    public static String generatedNameOf(Class<?> clazz, Class<? extends Annotation> annotationClass)
    {
        return builder(clazz, annotationClass).build();
    }

    /**
     * Produce a generated JMX object name.
     *
     * @return JMX object name of the form "[package_name]:type=[class_name],name=[named_value]"
     */
    public static String generatedNameOf(Class<?> clazz, Named named)
    {
        return builder(clazz, named).build();
    }

    /**
     * Produce a generated JMX object name.
     *
     * @return JMX object name of the form "[package_name]:type=[class_name],name=[named_value]"
     */
    public static String generatedNameOf(Class<?> clazz, String name)
    {
        return builder(clazz, name).build();
    }

    static String quoteValueIfNecessary(String name)
    {
        boolean needQuote = false;
        StringBuilder builder = new StringBuilder("\"");
        for (int i = 0; i < name.length(); ++i) {
            char c = name.charAt(i);
            switch (c) {
                case ':':
                case ',':
                case '=':
                    needQuote = true;
                    builder.append(c);
                    break;
                case '\"':
                case '?':
                case '*':
                    needQuote = true;
                    builder.append('\\');
                    builder.append(c);
                    break;
                case '\n':
                    needQuote = true;
                    builder.append("\\n");
                    break;
                case '\\':
                    builder.append("\\\\");
                    break;
                default:
                    builder.append(c);
            }
        }

        if (needQuote) {
            name = builder.append('\"').toString();
        }
        return name;
    }

    public static ObjectNameBuilder builder(Class<?> clazz)
    {
        return new ObjectNameBuilder(clazz.getPackage().getName())
                .withProperty("name", clazz.getSimpleName());
    }

    public static ObjectNameBuilder builder(Class<?> clazz, Annotation annotation)
    {
        return builder(clazz, annotation.annotationType());
    }

    public static ObjectNameBuilder builder(Class<?> clazz, Class<? extends Annotation> annotationClass)
    {
        return builder(clazz, annotationClass.getSimpleName());
    }

    public static ObjectNameBuilder builder(Class<?> clazz, Named named)
    {
        return builder(clazz, named.value());
    }

    public static ObjectNameBuilder builder(Class<?> clazz, String name)
    {
        return new ObjectNameBuilder(clazz.getPackage().getName())
                .withProperty("type", clazz.getSimpleName())
                .withProperty("name", name);
    }
}
