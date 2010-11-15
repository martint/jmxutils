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

// Annotation inherited from grandparent, with method in parent with same name, different argument types
public class TestInheritance14
        extends TestInheritanceBase
{
    public TestInheritance14()
    {
        super(C.class, A.class);
    }

    private static class A
    {
        @Managed(description="A")
        public Object method(String s)
        {
            return null;
        }
    }

    private static class B
        extends A
    {
        @Managed(description="B")
        public Object method(int i)
        {
            return null;
        }
    }

    private static class C
        extends B
    {
        @Override
        public Object method(String s)
        {
            return null;
        }
    }

    public Method getTargetMethod() throws NoSuchMethodException
    {
        return target.getMethod("method", String.class);
    }

    public Method expected() throws NoSuchMethodException
    {
        return source.getDeclaredMethod("method", String.class);
    }
}
