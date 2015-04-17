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

import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.Paranamer;

import javax.management.Descriptor;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class MBeanOperationBuilder
{
    private Object target;
    private String name;
    private Method concreteMethod;
    private Method annotatedMethod;

    public MBeanOperationBuilder onInstance(Object target)
    {
        if (target == null) throw new NullPointerException("target is null");
        this.target = target;
        return this;
    }

    public MBeanOperationBuilder named(String name)
    {
        if (name == null) throw new NullPointerException("name is null");
        this.name = name;
        return this;
    }

    public MBeanOperationBuilder withConcreteMethod(Method concreteMethod)
    {
        if (concreteMethod == null) {
            throw new NullPointerException("concreteMethod is null");
        }
        this.concreteMethod = concreteMethod;
        return this;
    }

    public MBeanOperationBuilder withAnnotatedMethod(Method annotatedMethod)
    {
        if (annotatedMethod == null) {
            throw new NullPointerException("annotatedMethod is null");
        }
        this.annotatedMethod = annotatedMethod;
        return this;
    }

    public MBeanOperation build()
    {
        if (target == null) {
            throw new IllegalArgumentException("JmxOperation must have a target object");
        }

        // We must have a method to invoke
        if (concreteMethod == null) {
            throw new IllegalArgumentException("JmxOperation must have a concrete method");
        }

        String operationName = name;
        if (operationName == null) {
            operationName = concreteMethod.getName();
        }

        //
        // Build Parameter Infos
        // Extract parameter names from debug symbols
        String[] parameterNames = getParameterNames(concreteMethod);
        Class<?>[] types = concreteMethod.getParameterTypes();

        // Parameter annotations used form descriptor come from the annotated method, not the public method
        Annotation[][] parameterAnnotations;
        if (annotatedMethod != null) {
            parameterAnnotations = annotatedMethod.getParameterAnnotations();
        }
        else {
            parameterAnnotations = new Annotation[annotatedMethod.getParameterTypes().length][];
        }

        MBeanParameterInfo[] parameterInfos = new MBeanParameterInfo[parameterNames.length];
        for (int i = 0; i < parameterNames.length; ++i) {
            // Parameter Descriptor
            Descriptor parameterDescriptor = AnnotationUtils.buildDescriptor(parameterAnnotations[i]);
            // Parameter Description
            String parameterDescription = AnnotationUtils.getDescription(parameterDescriptor, parameterAnnotations[i]);

            parameterInfos[i] = new MBeanParameterInfo(
                    parameterNames[i],
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

    private static String[] getParameterNames(Method method)
    {
        try {
            Paranamer paranamer = new BytecodeReadingParanamer();
            return paranamer.lookupParameterNames(method);
        }
        catch (RuntimeException e) {
            String[] names = new String[method.getParameterCount()];
            for (int i = 0; i < names.length; i++) {
                names[i] = "p" + i;
            }
            return names;
        }
    }
}
