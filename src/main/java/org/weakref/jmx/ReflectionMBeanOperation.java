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

import javax.management.MBeanException;
import javax.management.MBeanOperationInfo;
import javax.management.ReflectionException;
import java.lang.reflect.Method;

class ReflectionMBeanOperation implements MBeanOperation
{
    private final MBeanOperationInfo info;
    private final Object target;
    private final Method method;
    private final Signature signature;

    public ReflectionMBeanOperation(MBeanOperationInfo info, Object target, Method method)
    {
        this.info = info;
        this.target = target;
        this.method = method;

        this.signature = new Signature(method);
    }

    @Override
    public MBeanOperationInfo getInfo()
    {
        return info;
    }

    public Object getTarget()
    {
        return target;
    }

    @Override
    public Signature getSignature()
    {
        return signature;
    }

    public Method getMethod()
    {
        return method;
    }

    @Override
    public Object invoke(Object[] params)
            throws MBeanException, ReflectionException
    {
        Object result = ReflectionUtils.invoke(target, method, params);
        return result;
    }
}
