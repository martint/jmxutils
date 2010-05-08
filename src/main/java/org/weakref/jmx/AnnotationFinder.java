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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

class AnnotationFinder
{
    public Map<Method, Annotation> findAnnotatedMethods(Class<?> clazz)
    {
        Map<Method, Annotation> result = new HashMap<Method, Annotation>();

        // gather all publicly available methods
        // this returns everything, even if it's declared in a parent
        for (Method method : clazz.getMethods()) {
            if (method.isSynthetic() || method.isBridge()) {
                continue;
            }

            // look for annotations recursively in superclasses or interfaces
            Annotation annotation = findAnnotation(clazz, method.getName(), method.getParameterTypes());
            if (annotation != null) {
                result.put(method, annotation);
            }
        }

        return result;
    }

    private Annotation findAnnotation(Class<?> clazz, String methodName, Class<?>[] paramTypes)
    {
        Annotation annotation = null;
        Method method;
        try {
            method = clazz.getDeclaredMethod(methodName, paramTypes);
            for (Annotation candidate : method.getAnnotations()) {
              if (candidate.annotationType().isAnnotationPresent(ManagedAnnotation.class)) {
                annotation = candidate;
                break;
              }
            }
        }
        catch (NoSuchMethodException e) {
        }

        if (annotation == null && clazz.getSuperclass() != null) {
            annotation = findAnnotation(clazz.getSuperclass(), methodName, paramTypes);
        }

        if (annotation == null) {
            for (Class iface : clazz.getInterfaces()) {
                annotation = findAnnotation(iface, methodName, paramTypes);
                if (annotation != null) {
                    break;
                }
            }
        }

        return annotation;
    }
}
