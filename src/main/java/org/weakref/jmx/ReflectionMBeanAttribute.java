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

import static org.weakref.jmx.ReflectionUtils.invoke;

import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import java.lang.reflect.Method;

class ReflectionMBeanAttribute implements MBeanAttribute
{
    private final MBeanAttributeInfo info;
    private final Object target;
    private final String name;
    private final Method getter;
    private final Method setter;

    public ReflectionMBeanAttribute(MBeanAttributeInfo info, Object target, Method getter, Method setter)
    {
        if (info == null) {
            throw new NullPointerException("info is null");
        }
        if (target == null) {
            throw new NullPointerException("target is null");
        }
        this.info = info;
        this.target = target;
        this.name = info.getName();
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public MBeanAttributeInfo getInfo()
    {
        return info;
    }

    public Object getTarget()
    {
        return target;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public Object getValue()
            throws AttributeNotFoundException, MBeanException, ReflectionException
    {
        if (getter == null) {
            throw new AttributeNotFoundException(name + " is write-only");
        }
        Object result = invoke(target, getter);
        return result;
    }

    @Override
    public void setValue(Object value)
            throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException
    {
        if (setter == null) {
            throw new AttributeNotFoundException(name + " is read-only");
        }
        if (!ReflectionUtils.isAssignable(value, setter.getParameterTypes()[0])) {
            throw new InvalidAttributeValueException("Can not assign " + value.getClass() + " to attribute " + name);
        }
        invoke(target, setter, value);
    }
}