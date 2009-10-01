package mt.jmx.guice;

import com.google.inject.multibindings.Multibinder;
import com.google.inject.Key;

import java.lang.annotation.Annotation;

public class NamedBindingBuilder
{
    protected final Multibinder<Mapping> binder;
    protected final Class<?> clazz;
    protected final Annotation annotation;
    protected final Class<? extends Annotation> annotationClass;

    NamedBindingBuilder(Multibinder<Mapping> binder, Class<?> clazz)
    {
        this.binder = binder;
        this.clazz = clazz;
        this.annotation = null;
        this.annotationClass = null;
    }

    NamedBindingBuilder(Multibinder<Mapping> binder, Class<?> clazz, Annotation annotation)
    {
        this.binder = binder;
        this.clazz = clazz;
        this.annotation = annotation;
        this.annotationClass = null;
    }

    NamedBindingBuilder(Multibinder<Mapping> binder, Class<?> clazz, Class<? extends Annotation> annotationClass)
    {
        this.binder = binder;
        this.clazz = clazz;
        this.annotation = null;
        this.annotationClass = annotationClass;
    }

    public void as(String name)
    {
        Key<?> key;
        if (annotation != null) {
            key = Key.get(clazz, annotation);
        }
        else if (annotationClass != null) {
            key = Key.get(clazz, annotationClass);
        }
        else {
            key = Key.get(clazz);
        }

        binder.addBinding().toInstance(new Mapping(name, key));
    }
}
