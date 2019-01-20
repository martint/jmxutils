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
package org.weakref.jmx.guice;

import com.google.inject.Key;
import org.weakref.jmx.ObjectNameGenerator;

import java.util.function.Function;

class Mapping
{
    private final Function<ObjectNameGenerator, String> nameFactory;
    private final Key<?> key;

    Mapping(Function<ObjectNameGenerator, String> nameFactory, Key<?> key)
    {
        this.nameFactory = nameFactory;
        this.key = key;
    }

    public String getName(ObjectNameGenerator objectNameGenerator)
    {
        return nameFactory.apply(objectNameGenerator);
    }

    public Key<?> getKey()
    {
        return key;
    }
}
