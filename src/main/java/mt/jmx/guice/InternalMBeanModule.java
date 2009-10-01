package mt.jmx.guice;

import com.google.inject.AbstractModule;

/**
 * This module has to get installed only once, so equals & hashCode are implemented
 * to test for class identity
 */
final class InternalMBeanModule
        extends AbstractModule
{
    @Override
    protected void configure()
    {
        bind(GuiceMBeanExporter.class).asEagerSingleton();
    }

    @Override
    public boolean equals(Object o)
    {
        // Is only ever installed internally, so we don't need to check state.
        return o instanceof InternalMBeanModule;
    }

    @Override
    public int hashCode()
    {
        return InternalMBeanModule.class.hashCode();
    }
}
