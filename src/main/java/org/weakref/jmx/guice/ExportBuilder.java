package org.weakref.jmx.guice;

import com.google.inject.multibindings.Multibinder;

public class ExportBuilder {
	private final Multibinder<Mapping> binder; 
	
	ExportBuilder(Multibinder<Mapping> binder) {
		this.binder = binder;
	}

	public AnnotatedExportBuilder export(Class<?> clazz) {
		return new AnnotatedExportBuilder(binder, clazz);
	}
}
