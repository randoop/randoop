package randoop.reflection;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import org.junit.BeforeClass;
import org.junit.Test;
import randoop.operation.TypedOperation;
import randoop.types.ClassOrInterfaceType;

/** Tests for omit-methods filtering. */
public class OmitMethodsTest {

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
  public void testGM1Match() {

    Set<TypedOperation> operations;
    Pattern omitpattern = Pattern.compile("^randoop\\.reflection\\.omitinputs\\.p\\.G\\.m1\\(\\)");
    assertDoesNotHaveMethod("m1", gType, omitpattern);
    assertDoesNotHaveMethod("m1", pType, omitpattern);
    assertDoesNotHaveMethod("m1", cType, omitpattern);
    assertDoesNotHaveMethod("m1", dType, omitpattern);
    assertHasMethod("m1", eType, omitpattern);
  }

  @Test
  public void testM1NameMatch() {
    Set<TypedOperation> operations;
    Pattern omitpattern = Pattern.compile("m1\\(\\)");
    assertDoesNotHaveMethod("m1", gType, omitpattern);
    assertDoesNotHaveMethod("m1", pType, omitpattern);
    assertDoesNotHaveMethod("m1", cType, omitpattern);
    assertDoesNotHaveMethod("m1", dType, omitpattern);
    assertDoesNotHaveMethod("m1", eType, omitpattern);
  }

  @Test
  public void testGM2Match() {
    Set<TypedOperation> operations;
    Pattern omitpattern = Pattern.compile("^randoop\\.reflection\\.omitinputs\\.p\\.G\\.m2\\(\\)");
    assertDoesNotHaveMethod("m2", gType, omitpattern);
    assertDoesNotHaveMethod("m2", pType, omitpattern);
    assertDoesNotHaveMethod("m2", cType, omitpattern);
    assertDoesNotHaveMethod("m2", dType, omitpattern);
    assertHasMethod("m2", eType, omitpattern);
  }

  @Test
  public void testPM1Match() {

    Set<TypedOperation> operations;
    Pattern omitpattern = Pattern.compile("^randoop\\.reflection\\.omitinputs\\.p\\.P\\.m1\\(\\)");
    assertHasMethod("m1", gType, omitpattern);
    assertDoesNotHaveMethod("m1", pType, omitpattern);
    assertDoesNotHaveMethod("m1", cType, omitpattern);
    assertHasMethod("m1", dType, omitpattern);
    assertHasMethod("m1", eType, omitpattern);
  }

  @Test
  public void testCM1Match() {

    Set<TypedOperation> operations;
    Pattern omitpattern = Pattern.compile("^randoop\\.reflection\\.omitinputs\\.p\\.C\\.m1\\(\\)");
    assertHasMethod("m1", gType, omitpattern);
    assertHasMethod("m1", pType, omitpattern);
    assertDoesNotHaveMethod("m1", cType, omitpattern);
    assertHasMethod("m1", dType, omitpattern);
    assertHasMethod("m1", eType, omitpattern);
  }

  @Test
  public void testM1InterfaceMatch() {

    Set<TypedOperation> operations;
    Pattern omitpattern =
        Pattern.compile("^randoop\\.reflection\\.omitinputs\\.p\\.M1Interface\\.m1\\(\\)");
    assertDoesNotHaveMethod("m1", gType, omitpattern);
    assertDoesNotHaveMethod("m1", pType, omitpattern);
    assertDoesNotHaveMethod("m1", cType, omitpattern);
    assertDoesNotHaveMethod("m1", dType, omitpattern);
    assertDoesNotHaveMethod("m1", eType, omitpattern);
  }

  @Test
  public void testDM1Match() {

    Set<TypedOperation> operations;
    Pattern omitpattern = Pattern.compile("^randoop\\.reflection\\.omitinputs\\.q\\.D\\.m1\\(\\)");
    assertHasMethod("m1", gType, omitpattern);
    assertHasMethod("m1", pType, omitpattern);
    assertHasMethod("m1", cType, omitpattern);
    assertDoesNotHaveMethod("m1", dType, omitpattern);
    assertHasMethod("m1", eType, omitpattern);
  }

  private void assertHasMethod(String methodName, ClassOrInterfaceType c, Pattern omitPattern) {
    Pattern omitpattern = Pattern.compile("^randoop\\.reflection\\.omitinputs\\.q\\.D\\.m1\\(\\)");
    Set<TypedOperation> operations = getOperations(c, omitpattern);
    if (!hasMethodNamed(operations, methodName)) {
      throw new Error(String.format("Expected %s, found %s", methodName, operations));
    }
  }

  private void assertDoesNotHaveMethod(
      String methodName, ClassOrInterfaceType c, Pattern omitPattern) {
    Pattern omitpattern = Pattern.compile("^randoop\\.reflection\\.omitinputs\\.q\\.D\\.m1\\(\\)");
    Set<TypedOperation> operations = getOperations(c, omitpattern);
    if (hasMethodNamed(operations, methodName)) {
      throw new Error(String.format("Expected not to find %s, found %s", methodName, operations));
    }
  }

  private boolean hasMethodNamed(Set<TypedOperation> operations, String name) {
    for (TypedOperation operation : operations) {
      if (operation.getName().endsWith("." + name)) {
        return true;
      }
    }
    return false;
  }

  private Set<TypedOperation> getOperations(ClassOrInterfaceType type, Pattern omitpattern) {
    List<Pattern> omitList = Collections.singletonList(omitpattern);
    OmitMethodsPredicate omitMethodsPredicate = new OmitMethodsPredicate(omitList);
    VisibilityPredicate visibility =
        new VisibilityPredicate.PackageVisibilityPredicate("randoop.reflection");
    ReflectionPredicate reflectionPredicate = new DefaultReflectionPredicate();
    Collection<TypedOperation> oneClassOperations =
        OperationExtractor.operations(type, reflectionPredicate, omitMethodsPredicate, visibility);
    return new TreeSet<>(oneClassOperations);
  }
}
