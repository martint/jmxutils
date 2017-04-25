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

import static org.weakref.jmx.ReflectionUtils.getAttributeName;
import static org.weakref.jmx.ReflectionUtils.isGetter;
import static org.weakref.jmx.ReflectionUtils.isSetter;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.google.common.base.Preconditions.checkNotNull;

class MBeanBuilder
{
    private String className;
    private final List<MBeanAttributeBuilder> attributeBuilders = new ArrayList<MBeanAttributeBuilder>();
    private final List<MBeanOperationBuilder> operationBuilders = new ArrayList<MBeanOperationBuilder>();
    private String description;

    public static MBeanBuilder from(Object object)
    {
        return from(object.getClass(), Suppliers.ofInstance(object));
    }

    static MBeanBuilder from(Class targetType, Supplier targetSupplier)
    {
        return new MBeanBuilder(targetType, targetSupplier);
    }

    private MBeanBuilder(Class targetType, Supplier targetSupplier)
    {
        checkNotNull(targetType, "targetType is null");
        checkNotNull(targetSupplier, "targetSupplier is null");

        Map<String, MBeanAttributeBuilder> attributeBuilders = new TreeMap<String, MBeanAttributeBuilder>();

        for (Map.Entry<Method, Method> entry : AnnotationUtils.findManagedMethods(targetType).entrySet()) {
            Method concreteMethod = entry.getKey();
            Method annotatedMethod = entry.getValue();

            if (isGetter(concreteMethod) || isSetter(concreteMethod)) { // is it an attribute?
                String attributeName = getAttributeName(concreteMethod);

                MBeanAttributeBuilder attributeBuilder = attributeBuilders.get(attributeName);
                if (attributeBuilder == null) {
                    attributeBuilder = new MBeanAttributeBuilder().named(attributeName).withTargetSupplier(targetSupplier);
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
                        .withTargetSupplier(targetSupplier)
                        .withConcreteMethod(concreteMethod)
                        .withAnnotatedMethod(annotatedMethod)
                        .build();
            }
        }

        for (MBeanAttributeBuilder attributeBuilder : attributeBuilders.values()) {
            this.attributeBuilders.add(attributeBuilder);
        }

        className = targetType.getName();
        description = AnnotationUtils.getDescription(targetType.getAnnotations());
    }

    public MBeanBuilder withDescription(String description)
    {
        this.description = description;
        return this;
    }

    public MBeanAttributeBuilder addAttribute()
    {
        MBeanAttributeBuilder builder = new MBeanAttributeBuilder();
        attributeBuilders.add(builder);
        return builder;
    }

    public MBeanOperationBuilder addOperation()
    {
        MBeanOperationBuilder builder = new MBeanOperationBuilder();
        operationBuilders.add(builder);
        return builder;
    }

    public MBean build()
    {
        List<MBeanAttribute> attributes = new ArrayList<MBeanAttribute>();
        List<MBeanOperation> operations = new ArrayList<MBeanOperation>();
        for (MBeanAttributeBuilder attributeBuilder : attributeBuilders) {
            for (MBeanFeature feature : attributeBuilder.build()) {
                if (feature instanceof MBeanAttribute) {
                    attributes.add((MBeanAttribute) feature);
                }
                if (feature instanceof MBeanOperation) {
                    operations.add((MBeanOperation) feature);
                }
            }
        }
        for (MBeanOperationBuilder operationBuilder : operationBuilders) {
            operations.add(operationBuilder.build());
        }

        return new MBean(className, description, attributes, operations);
    }
}
