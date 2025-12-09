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

import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.RuntimeErrorException;
import javax.management.RuntimeOperationsException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Collections;

import static java.util.Objects.requireNonNull;

final class ReflectionUtils
{
    private ReflectionUtils()
    {
    }

    private static final Map<Class<?>, Class<?>> primitiveToWrapper;

    static {
        Map<Class<?>, Class<?>> map = new LinkedHashMap<>();
        map.put(boolean.class, Boolean.class);
        map.put(char.class, Character.class);
        map.put(byte.class, Byte.class);
        map.put(short.class, Short.class);
        map.put(int.class, Integer.class);
        map.put(long.class, Long.class);
        map.put(float.class, Float.class);
        map.put(double.class, Double.class);
        primitiveToWrapper = Collections.unmodifiableMap(map);
    }

    public static Object invoke(Object target, Method method, Object... params)
            throws MBeanException, ReflectionException
    {
        requireNonNull(target, "target is null");
        requireNonNull(method, "method is ull");
        requireNonNull(params, "params is null");

        try {
            return method.invoke(target, params);
        }
        catch (InvocationTargetException e) {
            // unwrap exception
            Throwable targetException = e.getTargetException();
            if (targetException instanceof RuntimeException runtimeException) {
                throw new MBeanException(
                        runtimeException,
                        "RuntimeException occurred while invoking " + toSimpleName(method));
            }
            else if (targetException instanceof ReflectionException exception) {
                // allow ReflectionException to passthrough
                throw exception;
            }
            else if (targetException instanceof MBeanException exception) {
                // allow MBeanException to passthrough
                throw exception;
            }
            else if (targetException instanceof Exception exception) {
                throw new MBeanException(
                        exception,
                        "Exception occurred while invoking " + toSimpleName(method));
            }
            else if (targetException instanceof Error error) {
                throw new RuntimeErrorException(
                        error,
                        "Error occurred while invoking " + toSimpleName(method));
            }
            else {
                throw new RuntimeErrorException(
                        new AssertionError(targetException),
                        "Unexpected throwable occurred while invoking " + toSimpleName(method));
            }
        }
        catch (RuntimeException e) {
            throw new RuntimeOperationsException(e, "RuntimeException occurred while invoking " + toSimpleName(method));
        }
        catch (IllegalAccessException e) {
            throw new ReflectionException(e, "IllegalAccessException occurred while invoking " + toSimpleName(method));
        }
        catch (Error err) {
            throw new RuntimeErrorException(err, "Error occurred while invoking " + toSimpleName(method));
        }
        catch (Exception e) {
            throw new ReflectionException(e, "Exception occurred while invoking " + toSimpleName(method));
        }
    }

    private static String toSimpleName(Method method)
    {
        return new Signature(method).toString();
    }

    public static boolean isGetter(Method method)
    {
        String methodName = method.getName();
        return (methodName.startsWith("get") || methodName.startsWith("is")) && isValidGetter(method);
    }

    public static boolean isSetter(Method method)
    {
        return method.getName().startsWith("set") && isValidSetter(method);
    }

    public static String getAttributeName(Method method)
    {
        String name = method.getName();

        if (name.startsWith("is")) {
            return name.substring(2);
        }

        if (name.startsWith("get") || name.startsWith("set")) {
            return name.substring(3);
        }

        throw new IllegalArgumentException("method does not represent a getter or setter");
    }

    public static boolean isValidGetter(Method getter)
    {
        requireNonNull(getter, "getter is null");
        return getter.getParameterCount() == 0 && !getter.getReturnType().equals(Void.TYPE);
    }

    public static boolean isValidSetter(Method setter)
    {
        requireNonNull(setter, "setter is null");
        return setter.getParameterCount() == 1;
    }

    public static boolean isAssignable(Object value, Class<?> type)
    {
        if (type.isPrimitive()) {
            return primitiveToWrapper.get(type).isInstance(value);
        }

        return value == null || type.isInstance(value);
    }
}
