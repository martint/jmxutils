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

import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.Map;

public abstract class TestInheritanceBase
{
    protected final Class target;
    protected final Class source;

    /**
     * @param target class to resolve
     * @param source class providing the annotation
     */
    TestInheritanceBase(Class target, Class source)
    {
        this.target = target;
        this.source = source;
    }

    public Class getTargetClass()
    {
        return target;
    }

    public Method getTargetMethod() throws NoSuchMethodException
    {
        return target.getMethod("method");
    }

    public Managed expected() throws NoSuchMethodException
    {
        return source.getDeclaredMethod("method").getAnnotation(Managed.class);
    }

    @Test
    public void testResolver() throws NoSuchMethodException
    {
        AnnotationFinder resolver = new AnnotationFinder();
        Map<Method, Managed> map = resolver.findAnnotatedMethods(getTargetClass());
        Managed annotation = map.get(getTargetMethod());
        Assert.assertEquals(annotation, expected());
    }
}
