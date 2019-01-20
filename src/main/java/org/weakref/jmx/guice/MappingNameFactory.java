package org.weakref.jmx.guice;

import org.weakref.jmx.ObjectNameGenerator;

interface MappingNameFactory
{
    String getName(ObjectNameGenerator objectNameGenerator);
}
