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

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.management.Descriptor;
import javax.management.DescriptorKey;
import javax.management.ImmutableDescriptor;

import org.weakref.jmx.JmxException.Reason;

import static java.util.Arrays.asList;

final class AnnotationUtils
{
    private AnnotationUtils()
    {
    }

    public static Descriptor buildDescriptor(Method annotatedMethod)
    {
        return buildDescriptor(annotatedMethod.getAnnotations());
    }

    public static Descriptor buildDescriptor(Annotation... annotations)
    {
        Map<String, Object> fields = new TreeMap<String, Object>();

        // process all direct annotations
        for (Annotation annotation : computeWalkSequence(annotations)) {
            processAnnotation(annotation, fields);
        }

        return new ImmutableDescriptor(fields);
    }

    public static List<Annotation> computeWalkSequence(Annotation... annotations)
    {
        Set<Annotation> seen = new HashSet<Annotation>();
        List<Annotation> result = new ArrayList<Annotation>();

        computeWalkSequence(seen, result, annotations);

        return new ArrayList<Annotation>(result);
    }

    private static void computeWalkSequence(Set<Annotation> seen, List<Annotation> result, Annotation... annotations)
    {
        seen.addAll(asList(annotations));

        for (Annotation annotation : annotations) {
            for (Annotation parent : annotation.annotationType().getAnnotations()) {
                if (!seen.contains(parent)) {
                    computeWalkSequence(seen, result, parent);
                }
            }
        }

        Collections.addAll(result, annotations);
    }

    private static void processAnnotation(Annotation annotation, Map<String, Object> fieldsCollector)
    {
        // for each field in the annotation
        for (Method field : annotation.annotationType().getMethods()) {
            // if the field is annotated with the descriptor key
            DescriptorKey descriptorKey = field.getAnnotation(DescriptorKey.class);
            if (descriptorKey == null) {
                continue;
            }

            // name is the name of the method
            String name = descriptorKey.value();

            // invoke method to get the value
            Object value;
            try {
                value = field.invoke(annotation);
            }
            catch (Exception e) {
                Throwable cause = e;
                if (e instanceof InvocationTargetException) {
                    cause = e.getCause();
                }
                throw new JmxException(Reason.INVALID_ANNOTATION, cause,
                        "Unexpected exception getting value from @DescriptorKey field type: annotationClass=%s, field=%s",
                        annotation.annotationType().getName(), field.getName());
            }

            // skip null values, since that is the default
            if (value == null) {
                continue;
            }

            // Convert Class and Enum value or array value to String or String array
            // see DescriptorKey javadocs for more info
            if (value instanceof Class) {
                value = ((Class<?>) value).getName();
            }
            else if (value instanceof Enum) {
                value = ((Enum<?>) value).name();
            }
            else if (value.getClass().isArray()) {
                Class<?> componentType = value.getClass().getComponentType();
                if (Class.class.equals(componentType)) {
                    Class<?>[] classArray = (Class<?>[]) value;
                    String[] stringArray = new String[classArray.length];
                    for (int i = 0; i < classArray.length; i++) {
                        if (classArray[i] != null) {
                            stringArray[i] = classArray[i].getName();
                        }
                    }
                    value = stringArray;
                }
                else if (componentType.isEnum()) {
                    Enum<?>[] enumArray = (Enum<?>[]) value;
                    String[] stringArray = new String[enumArray.length];
                    for (int i = 0; i < enumArray.length; i++) {
                        if (enumArray[i] != null) {
                            stringArray[i] = enumArray[i].name();
                        }
                    }
                    value = stringArray;
                }
            }
            else if (value instanceof Annotation) {
                throw new JmxException(Reason.INVALID_ANNOTATION,
                        "@DescriptorKey can not be applied to an annotation field type: annotationClass=%s, field=%s",
                        annotation.annotationType().getName(),
                        field.getName());
            }

            fieldsCollector.put(name, value);
        }
    }

    public static String getDescription(Descriptor descriptor, Method... annotatedMethods)
    {
        // First, check for a description method
        for (Method annotatedMethod : annotatedMethods) {
            if (annotatedMethod != null) {
                String description = getDescription(annotatedMethod);
                if (description != null) {
                    return description;
                }
            }
        }

        // If that didn't work, look for one in the descriptor object
        Object descriptionValue = descriptor.getFieldValue("description");
        if (descriptionValue instanceof String) {
            return (String) descriptionValue;
        }
        return null;
    }

