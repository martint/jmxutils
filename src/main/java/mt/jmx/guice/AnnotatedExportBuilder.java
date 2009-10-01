package mt.jmx.guice;

import com.google.inject.multibindings.Multibinder;

import java.lang.annotation.Annotation;

public class AnnotatedExportBuilder
        extends NamedBindingBuilder
{
    AnnotatedExportBuilder(Multibinder<Mapping> binder, Class<?> clazz)
    {
        super(binder, clazz);
    }

    public NamedBindingBuilder annotatedWith(Annotation annotation)
    {
        return new NamedBindingBuilder(binder, clazz, annotation);
    }

    public NamedBindingBuilder annotatedWith(Class<? extends Annotation> annotationClass)
    {
        return new NamedBindingBuilder(binder, clazz, annotationClass);
    }
}
