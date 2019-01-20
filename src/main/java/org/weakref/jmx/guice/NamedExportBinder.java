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
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Named;
import org.weakref.jmx.ObjectNameGenerator;

import java.util.function.Function;

public class NamedExportBinder
{
    protected final Multibinder<Mapping> binder;
    protected final Key<?> key;

    NamedExportBinder(Multibinder<Mapping> binder, Key<?> key)
    {
        this.binder = binder;
        this.key = key;
    }

    /**
     * Names the MBean according to {@link org.weakref.jmx.ObjectNames} name generator methods.
     */
    public void withGeneratedName()
    {
        if (key.getAnnotation() != null) {
            if (key.getAnnotation() instanceof Named) {
                as(factory -> factory.generatedNameOf(key.getTypeLiteral().getRawType(), ((Named) key.getAnnotation()).value()));
            }
            else {
                as(factory -> factory.generatedNameOf(key.getTypeLiteral().getRawType(), key.getAnnotation().annotationType().getSimpleName()));
            }
        }
        else if (key.getAnnotationType() != null) {
            as(factory -> factory.generatedNameOf(key.getTypeLiteral().getRawType(), key.getAnnotationType().getSimpleName()));
        }
        else {
            as(factory -> factory.generatedNameOf(key.getTypeLiteral().getRawType()));
        }
    }

    public void as(String name)
    {
        as(factory -> name);
    }

    public void as(Function<ObjectNameGenerator, String> nameFactory)
    {
        binder.addBinding().toInstance(new Mapping(nameFactory, key));
    }
}
