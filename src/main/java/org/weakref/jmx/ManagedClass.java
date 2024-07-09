/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.weakref.jmx;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static org.weakref.jmx.ReflectionUtils.getAttributeName;
import static org.weakref.jmx.ReflectionUtils.isGetter;

public class ManagedClass
{
    private static final Map<Class<?>, ManagedAttributeCacheEntry> managedClassDefinitionCache = new ConcurrentHashMap<>();

    private final WeakReference<Object> target;
    private final Map<String, ManagedClass> children;
    private final Map<String, ManagedAttribute> attributes;

    private ManagedClass(Object target, Map<String, ManagedClass> children, Map<String, ManagedAttribute> attributes)
    {
        this.target = new WeakReference<>(target);
        this.children = children;
        this.attributes = attributes;
    }

    public static ManagedClass fromExportedObject(Object target)
    {
        Class<?> targetClass = target.getClass();
        ImmutableMap.Builder<String, ManagedClass> children = ImmutableMap.builder();
        ManagedAttributeCacheEntry managedAttributeCacheEntry = computeManagedClassAttributeDefinitions(targetClass);
        for (ManagedAttributeDefinition attributeDefinition : managedAttributeCacheEntry.managedAttributeDefinitions()) {
            if (attributeDefinition.isAnnotatedMethodNestedOrFlatten()) {
                ManagedAttribute managedAttribute = attributeDefinition.managedAttribute();
                try {
                    Object childTarget = managedAttribute.getMethod().invoke(target);
                    if (childTarget != null) {
                        children.put(managedAttribute.getName(), fromExportedObject(childTarget));
                    }
                }
                catch (ReflectiveOperationException e) {
                    // Ignore and continue
                }
            }
        }
        return new ManagedClass(target, children.build(), managedAttributeCacheEntry.attributes());
    }

    private static ManagedAttributeCacheEntry computeManagedClassAttributeDefinitions(Class<?> targetClass)
    {
        return managedClassDefinitionCache.computeIfAbsent(targetClass, clazz -> {
            ImmutableList.Builder<ManagedAttributeDefinition> builder = ImmutableList.builder();
            for (Map.Entry<Method, Method> entry : AnnotationUtils.findManagedMethods(targetClass).entrySet()) {
                Method concreteMethod = entry.getKey();
                Method annotatedMethod = entry.getValue();

                if (isGetter(concreteMethod)) { // is it an attribute?

                    String attributeName = AnnotationUtils.getName(annotatedMethod);
                    String description = AnnotationUtils.getDescription(annotatedMethod);

                    if (attributeName == null || attributeName.equals("")) {
                        attributeName = getAttributeName(concreteMethod);
                    }

                    builder.add(new ManagedAttributeDefinition(new ManagedAttribute(concreteMethod, attributeName, description, AnnotationUtils.isFlatten(concreteMethod)), AnnotationUtils.isNested(annotatedMethod) || AnnotationUtils.isFlatten(annotatedMethod)));
                }
            }
            List<ManagedAttributeDefinition> attributeDefinitions = builder.build();
            Map<String, ManagedAttribute> attributes = attributeDefinitions.stream().collect(toImmutableMap(attributeDefinition -> attributeDefinition.managedAttribute().getName(), ManagedAttributeDefinition::managedAttribute));
            return new ManagedAttributeCacheEntry(attributeDefinitions, attributes);
        });
    }

    public Class getTargetClass()
    {
        return getTarget().getClass();
    }

    public Object getTarget()
    {
        Object reference = target.get();
        if (reference == null) {
            throw new IllegalStateException("Reference to target is no longer present.");
        }
        return reference;
    }

    public Map<String, ManagedClass> getChildren()
    {
        if (target.get() == null) {
            return Collections.emptyMap();
        }
        return children;
    }

    public Set<String> getAttributeNames()
    {
        if (target.get() == null) {
            return Collections.emptySet();
        }
        return ImmutableSet.copyOf(attributes.keySet());
    }

    public Object invokeAttribute(String attributeName)
            throws InvocationTargetException, IllegalAccessException
    {
        return getManagedAttribute(attributeName).getMethod().invoke(getTarget());
    }

    public String getAttributeDescription(String attributeName)
    {
        return getManagedAttribute(attributeName).getDescription();
    }

    public boolean isAttributeFlatten(String attributeName)
    {
        return getManagedAttribute(attributeName).isFlatten();
    }

    private ManagedAttribute getManagedAttribute(String attributeName)
    {
        ManagedAttribute managedAttribute = attributes.get(attributeName);
        if (managedAttribute == null) {
            throw new IllegalArgumentException("No attribute with name " + attributeName + " is registered with this managed class");
        }
        return managedAttribute;
    }

    private record ManagedAttributeDefinition(ManagedAttribute managedAttribute, boolean isAnnotatedMethodNestedOrFlatten) {}

    private record ManagedAttributeCacheEntry(List<ManagedAttributeDefinition> managedAttributeDefinitions, Map<String, ManagedAttribute> attributes) {}
}
