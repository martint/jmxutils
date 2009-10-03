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
package mt.jmx;

// Overriden method, inherit annotation from protected parent => A
public class TestInheritance2
    extends TestInheritanceBase
{
    public TestInheritance2()
    {
        super(B.class, A.class);
    }

    private static class A
    {
        @Managed(description="A")
        protected Object method() { return null; }
    }

    private static class B
        extends A
    {
        public Object method() { return null; }
    }


}
