package randoop;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;

import junit.framework.TestCase;


public class StatementKindTests extends TestCase {

  public void testPrimStKind() {

    // String.
    checkParse(new PrimitiveOrStringOrNullDecl(String.class, null));
    checkParse(new PrimitiveOrStringOrNullDecl(String.class, ""));
    checkParse(new PrimitiveOrStringOrNullDecl(String.class, " "));
    checkParse(new PrimitiveOrStringOrNullDecl(String.class, "\""));
    checkParse(new PrimitiveOrStringOrNullDecl(String.class, "\n"));
    checkParse(new PrimitiveOrStringOrNullDecl(String.class, "\u0000"));

    // Object.
    checkParse(new PrimitiveOrStringOrNullDecl(Object.class, null));
    try {
      checkParse(new PrimitiveOrStringOrNullDecl(Object.class, new Object()));
      fail();
    } catch (IllegalArgumentException e) {
      // Good.
    }

    // Array.
    checkParse(new PrimitiveOrStringOrNullDecl(new Object[][]{}.getClass(), null));

    // Primitives.
    checkParse(new PrimitiveOrStringOrNullDecl(int.class, 0));
    checkParse(new PrimitiveOrStringOrNullDecl(int.class, 1));
    checkParse(new PrimitiveOrStringOrNullDecl(int.class, -1));
    checkParse(new PrimitiveOrStringOrNullDecl(int.class, Integer.MAX_VALUE));
    checkParse(new PrimitiveOrStringOrNullDecl(int.class, Integer.MIN_VALUE));

    checkParse(new PrimitiveOrStringOrNullDecl(byte.class, (byte)0));
    checkParse(new PrimitiveOrStringOrNullDecl(short.class, (short)0));
    checkParse(new PrimitiveOrStringOrNullDecl(long.class, (long)0));
    checkParse(new PrimitiveOrStringOrNullDecl(float.class, (float)0));
    checkParse(new PrimitiveOrStringOrNullDecl(double.class, (double)0));
    checkParse(new PrimitiveOrStringOrNullDecl(boolean.class, false));

    checkParse(new PrimitiveOrStringOrNullDecl(char.class, ' '));
    checkParse(new PrimitiveOrStringOrNullDecl(char.class, '\u0000'));
    checkParse(new PrimitiveOrStringOrNullDecl(char.class, '\''));
    checkParse(new PrimitiveOrStringOrNullDecl(char.class, '0'));
  }

  public void testRMethod() {

    for (Method m : ArrayList.class.getMethods()) {
      checkParse(RMethod.getRMethod(m));
    }
  }

  public void testRConstructor() {

    for (Constructor<?> c : ArrayList.class.getConstructors()) {
      checkParse(RConstructor.getRConstructor(c));
    }
  }

  public void testDummyStatement() {
    checkParse(new DummyStatement("foobar"));
  }

  public void testArrayDecl() {
    checkParse(new ArrayDeclaration(int.class, 3));
  }



  private void checkParse(StatementKind st) {
    String stStr = st.toParseableString();
    System.out.println(stStr);
    StatementKind st2;
    try {
      st2 = StatementKinds.parse(StatementKinds.getId(st) + ":" + stStr);
    } catch (StatementKindParseException e) {
      throw new Error(e);
    }
    assertNotNull(st2);
    assertTrue(st.toString() + "," + st2.toString(), st2.equals(st));
    assertTrue(st.toParseableString() + "," + st2.toParseableString(), st.toParseableString().equals(st2.toParseableString()));
  }

}
