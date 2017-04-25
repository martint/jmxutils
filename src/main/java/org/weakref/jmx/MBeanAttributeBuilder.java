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

import static org.weakref.jmx.ReflectionUtils.isValidGetter;
import static org.weakref.jmx.ReflectionUtils.isValidSetter;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;

import javax.management.Descriptor;
import javax.management.ImmutableDescriptor;
import javax.management.MBeanAttributeInfo;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MBeanAttributeBuilder
{
    private final static Pattern getterOrSetterPattern = Pattern.compile("(get|set|is)(.+)");
    private Supplier targetSupplier;
    private String name;
    private Method concreteGetter;
    private Method annotatedGetter;
    private Method concreteSetter;
    private Method annotatedSetter;
    private boolean flatten;
    private boolean nested;

    public MBeanAttributeBuilder withTargetSupplier(Supplier targetSupplier)
    {
        if (targetSupplier == null) {
            throw new NullPointerException("targetSupplier is null");
        }
        this.targetSupplier = targetSupplier;
        return this;
    }

    public MBeanAttributeBuilder named(String name)
    {
        if (name == null) {
            throw new NullPointerException("name is null");
        }
        this.name = name;
        return this;
    }

    public MBeanAttributeBuilder withConcreteGetter(Method concreteGetter)
    {
        if (concreteGetter == null) {
            throw new NullPointerException("concreteGetter is null");
        }
        if (!isValidGetter(concreteGetter)) {
            throw new IllegalArgumentException("Method is not a valid getter: " + concreteGetter);
        }
        this.concreteGetter = concreteGetter;
        return this;
    }

    public MBeanAttributeBuilder withAnnotatedGetter(Method annotatedGetter)
    {
        if (annotatedGetter == null) {
            throw new NullPointerException("annotatedGetter is null");
        }
        if (!isValidGetter(annotatedGetter)) {
            throw new IllegalArgumentException("Method is not a valid getter: " + annotatedGetter);
        }
        this.annotatedGetter = annotatedGetter;
        return this;
    }

    public MBeanAttributeBuilder withConcreteSetter(Method concreteSetter)
    {
        if (concreteSetter == null) {
            throw new NullPointerException("concreteSetter is null");
        }
        if (!isValidSetter(concreteSetter)) {
            throw new IllegalArgumentException("Method is not a valid setter: " + concreteSetter);
        }
        this.concreteSetter = concreteSetter;
        return this;
    }

    public MBeanAttributeBuilder withAnnotatedSetter(Method annotatedSetter)
    {
        if (annotatedSetter == null) {
            throw new NullPointerException("annotatedSetter is null");
        }
        if (!isValidSetter(annotatedSetter)) {
            throw new IllegalArgumentException("Method is not a valid setter: " + annotatedSetter);
        }
        this.annotatedSetter = annotatedSetter;
        return this;
    }

    public MBeanAttributeBuilder flatten()
    {
        this.flatten = true;
        return this;
    }

    public MBeanAttributeBuilder nested()
    {
        this.nested = true;
        return this;
    }

    public Collection<? extends MBeanFeature> build()
    {
        if (targetSupplier == null) {
            throw new IllegalArgumentException("JmxAttribute must have a target object");
        }

        // Name
        String attributeName = name;
        if (attributeName == null) {
            attributeName = getAttributeName(concreteGetter, concreteSetter, annotatedGetter, annotatedSetter);
        }

        if (flatten || AnnotationUtils.isFlatten(annotatedGetter)) {
            // must have a getter
            if (concreteGetter == null) {
                throw new IllegalArgumentException("Flattened JmxAttribute must have a concrete getter");
            }

            Class nestedObjectType = getNestedObjectType(concreteGetter);
            if (nestedObjectType == null) {
                return ImmutableList.of();
            }
            long cacheDurationMillis = getNestedObjectCacheDuration(annotatedGetter);
            Supplier nestedObjectSupplier = createNestedObjectSupplier(nestedObjectType, concreteGetter, cacheDurationMillis);

            MBean mbean = MBeanBuilder.from(nestedObjectType, nestedObjectSupplier).build();
            ArrayList<MBeanFeature> features = new ArrayList<MBeanFeature>();
            features.addAll(mbean.getAttributes());
            features.addAll(mbean.getOperations());
            return Collections.unmodifiableCollection(features);
        }
        else if (nested || AnnotationUtils.isNested(annotatedGetter)) {
            // must have a getter
            if (concreteGetter == null) {
                throw new IllegalArgumentException("Nested JmxAttribute must have a concrete getter");
            }

            Class nestedObjectType = getNestedObjectType(concreteGetter);
            if (nestedObjectType == null) {
                return ImmutableList.of();
            }
            long cacheDurationMillis = getNestedObjectCacheDuration(annotatedGetter);
            Supplier nestedObjectSupplier = createNestedObjectSupplier(nestedObjectType, concreteGetter, cacheDurationMillis);

            MBean mbean = MBeanBuilder.from(nestedObjectType, nestedObjectSupplier).build();
            ArrayList<MBeanFeature> features = new ArrayList<MBeanFeature>();
            for (MBeanAttribute attribute : mbean.getAttributes()) {
                features.add(new NestedMBeanAttribute(attributeName, attribute));
            }
            for (MBeanOperation operation : mbean.getOperations()) {
                features.add(new NestedMBeanOperation(attributeName, operation));
            }
            return Collections.unmodifiableCollection(features);
        }
        else {
            // We must have a getter or a setter
            if (concreteGetter == null && concreteSetter == null) {
                throw new IllegalArgumentException("JmxAttribute must have a concrete getter or setter method");
            }

            // Type
            Class<?> attributeType;
            if (concreteGetter != null) {
                attributeType = concreteGetter.getReturnType();
            }
            else {
                attributeType = concreteSetter.getParameterTypes()[0];
            }

            // Descriptor
            Descriptor descriptor = null;
            if (annotatedGetter != null) {
                descriptor = AnnotationUtils.buildDescriptor(annotatedGetter);
            }
            if (annotatedSetter != null) {
                Descriptor setterDescriptor = AnnotationUtils.buildDescriptor(annotatedSetter);
                if (descriptor == null) {
                    descriptor = setterDescriptor;
                }
                else {
                    descriptor = ImmutableDescriptor.union(descriptor, setterDescriptor);
                }
            }

            // Description
            String description = AnnotationUtils.getDescription(descriptor, annotatedGetter, annotatedSetter);

            MBeanAttributeInfo mbeanAttributeInfo = new MBeanAttributeInfo(
                    attributeName,
                    attributeType.getName(),
                    description,
                    concreteGetter != null,
                    concreteSetter != null,
                    concreteGetter != null && concreteGetter.getName().startsWith("is"),
                    descriptor);


            return Collections.singleton(new ReflectionMBeanAttribute(mbeanAttributeInfo, targetSupplier, concreteGetter, concreteSetter));
        }
    }

    private static String getAttributeName(Method... methods)
    {
        for (Method method : methods) {
            if (method != null) {
                Matcher matcher = getterOrSetterPattern.matcher(method.getName());
                if (matcher.matches()) {
                    return matcher.group(2);
                }
            }
        }

        // just use the name of the first non-null method
        for (Method method : methods) {
            if (method != null) {
                return method.getName();
            }
        }
        return null;
    }

    private Class getNestedObjectType(Method concreteGetter)
    {
        try {
            Object value = concreteGetter.invoke(targetSupplier.get());
            return value.getClass();
        }
        catch (Exception e) {
            // todo log me
            return null;
        }
    }

    private long getNestedObjectCacheDuration(Method annotatedGetter)
    {
        long cacheDurationMillis = -1;
        Nested nestedAnnotation = AnnotationUtils.findAnnotation(Nested.class, annotatedGetter);
        if (nestedAnnotation != null) {
            cacheDurationMillis = nestedAnnotation.cacheDurationMillis();
        }
        Flatten flattenAnnotation = AnnotationUtils.findAnnotation(Flatten.class, annotatedGetter);
        if (flattenAnnotation != null) {
            cacheDurationMillis = flattenAnnotation.cacheDurationMillis();
        }
        return cacheDurationMillis;
    }

    private Supplier createNestedObjectSupplier(final Class requiredType, final Method concreteGetter, long cacheDurationMills)
    {
        Supplier supplier = new Supplier()
        {
            public Object get()
            {
                try {
                    return requiredType.cast(concreteGetter.invoke(targetSupplier.get()));
                }
                catch (Exception e) {
                    // todo log me
                    return null;
                }
            }
        };
        if (cacheDurationMills >= 0) {
            supplier = Suppliers.memoizeWithExpiration(supplier, cacheDurationMills, TimeUnit.MILLISECONDS);
        } else {
            supplier = Suppliers.memoize(supplier);
        }
        return supplier;
    }
}
