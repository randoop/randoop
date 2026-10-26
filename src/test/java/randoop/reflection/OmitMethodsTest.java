package randoop.reflection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static randoop.reflection.AccessibilityPredicate.IS_PUBLIC;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.signature.qual.ClassGetName;
import org.junit.Test;
import randoop.condition.RandoopSpecificationError;
import randoop.main.ThrowClassNameError;
import randoop.operation.TypedOperation;
import randoop.reflection.omitinputs.p.M1Interface;
import randoop.types.ClassOrInterfaceType;

/**
 * Tests for omit-methods filtering.
 *
 * <p>The classes under test, in packages {@code randoop.reflection.omitinputs.p} and {@code
 * randoop.reflection.omitinputs.q}, are:
 *
 * <pre>
 * M1Interface       declares m1()
 *   E               declares m1(), m2()
 *   G               declares m1(), m2()
 *     P             declares m2()
 *       C
 *     q.D
 * </pre>
 */
public class OmitMethodsTest {

  /** The names of the classes under test. */
  private static final List<@ClassGetName String> CLASSNAMES =
      Arrays.asList(
          "randoop.reflection.omitinputs.p.M1Interface",
          "randoop.reflection.omitinputs.p.E",
          "randoop.reflection.omitinputs.p.G",
          "randoop.reflection.omitinputs.p.P",
          "randoop.reflection.omitinputs.p.C",
          "randoop.reflection.omitinputs.q.D");

  /** The package of the classes under test, as a regular expression. */
  private static final String P = "^randoop\\.reflection\\.omitinputs\\.p\\.";

  @Test
  public void testNoOmission() throws Exception {
    Collection<TypedOperation> operations = createModel(CLASSNAMES, null).getOperations();
    assertHasMethod("M1Interface.m1", operations);
    assertHasMethod("E.m1", operations);
    assertHasMethod("E.m2", operations);
    assertHasMethod("G.m1", operations);
    assertHasMethod("G.m2", operations);
    assertHasMethod("P.m2", operations);
  }

  @Test
  public void testGM1Match() throws Exception {
    Collection<TypedOperation> operations =
        createModel(CLASSNAMES, P + "G\\.m1\\(\\)").getOperations();
    assertDoesNotHaveMethod("G.m1", operations);
    // G.m1 overrides M1Interface.m1.
    assertDoesNotHaveMethod("M1Interface.m1", operations);
    assertHasMethod("E.m1", operations);
    assertHasMethod("E.m2", operations);
    assertHasMethod("G.m2", operations);
    assertHasMethod("P.m2", operations);
  }

  @Test
  public void testM1NameMatch() throws Exception {
    Collection<TypedOperation> operations = createModel(CLASSNAMES, "m1\\(\\)").getOperations();
    assertDoesNotHaveMethod("M1Interface.m1", operations);
    assertDoesNotHaveMethod("E.m1", operations);
    assertDoesNotHaveMethod("G.m1", operations);
    assertHasMethod("E.m2", operations);
    assertHasMethod("G.m2", operations);
    assertHasMethod("P.m2", operations);
  }

  @Test
  public void testGM2Match() throws Exception {
    Collection<TypedOperation> operations =
        createModel(CLASSNAMES, P + "G\\.m2\\(\\)").getOperations();
    assertDoesNotHaveMethod("G.m2", operations);
    // P.m2 overrides G.m2, so it is not omitted.
    assertHasMethod("P.m2", operations);
    assertHasMethod("E.m2", operations);
    assertHasMethod("M1Interface.m1", operations);
    assertHasMethod("G.m1", operations);
  }

  @Test
  public void testPM2Match() throws Exception {
    Collection<TypedOperation> operations =
        createModel(CLASSNAMES, P + "P\\.m2\\(\\)").getOperations();
    assertDoesNotHaveMethod("P.m2", operations);
    // P.m2 overrides G.m2.
    assertDoesNotHaveMethod("G.m2", operations);
    assertHasMethod("E.m2", operations);
    assertHasMethod("G.m1", operations);
  }

  @Test
  public void testM1InterfaceMatch() throws Exception {
    Collection<TypedOperation> operations =
        createModel(CLASSNAMES, P + "M1Interface\\.m1\\(\\)").getOperations();
    assertDoesNotHaveMethod("M1Interface.m1", operations);
    // E.m1 and G.m1 override M1Interface.m1, so they are not omitted.
    assertHasMethod("E.m1", operations);
    assertHasMethod("G.m1", operations);
  }

