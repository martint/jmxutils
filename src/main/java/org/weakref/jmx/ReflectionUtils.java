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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Collections;

final class ReflectionUtils
{
    private ReflectionUtils()
    {
    }

    private static final Pattern getterOrSetterPattern = Pattern.compile("(get|set|is)(.+)");
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
        assertNotNull(target, "target");
        assertNotNull(target, "method");
        assertNotNull(target, "params");

        try {
            Object result = method.invoke(target, params);
            return result;
        }
        catch (InvocationTargetException e) {
            // unwrap exception
            Throwable targetException = e.getTargetException();
            if (targetException instanceof RuntimeException) {
                throw new MBeanException(
                        (RuntimeException) targetException,
                        "RuntimeException occurred while invoking " + toSimpleName(method));
            }
            else if (targetException instanceof ReflectionException) {
                // allow ReflectionException to passthrough
                throw (ReflectionException) targetException;
            }
            else if (targetException instanceof MBeanException) {
                // allow MBeanException to passthrough
                throw (MBeanException) targetException;
            }
            else if (targetException instanceof Exception) {
                throw new MBeanException(
                        (Exception) targetException,
                        "Exception occurred while invoking " + toSimpleName(method));
            }
            else if (targetException instanceof Error) {
                throw new RuntimeErrorException(
                        (Error) targetException,
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
        Matcher matcher = getterOrSetterPattern.matcher(method.getName());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("method does not represent a getter or setter");
        }
        return matcher.group(2);
    }

    public static boolean isValidGetter(Method getter)
    {
        if (getter == null) {
            throw new NullPointerException("getter is null");
        }
        if (getter.getParameterTypes().length != 0) {
            return false;
        }
        if (getter.getReturnType().equals(Void.TYPE)) {
            return false;
        }
        return true;
    }

    public static boolean isValidSetter(Method setter)
    {
        if (setter == null) {
            throw new NullPointerException("setter is null");
        }
        if (setter.getParameterTypes().length != 1) {
            return false;
        }
        return true;
    }

    public static boolean isAssignable(Object value, Class<?> type)
    {
        if (type.isPrimitive()) {
            return primitiveToWrapper.get(type).isInstance(value);
        }
        else {
            return value == null || type.isInstance(value);
        }
    }

    private static void assertNotNull(Object param, String name)
    {
        if (param == null) {
            throw new RuntimeOperationsException(new NullPointerException(name + " is null"));
        }
    }
}
