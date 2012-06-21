package org.weakref.jmx.guice;

import com.google.inject.Key;
import com.google.inject.multibindings.Multibinder;

@Deprecated
public class ExportBuilder
{
	private final Multibinder<Mapping> binder; 
	
	ExportBuilder(Multibinder<Mapping> binder)
    {
		this.binder = binder;
	}

	public AnnotatedExportBuilder export(Class<?> clazz)
    {
		return new AnnotatedExportBuilder(binder, Key.get(clazz));
	}

    public NamedBindingBuilder export(Key<?> key)
    {
        return new NamedBindingBuilder(binder, key);
    }
}
