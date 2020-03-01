package randoop.reflection;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertEquals;
import static randoop.reflection.VisibilityPredicate.IS_PUBLIC;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.junit.Rule;
import org.junit.Test;
import randoop.main.RandoopBug;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;

@SuppressWarnings("deprecation") // ExpectedException deprecated in JUnit 4.12, replaced in 4.13.
public class SignatureParserTest {

  @Rule public org.junit.rules.ExpectedException thrown = org.junit.rules.ExpectedException.none();

  @Test
  public void testParse() throws SignatureParseException {
    checkParse("java.util.ArrayList()");
    checkParse("randoop.reflection.ConcreteClass(java.lang.String, int, int, int)");
    checkParse("randoop.reflection.ConcreteClass.<init>(java.lang.String, int, int, int)");
    checkParse("randoop.reflection.ConcreteClass.getThePrivateField()");
    checkParse("randoop.reflection.ConcreteClass.setThePublicField(java.lang.Object)");
    checkParse("randoop.reflection.ConcreteClass.setThePrivateField(int)");
  }

  @Test
  public void testBadNameParse() throws SignatureParseException {
    thrown.expect(SignatureParseException.class);
    thrown.expectMessage(startsWith("Expected fully-qualified name but got "));
    checkParse("ConcreteClass(java.lang.String, int, int, int)");
  }

  @Test
  public void testBadClassParse() throws SignatureParseException {
    thrown.expect(SignatureParseException.class);
    thrown.expectMessage(startsWith("Class not found for method or constructor"));
    checkParse("randoop.reflection.TheConcreteClass(java.lang.String, int, int, int)");
  }

  @Test
  public void testBadConstructorParse() throws SignatureParseException {
    thrown.expect(SignatureParseException.class);
    thrown.expectMessage(containsString("constructor not found for signature"));
    checkParse("randoop.reflection.ConcreteClass(java.lang.String, double)");
  }

  @Test
  public void testBadArgumentParse() throws SignatureParseException {
    thrown.expect(SignatureParseException.class);
    thrown.expectMessage(startsWith("Argument type \"izBadType\" not recognized"));
    checkParse("randoop.reflection.ConcreteClass(java.lang.String, izBadType)");
  }

  private void checkParse(String inputString) throws SignatureParseException {

    AccessibleObject accessibleObject;
    try {
      accessibleObject =
          SignatureParser.parse(inputString, IS_PUBLIC, new DefaultReflectionPredicate());
    } catch (FailedPredicateException e) {
      throw new RandoopBug("This can't happen", e);
    }

    TypedClassOperation operation =
        (accessibleObject instanceof Constructor)
            ? TypedOperation.forConstructor((Constructor) accessibleObject)
            : TypedOperation.forMethod((Method) accessibleObject);

    String expectedString = inputString.replace(" ", "").replace(".<init>", "");
    String signatureString = operation.getRawSignature().toString();
    assertEquals(expectedString, signatureString);
  }
}
