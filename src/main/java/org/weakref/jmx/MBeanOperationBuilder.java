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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.airlift.parameternames.ParameterNames.getParameterNames;

public class MBeanOperationBuilder
{
    private record OperationKey(Class<?> clazz, String operationName) {}

    private static final Map<OperationKey, MBeanOperationInfo> methodOperationInfoCache = new ConcurrentHashMap<>();

    private Object target;
    private String name;
    private Method concreteMethod;

    private Method annotatedMethod;

    public MBeanOperationBuilder onInstance(Object target)
    {
        if (target == null) {
            throw new NullPointerException("target is null");
        }
        this.target = target;
        return this;
    }

    public MBeanOperationBuilder named(String name)
    {
        if (name == null) {
            throw new NullPointerException("name is null");
        }
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

        final String operationName = name != null ?
                name :
                concreteMethod.getName();

        MBeanOperationInfo mbeanOperationInfo = methodOperationInfoCache.computeIfAbsent(new OperationKey(target.getClass(), operationName), methods -> {
            //
            // Build Parameter Infos
            List<String> parameterNames = getParameterNames(concreteMethod);
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

            return new MBeanOperationInfo(
                    operationName,
                    description,
                    parameterInfos,
                    concreteMethod.getReturnType().getName(),
                    MBeanOperationInfo.UNKNOWN,
                    descriptor);
        });

        return new ReflectionMBeanOperation(mbeanOperationInfo, target, concreteMethod);
    }
}
