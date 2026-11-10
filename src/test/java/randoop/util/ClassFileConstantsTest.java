package randoop.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;
import org.junit.Test;
import randoop.main.GenInputsAbstract;
import randoop.operation.NonreceiverTerm;
import randoop.types.JavaTypes;

/** Tests for {@link ClassFileConstants}. */
public class ClassFileConstantsTest {

  @Test
  public void skipsOverlyLongStringConstants() {
    ClassFileConstants.ConstantSet constants = new ClassFileConstants.ConstantSet();
    constants.ints.add(7);
    constants.strings.add("short");

    char[] characters = new char[GenInputsAbstract.string_maxlen + 1];
    Arrays.fill(characters, 'x');
    constants.strings.add(new String(characters));

    ByteArrayOutputStream output = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    Set<NonreceiverTerm> terms;
    try {
      System.setOut(new PrintStream(output));
      terms = ClassFileConstants.constantSetToNonreceiverTerms(constants);
    } finally {
      System.setOut(originalOut);
    }

    assertEquals(2, terms.size());
    assertTrue(terms.contains(new NonreceiverTerm(JavaTypes.INT_TYPE, 7)));
    assertTrue(terms.contains(new NonreceiverTerm(JavaTypes.STRING_TYPE, "short")));

    String warning = new String(output.toByteArray(), StandardCharsets.UTF_8);
    assertTrue(warning.contains("Ignoring String constant value"));
    assertTrue(warning.contains("length = " + characters.length));
  }
}
