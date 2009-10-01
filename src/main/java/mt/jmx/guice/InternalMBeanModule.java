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
package mt.jmx.guice;

import com.google.inject.AbstractModule;

/**
 * This module has to get installed only once, so equals & hashCode are implemented
 * to test for class identity
 */
final class InternalMBeanModule
        extends AbstractModule
{
    @Override
    protected void configure()
    {
        bind(GuiceMBeanExporter.class).asEagerSingleton();
    }

    @Override
    public boolean equals(Object o)
    {
        // Is only ever installed internally, so we don't need to check state.
        return o instanceof InternalMBeanModule;
    }

    @Override
    public int hashCode()
    {
        return InternalMBeanModule.class.hashCode();
    }
}
