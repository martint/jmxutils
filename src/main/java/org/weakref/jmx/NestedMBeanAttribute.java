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

import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.ReflectionException;

public class NestedMBeanAttribute implements MBeanAttribute
{
    private final MBeanAttribute delegate;
    private final MBeanAttributeInfo info;

    public NestedMBeanAttribute(String prefix, MBeanAttribute delegate)
    {
        this.delegate = delegate;

        MBeanAttributeInfo delegateInfo = delegate.getInfo();
        this.info = new MBeanAttributeInfo(prefix + "." + delegateInfo.getName(),
                delegateInfo.getType(),
                delegateInfo.getDescription(),
                delegateInfo.isReadable(),
                delegateInfo.isWritable(),
                delegateInfo.isIs(),
                delegateInfo.getDescriptor());
    }

    public MBeanAttributeInfo getInfo()
    {
        return info;
    }

    public String getName()
    {
        return info.getName();
    }

    public Object getValue()
            throws AttributeNotFoundException, MBeanException, ReflectionException
    {
        return delegate.getValue();
    }

    public void setValue(Object value)
            throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException
    {
        delegate.setValue(value);
    }
}