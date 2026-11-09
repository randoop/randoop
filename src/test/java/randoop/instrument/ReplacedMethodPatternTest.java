package randoop.instrument;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.Type;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.Test;
import randoop.main.GenTests;
import randoop.reflection.RawSignature;

/**
 * Tests that the signatures that the replacecall agent gives Randoop, via {@link
 * MethodSignature#toRawSignatureString()}, yield omit patterns that match Randoop's {@link
 * RawSignature} of the replaced method and nothing else.
 */
public class ReplacedMethodPatternTest {

  /** A non-static inner class, whose constructor has an implicit outer-instance parameter. */
  @SuppressWarnings("UnusedVariable") // used reflectively
  private class Inner {
    /**
     * Creates an {@code Inner}.
     *
     * @param entry an entry
     */
    Inner(Map.Entry<?, ?> entry) {}

    /**
     * Does nothing.
     *
     * @param entries some entries
     */
    void entries(Map.Entry<?, ?>[] entries) {}
  }

  @Test
  public void bcelSignaturesMatchRawSignatures() throws Exception {
    assertMatches(java.util.Random.class.getConstructor());
    assertMatches(Math.class.getMethod("max", int.class, int.class));
    assertMatches(String.class.getMethod("valueOf", char[].class));
    assertMatches(Arrays.class.getMethod("asList", Object[].class));
    Class<?> simpleEntry = Class.forName("java.util.AbstractMap$SimpleEntry");
    assertMatches(simpleEntry.getConstructor(Map.Entry.class));
    assertMatches(simpleEntry.getMethod("getKey"));
    assertMatches(
        Inner.class.getDeclaredConstructor(ReplacedMethodPatternTest.class, Map.Entry.class));
    assertMatches(Inner.class.getDeclaredMethod("entries", Map.Entry[].class));
  }

  @Test
  public void defaultReplacementsMatchRawSignatures() throws Exception {
    Map<MethodSignature, MethodSignature> replacementMap;
    String replacementPath = "/default-replacements.txt";
    try (InputStream inputStream = ReplaceCallAgent.class.getResourceAsStream(replacementPath)) {
      assertNotNull(inputStream);
      replacementMap =
          ReplacementFileReader.readReplacements(
              new InputStreamReader(inputStream, UTF_8), replacementPath);
    }
    assertFalse(replacementMap.isEmpty());
    for (MethodSignature replaced : replacementMap.keySet()) {
      Class<?> c =
          Class.forName(
              replaced.getClassname(), false, ReplacedMethodPatternTest.class.getClassLoader());
      Executable executable = findExecutable(c, replaced);
      assertPatternMatches(replaced.toRawSignatureString(), rawSignatureString(c, executable));
    }
  }

  @Test
  public void patternIsAnchored() {
    Pattern pattern = GenTests.signatureToPattern("p.Foo()");
    assertTrue(pattern.matcher("p.Foo()").find());
    assertFalse(pattern.matcher("com.app.p.Foo()").find());
    assertFalse(pattern.matcher("p.Foo().bar()").find());
    pattern = GenTests.signatureToPattern("Foo()");
    assertTrue(pattern.matcher("Foo()").find());
    assertFalse(pattern.matcher("x.Bar.getFoo()").find());
  }

  @Test
  public void patternIsQuoted() {
    Pattern pattern = GenTests.signatureToPattern("p.C.m(int[])");
    assertTrue(pattern.matcher("p.C.m(int[])").find());
    assertFalse(pattern.matcher("pxC.m(int[])").find());
  }

  @Test
  public void dollarMatchesDollarOrDot() {
    // A "$" in a binary name is part of the canonical name if it is part of a class name, as in a
    // top-level class named "Foo$Bar".
    Pattern pattern = GenTests.signatureToPattern("a.Foo$Bar.m(a.Foo$Bar[])");
    assertTrue(pattern.matcher("a.Foo$Bar.m(a.Foo$Bar[])").find());
    assertTrue(pattern.matcher("a.Foo$Bar.m(a.Foo.Bar[])").find());
    assertFalse(pattern.matcher("a.Foo$Bar.m(a.FooxBar[])").find());
  }

