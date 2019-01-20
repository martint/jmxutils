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

// Annotation inherited from parent of interface and direct interface => E
public class TestInheritance9
        extends TestInheritanceBase
{
    public TestInheritance9()
    {
        super(B.class, E.class);
    }

    private static class B
            implements C, D
    {
        @Override
        public Object method()
        {
            return null;
        }
    }

    private interface C
        extends E
    {
        @Override
        Object method();
    }

    private interface E
    {
        @Managed(description = "C")
        Object method();
    }

    private interface D
    {
        @Managed(description = "D")
        Object method();
    }

}