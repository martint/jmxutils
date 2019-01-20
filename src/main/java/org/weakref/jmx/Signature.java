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
import java.util.ArrayList;
import java.util.Arrays;
import static java.util.Collections.unmodifiableList;
import java.util.List;

final class Signature
{
    private final String actionName;
    private final List<String> parameterTypes;

    public Signature(Method method)
    {
        this.actionName = method.getName();

        List<String> builder = new ArrayList<>();
        for (Class<?> type : method.getParameterTypes()) {
            builder.add(type.getName());
        }
        parameterTypes = unmodifiableList(builder);
    }

    public Signature(String actionName, String... parameterTypes)
    {
        this(actionName, Arrays.asList(parameterTypes));
    }

    public Signature(String actionName, List<String> parameterTypes)
    {
        this.actionName = actionName;
        this.parameterTypes = unmodifiableList(parameterTypes);
    }

    public Signature(MBeanOperationInfo info) {
        this.actionName = info.getName();

        List<String> parameterTypes = new ArrayList<>(info.getSignature().length);
        for (MBeanParameterInfo parameterInfo : info.getSignature()) {
            parameterTypes.add(parameterInfo.getType());
        }
        this.parameterTypes = unmodifiableList(parameterTypes);
    }

    public String getActionName()
    {
        return actionName;
    }

    public List<String> getParameterTypes()
    {
        return parameterTypes;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Signature signature1 = (Signature) o;

        if (!actionName.equals(signature1.actionName)) return false;
        if (!parameterTypes.equals(signature1.parameterTypes)) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = actionName.hashCode();
        result = 31 * result + parameterTypes.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
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
