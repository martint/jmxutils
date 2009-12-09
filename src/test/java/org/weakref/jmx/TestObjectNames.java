package org.weakref.jmx;

import static org.testng.Assert.assertEquals;
import static org.weakref.jmx.ObjectNames.singletonNameOf;

import org.testng.annotations.Test;

public class TestObjectNames {

  @Test
  public void testOfSingleton() {
    assertEquals(singletonNameOf(SimpleObject.class), "org.weakref.jmx:name=SimpleObject");
  }
}
