package org.weakref.jmx.testing;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import org.weakref.jmx.guice.MBeanModule;

import javax.management.MBeanServer;

public class TestingMBeanModule
    extends AbstractModule
{
    @Override
    protected void configure()
    {
        install(new MBeanModule());
        bind(MBeanServer.class).to(TestingMBeanServer.class).in(Scopes.SINGLETON);
    }
}
