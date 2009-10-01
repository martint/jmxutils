package mt.jmx.guice;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

public abstract class MBeanModule
        extends AbstractModule
{
    private Multibinder<Mapping> binder;

    @Override
    protected final void configure()
    {
        binder = Multibinder.newSetBinder(binder(), Mapping.class);

        install(new InternalMBeanModule());
        configureMBeans();
    }

    protected abstract void configureMBeans();

    protected AnnotatedExportBuilder export(Class<?> clazz)
    {
        return new AnnotatedExportBuilder(binder, clazz);
    }

}
