/**
 *  Copyright 2009 Martin Traverso
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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static java.util.Objects.requireNonNull;
import static org.weakref.jmx.ReflectionUtils.getAttributeName;
import static org.weakref.jmx.ReflectionUtils.isGetter;
import static org.weakref.jmx.ReflectionUtils.isSetter;

final class MBeanBuilder
{
    private final String className;
    private final List<MBeanAttributeBuilder> attributeBuilders = new ArrayList<>();
    private final List<MBeanOperationBuilder> operationBuilders = new ArrayList<>();
    private String description;

    private MBeanBuilder(String className)
    {
        this.className = className;
    }

    public static MBeanBuilder from(String className)
    {
        return new MBeanBuilder(className);
    }

    public static MBeanBuilder from(Object object)
    {
        return new MBeanBuilder(object);
    }

    public MBeanBuilder(Object target)
    {
        requireNonNull(target, "target is null");

        Map<String, MBeanAttributeBuilder> attributeBuilders = new TreeMap<>();

        for (Map.Entry<Method, Method> entry : AnnotationUtils.findManagedMethods(target.getClass()).entrySet()) {
            Method concreteMethod = entry.getKey();
            Method annotatedMethod = entry.getValue();

            if (isGetter(concreteMethod) || isSetter(concreteMethod)) { // is it an attribute?
                String attributeName = getAttributeName(concreteMethod);

                MBeanAttributeBuilder attributeBuilder = attributeBuilders.get(attributeName);
                if (attributeBuilder == null) {
                    attributeBuilder = new MBeanAttributeBuilder().named(attributeName).onInstance(target);
                }
                
                if (isGetter(concreteMethod)) {
                    attributeBuilder = attributeBuilder
                            .withConcreteGetter(concreteMethod)
                            .withAnnotatedGetter(annotatedMethod);
                }
                else if (isSetter(concreteMethod)) {
                    attributeBuilder = attributeBuilder
                            .withConcreteSetter(concreteMethod)
                            .withAnnotatedSetter(annotatedMethod);
                }

                attributeBuilders.put(attributeName, attributeBuilder);
            }
            else {
                // TODO: change this so that we are not making assumptions about mutability or side effects
                //       in the builder
                addOperation()
                        .onInstance(target)
                        .withConcreteMethod(concreteMethod)
                        .withAnnotatedMethod(annotatedMethod)
                        .build();
            }
        }

        this.attributeBuilders.addAll(attributeBuilders.values());

        className = target.getClass().getName();
        description = AnnotationUtils.getDescription(target.getClass().getAnnotations());
    }

    public MBeanBuilder withDescription(String description)
    {
        this.description = description;
        return this;
    }

    public MBeanAttributeBuilder addAttribute() {
        MBeanAttributeBuilder builder = new MBeanAttributeBuilder();
        attributeBuilders.add(builder);
        return builder;
    }

    public MBeanOperationBuilder addOperation() {
        MBeanOperationBuilder builder = new MBeanOperationBuilder();
        operationBuilders.add(builder);
        return builder;
    }

    public MBean build()
    {
        List<MBeanAttribute> attributes = new ArrayList<>();
        List<MBeanOperation> operations = new ArrayList<>();
        for (MBeanAttributeBuilder attributeBuilder : attributeBuilders) {
            for (MBeanFeature feature : attributeBuilder.build()) {
                if (feature instanceof MBeanAttribute attribute) {
                    attributes.add(attribute);
                }
                if (feature instanceof MBeanOperation operation) {
                    operations.add(operation);
                }
            }
        }
        for (MBeanOperationBuilder operationBuilder : operationBuilders) {
            operations.add(operationBuilder.build());
        }
        
        return new MBean(className, description, attributes, operations);
    }
}
