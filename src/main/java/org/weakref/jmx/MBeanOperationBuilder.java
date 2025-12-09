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

import javax.management.Descriptor;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toUnmodifiableList;

public class MBeanOperationBuilder
{
    private Object target;
    private String name;
    private Method concreteMethod;
    private Method annotatedMethod;

    public MBeanOperationBuilder onInstance(Object target)
    {
        this.target = requireNonNull(target, "target is null");
        return this;
    }

    public MBeanOperationBuilder named(String name)
    {
        this.name = requireNonNull(name, "name is null");
        return this;
    }

    public MBeanOperationBuilder withConcreteMethod(Method concreteMethod)
    {
        this.concreteMethod = requireNonNull(concreteMethod, "concreteMethod is null");
        return this;
    }

    public MBeanOperationBuilder withAnnotatedMethod(Method annotatedMethod)
    {
        this.annotatedMethod = requireNonNull(annotatedMethod, "annotatedMethod is null");
        return this;
    }

    public MBeanOperation build()
    {
        requireNonNull(target, "JmxOperation must have a target object");
        // We must have a method to invoke
        requireNonNull(concreteMethod, "JmxOperation must have a concrete method");

        String operationName = name;
        if (operationName == null) {
            operationName = concreteMethod.getName();
        }

        //
        // Build Parameter Infos
        List<String> parameterNames = Arrays.stream(concreteMethod.getParameters())
                .map(Parameter::getName)
                .collect(toUnmodifiableList());

        Class<?>[] types = concreteMethod.getParameterTypes();

        // Parameter annotations used form descriptor come from the annotated method, not the public method
        Annotation[][] parameterAnnotations;
        if (annotatedMethod != null) {
            parameterAnnotations = annotatedMethod.getParameterAnnotations();
        }
        else {
            parameterAnnotations = new Annotation[parameterNames.size()][];
        }

        MBeanParameterInfo[] parameterInfos = new MBeanParameterInfo[parameterNames.size()];
        for (int i = 0; i < parameterNames.size(); i++) {
            // Parameter Descriptor
            Descriptor parameterDescriptor = AnnotationUtils.buildDescriptor(parameterAnnotations[i]);
            // Parameter Description
            String parameterDescription = AnnotationUtils.getDescription(parameterDescriptor, parameterAnnotations[i]);

            parameterInfos[i] = new MBeanParameterInfo(
                    parameterNames.get(i),
                    types[i].getName(),
                    parameterDescription,
                    parameterDescriptor);
        }

        // Descriptor
        Descriptor descriptor = null;
        if (annotatedMethod != null) {
            descriptor = AnnotationUtils.buildDescriptor(annotatedMethod);
        }

        // Description
        String description = AnnotationUtils.getDescription(descriptor, annotatedMethod);

        MBeanOperationInfo mbeanOperationInfo = new MBeanOperationInfo(
                operationName,
                description,
                parameterInfos,
                concreteMethod.getReturnType().getName(),
                MBeanOperationInfo.UNKNOWN,
                descriptor);

        return new ReflectionMBeanOperation(mbeanOperationInfo, target, concreteMethod);
    }
}
