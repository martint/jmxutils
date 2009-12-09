package org.weakref.jmx;

import static org.testng.Assert.assertEquals;
import static org.weakref.jmx.ObjectNames.generatedNameOf;

import java.lang.annotation.Annotation;

import org.testng.annotations.Test;

import com.google.inject.name.Names;

public class TestObjectNames {

  @interface Ann {}
  
  static class AnnImpl implements Ann {
    public Class<? extends Annotation> annotationType() {
      return Ann.class;
    }  
  }
  
  static class Inner {}
  
  @Test
  public void testGeneratedNameOf1() {
    assertEquals(
        generatedNameOf(SimpleObject.class), 
        "org.weakref.jmx:name=SimpleObject");
  }
  
  @Test
  public void testGeneratedNameOf2() {
    assertEquals(
        generatedNameOf(SimpleObject.class, Names.named("1")), 
        "org.weakref.jmx:type=SimpleObject,name=1");
  }
  
  @Test
  public void testGeneratedNameOf3() {
    assertEquals(
        generatedNameOf(SimpleObject.class, Ann.class), 
        "org.weakref.jmx:type=SimpleObject,name=Ann");
  }

  @Test
  public void testGeneratedNameOf4() {
    assertEquals(
        generatedNameOf(SimpleObject.class, new AnnImpl()), 
        "org.weakref.jmx:type=SimpleObject,name=Ann");
  }
  
  @Test
  public void testGeneratedNameOf5() {
    assertEquals(
        generatedNameOf(Inner.class), 
        "org.weakref.jmx:name=Inner");
  }
}
