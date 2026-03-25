package org.weakref.jmx;

import com.google.inject.name.Names;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import java.lang.annotation.Annotation;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.weakref.jmx.ObjectNames.generatedNameOf;

public class TestObjectNames {

  @interface Ann {}
  
  static class AnnImpl implements Ann {
    @Override
    public Class<? extends Annotation> annotationType() {
      return Ann.class;
    }  
  }
  
  static class Inner {}
  
  @Test
  public void testGeneratedNameOf1() {
    assertThat(generatedNameOf(SimpleObject.class))
        .isEqualTo("org.weakref.jmx:name=SimpleObject");
  }
  
  @Test
  public void testGeneratedNameOf2() {
    assertThat(generatedNameOf(SimpleObject.class, Names.named("1")))
        .isEqualTo("org.weakref.jmx:type=SimpleObject,name=1");
  }
  
  @Test
  public void testGeneratedNameOf3() {
    assertThat(generatedNameOf(SimpleObject.class, Ann.class))
        .isEqualTo("org.weakref.jmx:type=SimpleObject,name=Ann");
  }

  @Test
  public void testGeneratedNameOf4() {
    assertThat(generatedNameOf(SimpleObject.class, new AnnImpl()))
        .isEqualTo("org.weakref.jmx:type=SimpleObject,name=Ann");
  }
  
  @Test
  public void testGeneratedNameOf5() {
    assertThat(generatedNameOf(Inner.class))
        .isEqualTo("org.weakref.jmx:name=Inner");
  }

   @Test
   public void testGeneratedNameOfStringWithQuoting() {
     assertThat(generatedNameOf(SimpleObject.class, "bar,baz"))
        .isEqualTo("org.weakref.jmx:type=SimpleObject,name=\"bar,baz\"");
   }

  @ParameterizedTest
  @MethodSource("getNames")
  public void testQuotesName(String name, boolean shouldQuote)
          throws MalformedObjectNameException
  {
      ObjectName objectName = ObjectName.getInstance(generatedNameOf(SimpleObject.class, Names.named(name)));
      if (shouldQuote) {
          String quotedName = objectName.getKeyProperty("name");
          int index = 0;
          StringBuilder builder = new StringBuilder();
          assertThat(quotedName.charAt(index++)).isEqualTo('\"');
          char c;
          while ((c = quotedName.charAt(index++)) != '\"') {
              if (c == '\\') {
                  c = quotedName.charAt(index++);
                  assertThat("*?n\\\"".indexOf(c) != -1).describedAs("valid character '" + c + "' after backslash").isTrue();
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
          assertThat(index).isEqualTo(quotedName.length());
          assertThat(builder.toString()).isEqualTo(name);
      }
      else {
          assertThat(objectName.getKeyProperty("name")).isEqualTo(name);
      }
  }

  public static Object[][] getNames()
  {
      ArrayList<Object[]> names = new ArrayList<>();
      for (char c = 0; c < 500; ++c) {
          names.add(new Object[] {String.valueOf(c), ",=:*?\"\n".indexOf(c) != -1});
      }
      names.add(new Object[] {":\\", true});
      return names.toArray(new Object[][]{});
  }
}
