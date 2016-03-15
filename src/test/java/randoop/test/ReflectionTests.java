package randoop.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import randoop.Globals;
import randoop.main.ClassReader;
import randoop.operation.MethodSignatures;
import randoop.reflection.PublicVisibilityPredicate;
import randoop.reflection.VisibilityPredicate;
import randoop.util.Reflection;

import junit.framework.TestCase;

public class ReflectionTests extends TestCase {

  public void testArgCheckPrimNull() throws Exception {
    assertTrue(Reflection.canBePassedAsArgument(null, Integer.TYPE));
  }

  public void testArgCheckPrimInt() throws Exception {
    assertTrue(Reflection.canBePassedAsArgument(3, Integer.TYPE));
  }

  public void testArgCheckStringInt() throws Exception {
    assertTrue(!Reflection.canBePassedAsArgument("foo", Integer.TYPE));
  }

  public void testArgCheckStringString() throws Exception {
    assertTrue(Reflection.canBePassedAsArgument("foo", String.class));
  }

  public void testArgCheckStringStringArray() throws Exception {
    assertTrue(!Reflection.canBePassedAsArgument("foo", String[].class));
  }

  public void testArgCheckNullStringArray() throws Exception {
    assertTrue(Reflection.canBePassedAsArgument(null, String[].class));
  }

  public void testRelatedClasses1() throws Exception {

    if (System.getProperty("java.vendor").contains("Apple")) {
      // TODO for Apple, number of classes in Reflection package differs. Figure out why
      // and add appropriate test.
      return;
    }

    Set<Class<?>> classes = Reflection.relatedClasses(String.class, 1);
    Set<Class<?>> classes2 = Reflection.relatedClasses(Object.class, 2);

    // Class java.util.Locale.Category was introduced in JDK 7.
    if (System.getProperty("java.version").startsWith("1.6")) {
      assertEquals(46, classes.size());
      assertEquals(61, classes2.size());
    } else if (System.getProperty("java.version").startsWith("1.7")) {
      assertEquals(47, classes.size());
      assertEquals(62, classes2.size());
    } else if (System.getProperty("java.version").startsWith("1.8")) {
      assertEquals(80, classes.size());
      assertEquals(96, classes2.size());
    } else {
      throw new Error("Unrecognized Java version: " + System.getProperty("java.version"));
    }
  }

  public void testLoadClassesFromStream1() throws IOException {
    StringBuilder b = new StringBuilder();
    StringReader r = new StringReader(b.toString());
    BufferedReader br = new BufferedReader(r);

    List<Class<?>> expected = Collections.emptyList();

    assertEquals(expected, ClassReader.getClassesForReader(br, "empty reader"));
    r.close();
    br.close();
  }

  public void testLoadClassesFromStream2() throws IOException {
    StringBuilder b = new StringBuilder();
    b.append("java.lang.String");
    StringReader r = new StringReader(b.toString());
    BufferedReader br = new BufferedReader(r);

    List<Class<String>> expected = Arrays.<Class<String>>asList(java.lang.String.class);

    assertEquals(expected, ClassReader.getClassesForReader(br, "reader with java.lang.String"));
    r.close();
    br.close();
  }

  public void testLoadClassesFromStream3() throws IOException {
    StringBuilder b = new StringBuilder();
    b.append("java.util.List" + Globals.lineSep + "");
    b.append("java.util.AbstractList" + Globals.lineSep + "");
    b.append("java.util.ArrayList" + Globals.lineSep + "");
    b.append("java.lang.Object" + Globals.lineSep + "");
    StringReader r = new StringReader(b.toString());
    BufferedReader br = new BufferedReader(r);

    List<Class<?>> expected =
        Arrays.<Class<?>>asList(
            java.util.List.class, AbstractList.class, ArrayList.class, Object.class);

    assertEquals(
        expected,
        ClassReader.getClassesForReader(br, "reader with List, AbstractList, ArrayList, Object"));
    r.close();
    br.close();
  }

  public void testSignature1() throws Exception {
    Method bhHasModeElements =
        Class.forName("randoop.test.bh.Body$1Enumerate").getMethod("hasMoreElements", new Class[0]);
    assertEquals(
        "randoop.test.bh.Body$1Enumerate.hasMoreElements()",
        MethodSignatures.getSignatureString(bhHasModeElements));

    Method stringLength = String.class.getMethod("length", new Class[0]);
    assertEquals("java.lang.String.length()", MethodSignatures.getSignatureString(stringLength));
  }

  public void testIsPublic1() throws Exception {
    VisibilityPredicate pred = new PublicVisibilityPredicate();
    assertFalse(pred.isVisible(randoop.test.A3.class));
  }

  public void testIsPublic2() throws Exception {
    VisibilityPredicate pred = new PublicVisibilityPredicate();
    Class<?> c = Class.forName("java.lang.String");
    assertTrue(pred.isVisible(c));
  }

  public void testIsPublic3() throws Exception {
    VisibilityPredicate pred = new PublicVisibilityPredicate();
    Class<?> c = Class.forName("java.util.Map$Entry");
    assertTrue(pred.isVisible(c));
  }
}