    public static String getDescription(Descriptor descriptor, Annotation... annotatedMethod)
    {
        // First, check for a description method
        String description = getDescription(annotatedMethod);
        if (description != null) {
            return description;
        }

        // If that didn't work, look for one in the descriptor object
        Object descriptionValue = descriptor.getFieldValue("description");
        if (descriptionValue instanceof String) {
            return (String) descriptionValue;
        }
        return null;
    }

    public static String getDescription(Method annotatedMethod)
    {
        return getDescription(annotatedMethod.getAnnotations());
    }

    public static String getDescription(Annotation... annotations)
    {
        String description = "";
        for (Annotation annotation : annotations) {
            try {
                Method descriptionMethod = annotation.annotationType().getMethod("description");
                description = descriptionMethod.invoke(annotation).toString();
            }
            catch (InvocationTargetException e) {
                // ignore
            }
            catch (NoSuchMethodException e) {
                // ignore
            }
            catch (IllegalAccessException e) {
                // ignore
            }
        }
        return description;
    }

    /**
     * Find methods that are tagged as managed somewhere in the hierarchy
     *
     * @param clazz the class to analyze
     * @return a map that associates a concrete method to the actual method tagged as managed
     * (which may belong to a different class in clazz's hierarchy)
     */
    public static Map<Method, Method> findManagedMethods(Class<?> clazz)
    {
        Map<Method, Method> result = new HashMap<Method, Method>();

        // gather all publicly available methods
        // this returns everything, even if it's declared in a parent
        for (Method method : clazz.getMethods()) {
            // skip methods that are used internally by the vm for implementing covariance, etc
            if (method.isSynthetic() || method.isBridge()) {
                continue;
            }

            // look for annotations recursively in superclasses or interfaces
            Method managedMethod = findManagedMethod(clazz, method.getName(), method.getParameterTypes());
            if (managedMethod != null) {
                result.put(method, managedMethod);
            }
        }

        return result;
    }

    public static Method findManagedMethod(Method method)
    {
        return findManagedMethod(method.getDeclaringClass(), method.getName(), method.getParameterTypes());
    }

    public static Method findManagedMethod(Class<?> clazz, String methodName, Class<?>[] paramTypes)
    {
        try {
            Method method = clazz.getDeclaredMethod(methodName, paramTypes);
            if (isManagedMethod(method)) return method;
            }
        catch (NoSuchMethodException e) {
            // ignore
        }

        if (clazz.getSuperclass() != null) {
            Method managedMethod = findManagedMethod(clazz.getSuperclass(), methodName, paramTypes);
            if (managedMethod != null) {
                return managedMethod;
            }
        }

        for (Class<?> iface : clazz.getInterfaces()) {
            Method managedMethod = findManagedMethod(iface, methodName, paramTypes);
            if (managedMethod != null) {
                return managedMethod;
            }
        }

        return null;
    }

    public static boolean isManagedMethod(Method method)
    {
        for (Annotation annotation : method.getAnnotations()) {
            if (annotation.annotationType().isAnnotationPresent(ManagedAnnotation.class)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isFlatten(Method method)
    {
        return findAnnotation(Flatten.class, method) != null;
    }

    public static boolean isNested(Method method)
    {
        return findAnnotation(Nested.class, method) != null;
    }

    public static <T extends Annotation> T findAnnotation(Class<T> annotationClass, Method method)
    {
        if (method != null) {
            return findAnnotation(annotationClass, new HashSet<Class<? extends Annotation>>(), method.getAnnotations());
        }
        return null;
    }

    private static <T extends Annotation> T findAnnotation(Class<T> annotationClass, Set<Class<? extends Annotation>> processedTypes, Annotation... annotations)
    {
        // are any of the annotations the specified annotation
        for (Annotation annotation : annotations) {
            if (annotationClass.isInstance(annotation)) {
                return annotationClass.cast(annotation);
            }
        }

        // are any of the annotations annotated with the specified annotation
        for (Annotation annotation : annotations) {
            if (processedTypes.add(annotation.annotationType())) {
                T foundAnnotation = findAnnotation(annotationClass, processedTypes, annotation.annotationType().getAnnotations());
                if (foundAnnotation != null) {
                    return foundAnnotation;
                }
            }
        }

        return null;
    }
}
