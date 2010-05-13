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

import static org.weakref.jmx.ObjectNames.generatedNameOf;

import java.lang.annotation.Annotation;

import com.google.inject.name.Named;
import org.weakref.jmx.ObjectNames;

import com.google.inject.Key;
import com.google.inject.multibindings.Multibinder;

public class NamedBindingBuilder
{
    protected final Multibinder<Mapping> binder;
    protected final Key<?> key;

    NamedBindingBuilder(Multibinder<Mapping> binder, Key<?> key)
    {
        this.binder = binder;
        this.key = key;
    }

    /**
     * Names the MBean according to {@link ObjectNames} name generator methods.
     */
    public void withGeneratedName() 
    {
        if (key.getAnnotation() != null) {
            if (key.getAnnotation() instanceof Named) {
                as(generatedNameOf(key.getTypeLiteral().getRawType(), (Named) key.getAnnotation()));
            }
            else {
                as(generatedNameOf(key.getTypeLiteral().getRawType(), key.getAnnotation()));
            }
        }
        else if (key.getAnnotationType() != null) {
            as(generatedNameOf(key.getTypeLiteral().getRawType(), key.getAnnotationType()));
        }
        else {
            as(generatedNameOf(key.getTypeLiteral().getRawType()));
        }
    }

    public void as(String name)
    {
        binder.addBinding().toInstance(new Mapping(name, key));
    }
}
