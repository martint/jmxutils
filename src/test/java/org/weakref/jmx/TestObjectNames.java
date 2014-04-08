package org.weakref.jmx;

import com.google.inject.name.Names;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.lang.annotation.Annotation;
import java.util.ArrayList;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.weakref.jmx.ObjectNames.generatedNameOf;

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

   @Test
   public void testGeneratedNameOfStringWithQuoting() {
     assertEquals(
        generatedNameOf(SimpleObject.class, "bar,baz"),
        "org.weakref.jmx:type=SimpleObject,name=\"bar,baz\"");
   }

  @Test(dataProvider = "names")
  public void testQuotesName(String name, boolean shouldQuote)
          throws MalformedObjectNameException
  {
      ObjectName objectName = ObjectName.getInstance(generatedNameOf(SimpleObject.class, Names.named(name)));
      if (shouldQuote) {
          String quotedName = objectName.getKeyProperty("name");
          int index = 0;
          StringBuilder builder = new StringBuilder();
          assertEquals(quotedName.charAt(index++), '\"');
          char c;
          while ((c = quotedName.charAt(index++)) != '\"') {
              if (c == '\\') {
                  c = quotedName.charAt(index++);
                  assertTrue("*?n\\\"".indexOf(c) != -1, "valid character '" + c + "' after backslash");
                  if (c == 'n') {
                      builder.append('\n');
                  }
                  else {
                      builder.append(c);
                  }
              }
              else {
                builder.append(c);
              }
          }
          assertEquals(index, quotedName.length());
          assertEquals(builder.toString(), name);
      }
      else {
          assertEquals(objectName.getKeyProperty("name"), name);
      }
  }

  @DataProvider(name = "names")
  public Object[][] getNames()
  {
      ArrayList<Object[]> names = new ArrayList<Object[]>();
      for (char c = 0; c < 500; ++c) {
          names.add(new Object[] {String.valueOf(c), ",=:*?\"\n".indexOf(c) != -1});
      }
      names.add(new Object[] {":\\", true});
      return names.toArray(new Object[][]{});
  }
}