  @Test
  public void testInheritedMethodMatch() throws Exception {
    // P and C inherit m1 from G, but do not declare it.
    Collection<TypedOperation> operations =
        createModel(CLASSNAMES, P + "[PC]\\.m1\\(\\)").getOperations();
    assertHasMethod("M1Interface.m1", operations);
    assertHasMethod("G.m1", operations);
  }

  @Test
  public void testOverriddenMethodNotUnderTest() throws Exception {
    // M1Interface is not under test, but the omit predicate omits M1Interface.m1 anyway, because
    // G.m1 overrides it.
    OperationModel model =
        createModel(
            Collections.singletonList("randoop.reflection.omitinputs.p.G"), P + "G\\.m1\\(\\)");
    assertTrue(
        model
            .getOmitMethodsPredicate()
            .shouldOmit(TypedOperation.forMethod(M1Interface.class.getMethod("m1"))));
  }

  @Test
  public void testOmittedOperations() throws RandoopSpecificationError {
    Pattern omitpattern = Pattern.compile(P + "G\\.m1\\(\\)");
    // m1 is declared in G.
    assertOmittedGM1(
        ClassOrInterfaceType.forClass(randoop.reflection.omitinputs.p.G.class), omitpattern);
    // m1 is inherited from G.
    assertOmittedGM1(
        ClassOrInterfaceType.forClass(randoop.reflection.omitinputs.p.C.class), omitpattern);
  }

  /**
   * Asserts that extracting the operations of {@code type} omits exactly {@code G.m1}.
   *
   * @param type the type whose operations to extract
   * @param omitpattern the omit pattern, which should match {@code G.m1} and nothing else
   * @throws RandoopSpecificationError if a specification is malformed
   */
  private void assertOmittedGM1(ClassOrInterfaceType type, Pattern omitpattern)
      throws RandoopSpecificationError {
    AccessibilityPredicate accessibility =
        new AccessibilityPredicate.PackageAccessibilityPredicate("randoop.reflection");
    OperationExtractor extractor =
        OperationExtractor.extract(
            new ReflectionManager(accessibility),
            type,
            new DefaultReflectionPredicate(),
            new OmitMethodsPredicate(Collections.singletonList(omitpattern)),
            accessibility,
            null);
    assertDoesNotHaveMethod("m1", extractor.getOperations());
    assertHasMethod("m2", extractor.getOperations());
    Set<TypedOperation> omitted = new TreeSet<>(extractor.getOmittedOperations());
    assertEquals(1, omitted.size());
    assertHasMethod("G.m1", omitted);
  }

  /**
   * Throws an error if {@code operations} has no method with the given name.
   *
   * @param methodName a method name, qualified by its simple class name
   * @param operations a collection of operations
   */
  private static void assertHasMethod(String methodName, Collection<TypedOperation> operations) {
    if (!hasMethodNamed(operations, methodName)) {
      throw new AssertionError(String.format("Expected %s, found %s", methodName, operations));
    }
  }

  /**
   * Throws an error if {@code operations} has a method with the given name.
   *
   * @param methodName a method name, qualified by its simple class name
   * @param operations a collection of operations
   */
  private static void assertDoesNotHaveMethod(
      String methodName, Collection<TypedOperation> operations) {
    if (hasMethodNamed(operations, methodName)) {
      throw new AssertionError(
          String.format("Expected not to find %s, found %s", methodName, operations));
    }
  }

  /**
   * Returns true if {@code operations} has a method with the given name.
   *
   * @param operations a collection of operations
   * @param name a method name, qualified by its simple class name
   * @return true if {@code operations} has a method with the given name
   */
  private static boolean hasMethodNamed(Collection<TypedOperation> operations, String name) {
    for (TypedOperation operation : operations) {
      if (operation.getName().endsWith("." + name)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns an operation model for the given classes.
   *
   * @param classnames the classes under test
   * @param omitRegex the omit-methods pattern, or null to omit nothing
   * @return an operation model for the given classes
   * @throws Exception if the operation model cannot be created
   */
  private static OperationModel createModel(
      List<@ClassGetName String> classnames, @Nullable String omitRegex) throws Exception {
    List<Pattern> omitMethods =
        omitRegex == null
            ? Collections.emptyList()
            : Collections.singletonList(Pattern.compile(omitRegex));
    return OperationModel.createModel(
        IS_PUBLIC,
        new DefaultReflectionPredicate(),
        omitMethods,
        new LinkedHashSet<>(classnames),
        new LinkedHashSet<>(),
        new ThrowClassNameError(),
        new ArrayList<>(),
        null);
  }
}
