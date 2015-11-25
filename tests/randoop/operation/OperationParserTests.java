package randoop.operation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;

import junit.framework.TestCase;


public class OperationParserTests extends TestCase {

  public void testPrimStKind() {

    // String.
    checkParse(new NonreceiverTerm(String.class, null));
    checkParse(new NonreceiverTerm(String.class, ""));
    checkParse(new NonreceiverTerm(String.class, " "));
    checkParse(new NonreceiverTerm(String.class, "\""));
    checkParse(new NonreceiverTerm(String.class, "\n"));
    checkParse(new NonreceiverTerm(String.class, "\u0000"));

    // Object.
    checkParse(new NonreceiverTerm(Object.class, null));
    try {
      checkParse(new NonreceiverTerm(Object.class, new Object()));
      fail();
    } catch (IllegalArgumentException e) {
      // Good.
    }

    // Array.
    checkParse(new NonreceiverTerm(new Object[][]{}.getClass(), null));

    // Primitives.
    checkParse(new NonreceiverTerm(int.class, 0));
    checkParse(new NonreceiverTerm(int.class, 1));
    checkParse(new NonreceiverTerm(int.class, -1));
    checkParse(new NonreceiverTerm(int.class, Integer.MAX_VALUE));
    checkParse(new NonreceiverTerm(int.class, Integer.MIN_VALUE));

    checkParse(new NonreceiverTerm(byte.class, (byte)0));
    checkParse(new NonreceiverTerm(short.class, (short)0));
    checkParse(new NonreceiverTerm(long.class, (long)0));
    checkParse(new NonreceiverTerm(float.class, (float)0));
    checkParse(new NonreceiverTerm(double.class, (double)0));
    checkParse(new NonreceiverTerm(boolean.class, false));

    checkParse(new NonreceiverTerm(char.class, ' '));
    checkParse(new NonreceiverTerm(char.class, '\u0000'));
    checkParse(new NonreceiverTerm(char.class, '\''));
    checkParse(new NonreceiverTerm(char.class, '0'));
  }

  public void testRMethod() {

    for (Method m : ArrayList.class.getMethods()) {
      checkParse(MethodCall.createMethodCall(m));
    }
  }

  public void testRConstructor() {

    for (Constructor<?> c : ArrayList.class.getConstructors()) {
      checkParse(ConstructorCall.createConstructorCall(c));
    }
  }

  public void testDummyStatement() {
    checkParse(new DummyStatement("foobar"));
  }

  public void testArrayDecl() {
    checkParse(new ArrayCreation(int.class, 3));
  }



  private void checkParse(Operation st) {
    String stStr = st.toParseableString();
    System.out.println(stStr);
    Operation st2;
    try {
      st2 = OperationParser.parse(OperationParser.getId(st) + ":" + stStr);
    } catch (OperationParseException e) {
      throw new Error(e);
    }
    assertNotNull(st2);
    assertTrue(st.toString() + "," + st2.toString(), st2.equals(st));
    assertTrue(st.toParseableString() + "," + st2.toParseableString(), st.toParseableString().equals(st2.toParseableString()));
  }

}
