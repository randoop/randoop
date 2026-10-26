package randoop.reflection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
import org.junit.BeforeClass;
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
 *
 * <p>Class {@code p.ComparableInput} implements {@code Comparable<ComparableInput>} and declares
 * {@code compareTo(ComparableInput)} and {@code compareTo(Integer)}.
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

  private static ClassOrInterfaceType gType;
  private static ClassOrInterfaceType pType;
  private static ClassOrInterfaceType cType;
  private static ClassOrInterfaceType dType;
  private static ClassOrInterfaceType eType;

  @BeforeClass
  public static void setup() {
    gType = ClassOrInterfaceType.forClass(randoop.reflection.omitinputs.p.G.class);
    pType = ClassOrInterfaceType.forClass(randoop.reflection.omitinputs.p.P.class);
    cType = ClassOrInterfaceType.forClass(randoop.reflection.omitinputs.p.C.class);
    dType = ClassOrInterfaceType.forClass(randoop.reflection.omitinputs.q.D.class);
    eType = ClassOrInterfaceType.forClass(randoop.reflection.omitinputs.p.E.class);
  }

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
  public void testM1NameMatch() throws RandoopSpecificationError {
    Set<TypedOperation> operations;
    Pattern omitpattern = Pattern.compile("m1\\(\\)");
    operations = getOperations(gType, omitpattern);
    assertFalse(hasMethodNamed(operations, "m1"));
    operations = getOperations(pType, omitpattern);
    assertFalse(hasMethodNamed(operations, "m1"));
    operations = getOperations(cType, omitpattern);
    assertFalse(hasMethodNamed(operations, "m1"));
    operations = getOperations(dType, omitpattern);
    assertFalse(hasMethodNamed(operations, "m1"));
    operations = getOperations(eType, omitpattern);
    assertFalse(hasMethodNamed(operations, "m1"));
  }

  @Test
  public void testGM2Match() throws RandoopSpecificationError {
    Set<TypedOperation> operations;
    Pattern omitpattern = Pattern.compile("^randoop\\.reflection\\.omitinputs\\.p\\.G\\.m2\\(\\)");
    operations = getOperations(gType, omitpattern);
    assertFalse(hasMethodNamed(operations, "m2"));
    operations = getOperations(pType, omitpattern);
    assertFalse(hasMethodNamed(operations, "m2"));
    operations = getOperations(cType, omitpattern);
    assertFalse(hasMethodNamed(operations, "m2"));
    operations = getOperations(dType, omitpattern);
    assertFalse(hasMethodNamed(operations, "m2"));
    operations = getOperations(eType, omitpattern);
    assertTrue(hasMethodNamed(operations, "m2"));
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
  public void testPM1Match() throws RandoopSpecificationError {

    Set<TypedOperation> operations;
    Pattern omitpattern = Pattern.compile("^randoop\\.reflection\\.omitinputs\\.p\\.P\\.m1\\(\\)");
    operations = getOperations(gType, omitpattern);
    assertTrue(hasMethodNamed(operations, "m1"));
    operations = getOperations(pType, omitpattern);
    assertFalse(hasMethodNamed(operations, "m1"));
    operations = getOperations(cType, omitpattern);
    assertFalse(hasMethodNamed(operations, "m1"));
    operations = getOperations(dType, omitpattern);
    assertTrue(hasMethodNamed(operations, "m1"));
    operations = getOperations(eType, omitpattern);
    assertTrue(hasMethodNamed(operations, "m1"));
  }

  @Test
  public void testCM1Match() throws RandoopSpecificationError {

    Set<TypedOperation> operations;
    Pattern omitpattern = Pattern.compile("^randoop\\.reflection\\.omitinputs\\.p\\.C\\.m1\\(\\)");
    operations = getOperations(gType, omitpattern);
    assertTrue(hasMethodNamed(operations, "m1"));
    operations = getOperations(pType, omitpattern);
    assertTrue(hasMethodNamed(operations, "m1"));
    operations = getOperations(cType, omitpattern);
    assertFalse(hasMethodNamed(operations, "m1"));
    operations = getOperations(dType, omitpattern);
    assertTrue(hasMethodNamed(operations, "m1"));
    operations = getOperations(eType, omitpattern);
    assertTrue(hasMethodNamed(operations, "m1"));
  }

  @Test
  public void testM1InterfaceMatch() throws RandoopSpecificationError {

    Set<TypedOperation> operations;
    Pattern omitpattern =
        Pattern.compile("^randoop\\.reflection\\.omitinputs\\.p\\.M1Interface\\.m1\\(\\)");
    operations = getOperations(gType, omitpattern);
    assertFalse(hasMethodNamed(operations, "m1"));
    operations = getOperations(pType, omitpattern);
    assertFalse(hasMethodNamed(operations, "m1"));
    operations = getOperations(cType, omitpattern);
    assertFalse(hasMethodNamed(operations, "m1"));
    operations = getOperations(dType, omitpattern);
    assertFalse(hasMethodNamed(operations, "m1"));
    operations = getOperations(eType, omitpattern);
    assertFalse(hasMethodNamed(operations, "m1"));
  }

  @Test
  public void testDM1Match() throws RandoopSpecificationError {

    Set<TypedOperation> operations;
    Pattern omitpattern = Pattern.compile("^randoop\\.reflection\\.omitinputs\\.q\\.D\\.m1\\(\\)");
    operations = getOperations(gType, omitpattern);
    assertTrue(hasMethodNamed(operations, "m1"));
    operations = getOperations(pType, omitpattern);
    assertTrue(hasMethodNamed(operations, "m1"));
    operations = getOperations(cType, omitpattern);
    assertTrue(hasMethodNamed(operations, "m1"));
    operations = getOperations(dType, omitpattern);
    assertFalse(hasMethodNamed(operations, "m1"));
    operations = getOperations(eType, omitpattern);
    assertTrue(hasMethodNamed(operations, "m1"));
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
  public void testOverriddenGenericMethod() throws Exception {
    // ComparableInput.compareTo(ComparableInput) overrides Comparable.compareTo(T), whose erased
    // parameter type is Object.
    OperationModel model =
        createModel(
            Collections.singletonList("randoop.reflection.omitinputs.p.ComparableInput"),
            P + "ComparableInput\\.compareTo\\(randoop");
    assertTrue(
        model
            .getOmitMethodsPredicate()
            .shouldOmit(
                TypedOperation.forMethod(Comparable.class.getMethod("compareTo", Object.class))));
  }

  @Test
  public void testOverloadOfOverriddenGenericMethod() throws Exception {
    // ComparableInput.compareTo(Integer) does not override Comparable.compareTo(T).
    OperationModel model =
        createModel(
            Collections.singletonList("randoop.reflection.omitinputs.p.ComparableInput"),
            P + "ComparableInput\\.compareTo\\(java\\.lang\\.Integer\\)");
    assertFalse(
        model
            .getOmitMethodsPredicate()
            .shouldOmit(
                TypedOperation.forMethod(Comparable.class.getMethod("compareTo", Object.class))));
  }

  @Test
  public void testOmittedOperations() throws RandoopSpecificationError {
    Pattern omitpattern = Pattern.compile("^randoop\\.reflection\\.omitinputs\\.p\\.G\\.m1\\(\\)");
    // m1 is declared in G.
    assertOmittedM1(gType, omitpattern);
    // m1 is inherited from G.
    assertOmittedM1(cType, omitpattern);
  }

  /**
   * Asserts that extracting the operations of {@code type} omits exactly {@code m1}, and that the
   * omitted operation has {@code type} as its declaring type.
   *
   * @param type the type whose operations to extract
   * @param omitpattern the omit pattern, which should match {@code m1} and nothing else
   * @throws RandoopSpecificationError if a specification is malformed
   */
  private void assertOmittedM1(ClassOrInterfaceType type, Pattern omitpattern)
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
    assertFalse(hasMethodNamed(new TreeSet<>(extractor.getOperations()), "m1"));
    assertTrue(hasMethodNamed(new TreeSet<>(extractor.getOperations()), "m2"));
    Set<TypedOperation> omitted = new TreeSet<>(extractor.getOmittedOperations());
    assertEquals(1, omitted.size());
    assertTrue(hasMethodNamed(omitted, "m1"));
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
   * @param name a method name, optionally qualified by its simple class name
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

  private Set<TypedOperation> getOperations(ClassOrInterfaceType type, Pattern omitpattern)
      throws RandoopSpecificationError {
    List<Pattern> omitList = Collections.singletonList(omitpattern);
    OmitMethodsPredicate omitMethodsPredicate = new OmitMethodsPredicate(omitList);
    AccessibilityPredicate accessibility =
        new AccessibilityPredicate.PackageAccessibilityPredicate("randoop.reflection");
    ReflectionPredicate reflectionPredicate = new DefaultReflectionPredicate();
    Collection<TypedOperation> oneClassOperations =
        OperationExtractor.operations(
            type, reflectionPredicate, omitMethodsPredicate, accessibility);
    return new TreeSet<>(oneClassOperations);
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
