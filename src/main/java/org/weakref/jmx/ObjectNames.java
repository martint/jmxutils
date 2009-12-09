package org.weakref.jmx;

import static java.lang.String.format;

public class ObjectNames {

  /**
   * Produce a standardized JMX object name assuming the instance of clazz 
   * will be a singleton.
   * 
   * @param clazz
   * @return JMX object name of the form "[package name]:name=[class name]"
   */
  public static String singletonNameOf(Class<?> clazz) {
    return format("%s:name=%s", 
        clazz.getPackage().getName(),
        clazz.getSimpleName());
  }
}
