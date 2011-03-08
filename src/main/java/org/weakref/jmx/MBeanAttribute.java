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

import javax.management.MBeanAttributeInfo;
import javax.management.AttributeNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.InvalidAttributeValueException;

public interface MBeanAttribute extends MBeanFeature
{
    MBeanAttributeInfo getInfo();

    String getName();

    Object getValue()
            throws AttributeNotFoundException, MBeanException, ReflectionException;

    void setValue(Object value)
                    throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException;
}
