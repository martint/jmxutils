/**
 *  Copyright 2010 Henning Schmiedehausen
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

import java.util.HashSet;
import java.util.Set;

import javax.management.MBeanServer;

/**
 * An MBeanExporter that keeps track of all the MBeans it has exported. It can remove all the MBeans that it knows off in one go, e.g. for shutting down a service.
 */
public class RecordingMBeanExporter extends MBeanExporter
{
	private final Set<String> exportedObjects = new HashSet<String>();

	public RecordingMBeanExporter(final MBeanServer server)
	{
		super(server);
	}

	@Override
	public void export(final String name, final Object object)
	{
		synchronized(exportedObjects) {
			exportedObjects.add(name);
			super.export(name, object);
		}
	}

	@Override
	public void unexport(final String name)
	{
		synchronized(exportedObjects) {
			super.unexport(name);
			exportedObjects.remove(name);
		}
	}

	/**
	 * Unexports all MBeans that have been exported through this MBeanExporter.
	 */
	public void unexportAll()
	{
		synchronized(exportedObjects) {
			for (String jmxName : exportedObjects) {
				super.unexport(jmxName);
			}
			exportedObjects.clear();
		}
	}
}