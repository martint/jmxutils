/**
 *  Copyright 2010 Dain Sundstrom
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.weakref.jmx;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ReflectionException;
import javax.management.RuntimeOperationsException;
import javax.management.ServiceNotFoundException;
import javax.management.modelmbean.ModelMBeanConstructorInfo;
import javax.management.modelmbean.ModelMBeanNotificationInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Collections;
import java.util.HashMap;

class MBean implements DynamicMBean
{
    private static final Object[] NO_PARAMS = new Object[0];
    private static final String[] NO_ARGS = new String[0];

    private final MBeanInfo mbeanInfo;
    private final Map<String, MBeanAttribute> attributes;
    private final Map<Signature, MBeanOperation> operations;

    public MBean(String className, String description, Collection<MBeanAttribute> attributes, Collection<MBeanOperation> operations)
    {
        List<MBeanAttributeInfo> attributeInfos = new ArrayList<>();
        Map<String, MBeanAttribute> attributesBuilder = new TreeMap<>();
        for (MBeanAttribute attribute : attributes) {
            attributesBuilder.put(attribute.getName(), attribute);
            attributeInfos.add(attribute.getInfo());
        }
        this.attributes = Collections.unmodifiableMap(attributesBuilder);

        Map<Signature, MBeanOperation> operationsBuilder = new HashMap<>();
        List<MBeanOperationInfo> operationsInfos = new ArrayList<>();
        for (MBeanOperation operation : operations) {
            operationsBuilder.put(operation.getSignature(), operation);
            operationsInfos.add(operation.getInfo());
        }
        this.operations = Collections.unmodifiableMap(operationsBuilder);

        mbeanInfo = new MBeanInfo(className,
                description,
                attributeInfos.toArray(new MBeanAttributeInfo[0]),
                new ModelMBeanConstructorInfo[0],
                operationsInfos.toArray(new MBeanOperationInfo[0]),
                new ModelMBeanNotificationInfo[0]);
    }

    @Override
    public MBeanInfo getMBeanInfo()
    {
        return mbeanInfo;
    }

    public Collection<MBeanAttribute> getAttributes()
    {
        return attributes.values();
    }

    public Collection<MBeanOperation> getOperations()
    {
        return operations.values();
    }

    @Override
    public Object invoke(String actionName, Object[] params, String[] argTypes)
            throws MBeanException, ReflectionException
    {
        assertNotNull("actionName", actionName);

        // params argTypes are allowed to be null and mean no-arg method
        if (params == null) {
            params = NO_PARAMS;
        }
        if (argTypes == null) {
            argTypes = NO_ARGS;
        }

        for (int i = 0; i < argTypes.length; i++) {
            assertNotNull("argTypes[" + i + "]", argTypes[i]);
        }

        Signature signature = new Signature(actionName, argTypes);
        MBeanOperation operation = operations.get(signature);
        if (operation == null) {
            String message = "Operation " + signature + " not found";
            throw new MBeanException(new ServiceNotFoundException(message), message);
        }

        Object result = operation.invoke(params);
        return result;
    }

    @Override
    public Object getAttribute(String name)
            throws AttributeNotFoundException, MBeanException, ReflectionException
    {
        assertNotNull("attribute", name);
        MBeanAttribute mbeanAttribute = attributes.get(name);
        if (mbeanAttribute == null) {
            throw new AttributeNotFoundException(name);
        }
        Object value = mbeanAttribute.getValue();
        return value;
    }

    @Override
    public void setAttribute(Attribute attribute)
            throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException
    {
        assertNotNull("attribute", attribute);
        String name = attribute.getName();
        assertNotNull("attribute.name", name);

        Object value = attribute.getValue();
        MBeanAttribute mbeanAttribute = attributes.get(name);
        if (mbeanAttribute == null) {
            throw new AttributeNotFoundException(name);
        }
        mbeanAttribute.setValue(value);
    }

    @Override
    public AttributeList getAttributes(String[] attributes)
    {
        // no attributes is a noop
        if (attributes == null) {
            return null;
        }

        AttributeList attributeList = new AttributeList();
        for (String attribute : attributes) {
            try {
                attributeList.add(new Attribute(attribute, getAttribute(attribute)));
            }
            catch (Exception e) {
                // todo log me
            }
        }
        return attributeList;
    }

    @Override
    public AttributeList setAttributes(AttributeList attributes)
    {
        // no attributes is a noop
        if (attributes == null) {
            return null;
        }

        AttributeList response = new AttributeList();
        for (Attribute attribute : attributes.asList()) {
            try {
                setAttribute(attribute);
                response.add(attribute);
            }
            catch (Exception ignored) {
            }
        }
        return response;
    }

    private static void assertNotNull(String name, Object value)
    {
        if (value == null) {
            throw new RuntimeOperationsException(new NullPointerException(name + " is null"));
        }
    }
}
