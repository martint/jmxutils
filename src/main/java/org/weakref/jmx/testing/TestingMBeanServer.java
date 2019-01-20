package org.weakref.jmx.testing;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.ReflectionException;
import javax.management.loading.ClassLoaderRepository;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;

public class TestingMBeanServer
        implements MBeanServer
{
    private final Map<ObjectName, DynamicMBean> mbeans = new HashMap<>();

    @Override
    public ObjectInstance registerMBean(Object object, ObjectName name)
            throws InstanceAlreadyExistsException
    {
        if (name == null) {
            // TODO: extract from ((MBeanRegistration) object)
            throw new UnsupportedOperationException("Only explicit name supported at this time");
        }

        if (!(object instanceof DynamicMBean)) {
            throw new UnsupportedOperationException("Only DynamicMBeans supported at this time");
        }

        DynamicMBean mbean = (DynamicMBean) object;

        if (mbeans.containsKey(name)) {
            throw new InstanceAlreadyExistsException(format("MBean already registered: %s", name));
        }

        mbeans.put(name, mbean);

        return new ObjectInstance(name, mbean.getMBeanInfo().getClassName());
    }

    @Override
    public void unregisterMBean(ObjectName name)
            throws InstanceNotFoundException
    {
        if (mbeans.remove(name) == null) {
            throw new InstanceNotFoundException(format("MBean not found: %s", name));
        }
    }

    @Override
    public ObjectInstance getObjectInstance(ObjectName name)
            throws InstanceNotFoundException
    {
        DynamicMBean mbean = getMBean(name);

        return new ObjectInstance(name, mbean.getMBeanInfo().getClassName());
    }

    @Override
    public Set<ObjectName> queryNames(ObjectName name, QueryExp query)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getAttribute(ObjectName name, String attribute)
            throws InstanceNotFoundException, AttributeNotFoundException, ReflectionException, MBeanException
    {
        return getMBean(name).getAttribute(attribute);
    }

    @Override
    public AttributeList getAttributes(ObjectName name, String[] attributes)
            throws InstanceNotFoundException
    {
        return getMBean(name).getAttributes(attributes);
    }

    @Override
    public boolean isRegistered(ObjectName name)
    {
        return mbeans.containsKey(name);
    }

    @Override
    public Integer getMBeanCount()
    {
        return mbeans.size();
    }

    @Override
    public void setAttribute(ObjectName name, Attribute attribute)
            throws InstanceNotFoundException, InvalidAttributeValueException, AttributeNotFoundException, ReflectionException, MBeanException
    {
        getMBean(name).setAttribute(attribute);
    }

    @Override
    public AttributeList setAttributes(ObjectName name, AttributeList attributes)
            throws InstanceNotFoundException
    {
        return getMBean(name).setAttributes(attributes);
    }

    @Override
    public Object invoke(ObjectName name, String operationName, Object[] params, String[] signature)
            throws InstanceNotFoundException, ReflectionException, MBeanException
    {
        DynamicMBean mbean = getMBean(name);

        return mbean.invoke(operationName, params, signature);
    }

    @Override
    public MBeanInfo getMBeanInfo(ObjectName name)
            throws InstanceNotFoundException
    {
        DynamicMBean mbean = getMBean(name);

        return mbean.getMBeanInfo();
    }


    private DynamicMBean getMBean(ObjectName name)
            throws InstanceNotFoundException
    {
        DynamicMBean mbean = mbeans.get(name);

        if (mbean == null) {
            throw new InstanceNotFoundException(format("MBean not found: %s", name));
        }

        return mbean;
    }


    @Override
    public ObjectInstance createMBean(String className, ObjectName name)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public ObjectInstance createMBean(String className, ObjectName name, Object[] params, String[] signature)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName, Object[] params, String[] signature)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<ObjectInstance> queryMBeans(ObjectName name, QueryExp query)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getDefaultDomain()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String[] getDomains()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addNotificationListener(ObjectName name, NotificationListener listener, NotificationFilter filter, Object handback)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addNotificationListener(ObjectName name, ObjectName listener, NotificationFilter filter, Object handback)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeNotificationListener(ObjectName name, ObjectName listener)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeNotificationListener(ObjectName name, ObjectName listener, NotificationFilter filter, Object handback)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeNotificationListener(ObjectName name, NotificationListener listener)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeNotificationListener(ObjectName name, NotificationListener listener, NotificationFilter filter, Object handback)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isInstanceOf(ObjectName name, String className)
            throws InstanceNotFoundException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object instantiate(String className)
            throws ReflectionException, MBeanException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object instantiate(String className, ObjectName loaderName)
            throws ReflectionException, MBeanException, InstanceNotFoundException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object instantiate(String className, Object[] params, String[] signature)
            throws ReflectionException, MBeanException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object instantiate(String className, ObjectName loaderName, Object[] params, String[] signature)
            throws ReflectionException, MBeanException, InstanceNotFoundException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public ObjectInputStream deserialize(ObjectName name, byte[] data)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public ObjectInputStream deserialize(String className, byte[] data)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public ObjectInputStream deserialize(String className, ObjectName loaderName, byte[] data)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public ClassLoader getClassLoaderFor(ObjectName mbeanName)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public ClassLoader getClassLoader(ObjectName loaderName)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public ClassLoaderRepository getClassLoaderRepository()
    {
        throw new UnsupportedOperationException();
    }

}
