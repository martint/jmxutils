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
import javax.management.MBeanParameterInfo;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toUnmodifiableList;

final record Signature(String actionName, List<String> parameterTypes)
{
    public Signature
    {
        actionName = actionName;
        parameterTypes = unmodifiableList(parameterTypes);
    }

    public Signature(Method method)
    {
        this(method.getName(), Arrays.stream(method.getParameterTypes())
                .map(Class::getName)
                .collect(toUnmodifiableList()));
    }

    public Signature(String actionName, String... parameterTypes)
    {
        this(actionName, Arrays.asList(parameterTypes));
    }

    public Signature(MBeanOperationInfo info)
    {
        this(info.getName(), Arrays.stream(info.getSignature())
                .map(MBeanParameterInfo::getType)
                .collect(toUnmodifiableList()));
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(actionName).append('(');
        boolean first = true;
        for (String type : parameterTypes) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(type);
            first = false;
        }
        sb.append(')');
        return sb.toString();
    }
}
