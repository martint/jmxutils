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

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.weakref.jmx.Managed;
import org.weakref.jmx.testing.TestingMBeanServer;

import javax.management.MBeanServer;
import javax.management.ObjectName;

public class TestModuleSubclassing
{
    @Test
    public void testSubclassing()
        throws Exception
    {
        final Named name1 = Names.named("bean1");
        final Named name2 = Names.named("bean2");

        final TestingMBeanServer tms = new TestingMBeanServer();

        final Module m1 = new MBeanModule() {
            @Override
            protected void configureMBeans() {
                export(Key.get(Bean.class, name1)).as("bean1:name=bean1");
            }
        };

        final Module m2 = new MBeanModule() {
            @Override
            protected void configureMBeans() {
                export(Key.get(Bean.class, name2)).as("bean2:name=bean2");
            }
        };

        Guice.createInjector(Stage.PRODUCTION,
                             m1,
                             m2,
                             new AbstractModule() {
                                 @Override
                                 public void configure() {
                                     bind(MBeanServer.class).toInstance(tms);
                                     bind(Bean.class).annotatedWith(name1).toInstance(new Bean("name1"));
                                     bind(Bean.class).annotatedWith(name2).toInstance(new Bean("name2"));
                                 }
                             });

        Assert.assertEquals(2, tms.getMBeanCount().intValue());
        Assert.assertNotNull(tms.getMBeanInfo(new ObjectName("bean1:name=bean1")));
        Assert.assertNotNull(tms.getMBeanInfo(new ObjectName("bean2:name=bean2")));
    }

    public static final class Bean
    {
        private final String name;

        Bean(final String name)
        {
            this.name = name;
        }

        @Managed
        public String getName()
        {
            return name;
        }
    }
}

