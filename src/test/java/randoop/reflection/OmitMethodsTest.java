package randoop.reflection;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
    operations = getOperations(gType, omitpattern);
    assertFalse(hasMethodNamed(operations, "m1"));
    operations = getOperations(pType, omitpattern);
    assertFalse(hasMethodNamed(operations, "m1"));
    operations = getOperations(cType, omitpattern);
    assertFalse(hasMethodNamed(operations, "m1"));
    operations = getOperations(dType, omitpattern);
    assertFalse(hasMethodNamed(operations, "m1"));
    operations = getOperations(eType, omitpattern);
    assertTrue(hasMethodNamed(operations, "m1"));
  }

  @Test
  public void testM1NameMatch() {
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
  public void testGM2Match() {
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
  public void testPM1Match() {

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
  public void testCM1Match() {

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
  public void testM1InterfaceMatch() {

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
  public void testDM1Match() {

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
    AccessibilityPredicate accessibility =
        new AccessibilityPredicate.PackageAccessibilityPredicate("randoop.reflection");
    ReflectionPredicate reflectionPredicate = new DefaultReflectionPredicate();
    Collection<TypedOperation> oneClassOperations =
        OperationExtractor.operations(
            type, reflectionPredicate, omitMethodsPredicate, accessibility);
    return new TreeSet<>(oneClassOperations);
  }
}
