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

import static org.weakref.jmx.ObjectNames.singletonNameOf;

import com.google.inject.multibindings.Multibinder;
import com.google.inject.Key;

import java.lang.annotation.Annotation;

import org.weakref.jmx.ObjectNames;

public class NamedBindingBuilder
{
    protected final Multibinder<Mapping> binder;
    protected final Class<?> clazz;
    protected final Annotation annotation;
    protected final Class<? extends Annotation> annotationClass;

    NamedBindingBuilder(Multibinder<Mapping> binder, Class<?> clazz)
    {
        this.binder = binder;
        this.clazz = clazz;
        this.annotation = null;
        this.annotationClass = null;
    }

    NamedBindingBuilder(Multibinder<Mapping> binder, Class<?> clazz, Annotation annotation)
    {
        this.binder = binder;
        this.clazz = clazz;
        this.annotation = annotation;
        this.annotationClass = null;
    }

    NamedBindingBuilder(Multibinder<Mapping> binder, Class<?> clazz, Class<? extends Annotation> annotationClass)
    {
        this.binder = binder;
        this.clazz = clazz;
        this.annotation = null;
        this.annotationClass = annotationClass;
    }
    
    /**
     * Names the MBean according to {@link ObjectNames#singletonNameOf(Class)}.
     */
    public void asStandardSingletonName() {
        as(singletonNameOf(clazz));
    }

    public void as(String name)
    {
        Key<?> key;
        if (annotation != null) {
            key = Key.get(clazz, annotation);
        }
        else if (annotationClass != null) {
            key = Key.get(clazz, annotationClass);
        }
        else {
            key = Key.get(clazz);
        }

        binder.addBinding().toInstance(new Mapping(name, key));
    }
}
