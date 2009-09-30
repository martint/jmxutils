package mt.jmx;

import javax.management.Descriptor;
import javax.management.IntrospectionException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.modelmbean.DescriptorSupport;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanConstructorInfo;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.ModelMBeanInfoSupport;
import javax.management.modelmbean.ModelMBeanNotificationInfo;
import javax.management.modelmbean.ModelMBeanOperationInfo;
import javax.management.modelmbean.RequiredModelMBean;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MBeanExporter
{
    private final MBeanServer server;

    public MBeanExporter(MBeanServer server)
    {
        this.server = server;
    }

    public void export(String name, Object object)
    {
        try {
            ObjectName objectName = new ObjectName(name);

            MBeanInfoBuilder builder = new MBeanInfoBuilder();
            ModelMBeanInfo info = builder.buildInfo(object.getClass());

            RequiredModelMBean mbean = new RequiredModelMBean(info);
            mbean.setManagedResource(object, "objectReference");

            // register the model MBean in the MBean server
            server.registerMBean(mbean, objectName);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
