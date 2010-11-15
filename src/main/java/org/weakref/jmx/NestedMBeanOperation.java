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

import javax.management.MBeanOperationInfo;
import javax.management.MBeanException;
import javax.management.ReflectionException;

public class NestedMBeanOperation implements MBeanOperation
{
    private final MBeanOperation delegate;
    private final MBeanOperationInfo info;
    private final Signature signature;

    public NestedMBeanOperation(String prefix, MBeanOperation delegate)
    {
        this.delegate = delegate;

        MBeanOperationInfo delegateInfo = delegate.getInfo();
        this.info = new MBeanOperationInfo(prefix + "." + delegateInfo.getName(),
                delegateInfo.getDescription(),
                delegateInfo.getSignature(),
                delegateInfo.getReturnType(),
                delegateInfo.getImpact(),
                delegateInfo.getDescriptor());

        signature = new Signature(this.info);
    }

    public MBeanOperationInfo getInfo()
    {
        return info;
    }

    public Signature getSignature()
    {
        return signature;
    }

    public Object invoke(Object[] params)
            throws MBeanException, ReflectionException
    {
        return delegate.invoke(params);
    }
}