  /**
   * Asserts that the agent's signature string for {@code executable}, computed from its class file,
   * yields an omit pattern that matches the {@link RawSignature} of {@code executable}.
   *
   * @param executable a constructor or method
   * @throws IOException if the class file of the declaring class cannot be read
   */
  private static void assertMatches(Executable executable) throws IOException {
    Class<?> c = executable.getDeclaringClass();
    String classfileName = c.getName().replace('.', '/') + ".class";
    JavaClass javaClass;
    try (InputStream in = c.getResourceAsStream("/" + classfileName)) {
      assertNotNull(classfileName, in);
      javaClass = new ClassParser(in, classfileName).parse();
    }
    String name = (executable instanceof Constructor) ? "<init>" : executable.getName();
    Type[] paramTypes = bcelParameterTypes(executable);
    for (org.apache.bcel.classfile.Method m : javaClass.getMethods()) {
      if (m.getName().equals(name) && Arrays.equals(m.getArgumentTypes(), paramTypes)) {
        MethodSignature signature = MethodSignature.of(javaClass.getClassName(), m);
        assertPatternMatches(signature.toRawSignatureString(), rawSignatureString(c, executable));
        return;
      }
    }
    fail("No method in class file for " + executable);
  }

  /**
   * Returns the constructor or method of {@code c} that {@code signature} represents. A method may
   * be declared in a superclass of {@code c}.
   *
   * @param c the class that has the constructor or method
   * @param signature a constructor or method of {@code c}
   * @return the constructor or method of {@code c} that {@code signature} represents
   */
  private static Executable findExecutable(Class<?> c, MethodSignature signature) {
    if (signature.getName().equals("<init>")) {
      Executable result = findExecutable(c.getDeclaredConstructors(), signature);
      if (result != null) {
        return result;
      }
    } else {
      for (Class<?> k = c; k != null; k = k.getSuperclass()) {
        Executable result = findExecutable(k.getDeclaredMethods(), signature);
        if (result != null) {
          return result;
        }
      }
    }
    throw new AssertionError("No constructor or method for " + signature);
  }

  /**
   * Returns the element of {@code candidates} that {@code signature} represents, ignoring the class
   * name in {@code signature}.
   *
   * @param candidates constructors or methods
   * @param signature a constructor or method signature
   * @return the element of {@code candidates} that {@code signature} represents, or null if none
   */
  private static @Nullable Executable findExecutable(
      Executable[] candidates, MethodSignature signature) {
    for (Executable e : candidates) {
      if ((e instanceof Constructor || e.getName().equals(signature.getName()))
          && Arrays.equals(bcelParameterTypes(e), signature.getParameterTypes())) {
        return e;
      }
    }
    return null;
  }

  /**
   * Returns the parameter types of the given constructor or method, as BCEL types.
   *
   * @param executable a constructor or method
   * @return the parameter types of {@code executable}
   */
  private static Type[] bcelParameterTypes(Executable executable) {
    return Arrays.stream(executable.getParameterTypes()).map(Type::getType).toArray(Type[]::new);
  }

  /**
   * Returns the {@link RawSignature#toString()} that Randoop's omit patterns are matched against,
   * for the given constructor or method as a member of {@code c}. This is the signature that {@code
   * TypedClassOperation.getRawSignature()} computes: its class is {@code c}, which differs from the
   * declaring class of an inherited method.
   *
   * @param c the class that has the constructor or method
   * @param executable a constructor or method of {@code c}
   * @return the {@link RawSignature#toString()} of {@code executable} in {@code c}
   */
  private static String rawSignatureString(Class<?> c, Executable executable) {
    String packageName = RawSignature.getPackageName(c.getPackage());
    String classname =
        (packageName == null) ? c.getName() : c.getName().substring(packageName.length() + 1);
    String name = (executable instanceof Constructor) ? classname : executable.getName();
    return new RawSignature(packageName, classname, name, executable.getParameterTypes())
        .toString();
  }

  /**
   * Asserts that the omit pattern for {@code agentSignature} matches {@code rawSignature}.
   *
   * @param agentSignature a signature string from the replacecall agent
   * @param rawSignature a {@link RawSignature#toString()}
   */
  private static void assertPatternMatches(String agentSignature, String rawSignature) {
    assertTrue(
        String.format("pattern for %s does not match %s", agentSignature, rawSignature),
        GenTests.signatureToPattern(agentSignature).matcher(rawSignature).find());
  }
}
