package mt.jmx.guice;

import com.google.inject.Inject;
import com.google.inject.Injector;
import mt.jmx.MBeanExporter;

import javax.management.MBeanServer;
import java.util.Set;

class GuiceMBeanExporter
{
    @Inject
    public GuiceMBeanExporter(Set<Mapping> mappings, MBeanServer server, Injector injector)
    {
        MBeanExporter exporter = new MBeanExporter(server);

        for (Mapping mapping : mappings) {
            exporter.export(mapping.getName(), injector.getInstance(mapping.getKey()));
        }
    }
}
