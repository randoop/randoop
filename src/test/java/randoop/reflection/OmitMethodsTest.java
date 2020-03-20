package randoop.reflection;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.Test;
import randoop.operation.TypedOperation;
import randoop.types.ClassOrInterfaceType;

/** Tests for omit-methods filtering. */
public class OmitMethodsTest {

  private static ClassOrInterfaceType m1Type =
      ClassOrInterfaceType.forClass(randoop.reflection.omitinputs.p.M1Interface.class);
  private static ClassOrInterfaceType gType =
      ClassOrInterfaceType.forClass(randoop.reflection.omitinputs.p.G.class);
  private static ClassOrInterfaceType pType =
      ClassOrInterfaceType.forClass(randoop.reflection.omitinputs.p.P.class);
  private static ClassOrInterfaceType cType =
      ClassOrInterfaceType.forClass(randoop.reflection.omitinputs.p.C.class);
  private static ClassOrInterfaceType dType =
      ClassOrInterfaceType.forClass(randoop.reflection.omitinputs.q.D.class);
  private static ClassOrInterfaceType eType =
      ClassOrInterfaceType.forClass(randoop.reflection.omitinputs.p.E.class);
  private static List<ClassOrInterfaceType> allTypes =
      Arrays.asList(m1Type, gType, pType, cType, dType, eType);

  @Test
  public void testGM1Match() {
    Pattern omitPattern = Pattern.compile("^randoop\\.reflection\\.omitinputs\\.p\\.G\\.m1\\(\\)");
    Collection<TypedOperation> operations = getOperations(omitPattern);
    assertDoesNotHaveMethod("M1Interface.m1", operations);
    assertDoesNotHaveMethod("G.m1", operations);
    assertDoesNotHaveMethod("P.m1", operations);
    assertDoesNotHaveMethod("C.m1", operations);
    assertDoesNotHaveMethod("D.m1", operations);
    assertHasMethod("E.m1", operations);
  }

  @Test
  public void testM1NameMatch() {
    Pattern omitPattern = Pattern.compile("m1\\(\\)");
    Collection<TypedOperation> operations = getOperations(omitPattern);
    assertDoesNotHaveMethod("M1Interface.m1", operations);
    assertDoesNotHaveMethod("G.m1", operations);
    assertDoesNotHaveMethod("P.m1", operations);
    assertDoesNotHaveMethod("C.m1", operations);
    assertDoesNotHaveMethod("D.m1", operations);
    assertDoesNotHaveMethod("E.m1", operations);
  }

  @Test
  public void testGM2Match() {
    Pattern omitPattern = Pattern.compile("^randoop\\.reflection\\.omitinputs\\.p\\.G\\.m2\\(\\)");
    Collection<TypedOperation> operations = getOperations(omitPattern);
    assertDoesNotHaveMethod("M1Interface.m2", operations);
    assertDoesNotHaveMethod("G.m2", operations);
    assertHasMethod("P.m2", operations);
    assertDoesNotHaveMethod("C.m2", operations);
    assertDoesNotHaveMethod("D.m2", operations);
    assertHasMethod("E.m2", operations);
  }

  // @Test
  // public void testPM1Match() {
  //   Pattern omitPattern =
  // Pattern.compile("^randoop\\.reflection\\.omitinputs\\.p\\.P\\.m1\\(\\)");
  //   Collection<TypedOperation> operations = getOperations(omitPattern);
  //   assertDoesNotHaveMethod("M1Interface.m1", operations);
  //   assertDoesNotHaveMethod("G.m1", operations);
  //   assertDoesNotHaveMethod("P.m1", operations);
  //   assertDoesNotHaveMethod("C.m1", operations);
  //   assertDoesNotHaveMethod("D.m1", operations);
  //   assertHasMethod("E.m1", operations);
  // }

  // @Test
  // public void testCM1Match() {
  //   Pattern omitPattern =
  // Pattern.compile("^randoop\\.reflection\\.omitinputs\\.p\\.C\\.m1\\(\\)");
  //   Collection<TypedOperation> operations = getOperations(omitPattern);
  //   assertHasMethod("M1Interface.m1", operations);
  //   assertHasMethod("G.m1", operations);
  //   assertDoesNotHaveMethod("P.m1", operations);
  //   assertDoesNotHaveMethod("C.m1", operations);
  //   assertDoesNotHaveMethod("D.m1", operations);
  //   assertHasMethod("E.m1", operations);
  // }

  @Test
  public void testM1InterfaceMatch() {
    Pattern omitPattern =
        Pattern.compile("^randoop\\.reflection\\.omitinputs\\.p\\.M1Interface\\.m1\\(\\)");
    Collection<TypedOperation> operations = getOperations(omitPattern);
    assertDoesNotHaveMethod("M1Interface.m1", operations);
    assertHasMethod("G.m1", operations);
    assertDoesNotHaveMethod("P.m1", operations);
    assertDoesNotHaveMethod("C.m1", operations);
    assertDoesNotHaveMethod("D.m1", operations);
    assertHasMethod("E.m1", operations);
  }

  // @Test
  // public void testDM1Match() {
  //   Pattern omitPattern =
  // Pattern.compile("^randoop\\.reflection\\.omitinputs\\.q\\.D\\.m1\\(\\)");
  //   Collection<TypedOperation> operations = getOperations(omitPattern);
  //   assertDoesNotHaveMethod("M1Interface.m1", operations);
  //   assertDoesNotHaveMethod("G.m1", operations);
  //   assertDoesNotHaveMethod("P.m1", operations);
  //   assertDoesNotHaveMethod("C.m1", operations);
  //   assertDoesNotHaveMethod("D.m1", operations);
  //   assertHasMethod("E.m1", operations);
  // }

  private void assertHasMethod(String methodName, Collection<TypedOperation> operations) {
    if (!hasMethodNamed(operations, methodName)) {
      throw new Error(String.format("Expected %s, found %s", methodName, operations));
    }
  }

  private void assertDoesNotHaveMethod(String methodName, Collection<TypedOperation> operations) {
    if (hasMethodNamed(operations, methodName)) {
      throw new Error(String.format("Expected not to find %s, found %s", methodName, operations));
    }
  }

  private boolean hasMethodNamed(Collection<TypedOperation> operations, String name) {
    for (TypedOperation operation : operations) {
      if (operation.getName().endsWith("." + name)) {
        return true;
      }
    }
    return false;
  }

  private Collection<TypedOperation> getOperations(Pattern omitPattern) {
    List<Pattern> omitList = Collections.singletonList(omitPattern);
    OmitMethodsPredicate omitMethodsPredicate = new OmitMethodsPredicate(omitList);
    VisibilityPredicate visibility =
        new VisibilityPredicate.PackageVisibilityPredicate("randoop.reflection");
    return OperationExtractor.operations(
        allTypes, new DefaultReflectionPredicate(), omitMethodsPredicate, visibility);
  }
}
