package randoop.output;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.github.javaparser.ParseException;
import com.github.javaparser.ast.stmt.BlockStmt;
import java.util.Arrays;
import org.junit.Test;

/** Tests for {@link JUnitCreator#parseFixture}. */
public class ParseFixtureTest {

  /**
   * Tests that well-formed fixture text is parsed.
   *
   * @throws ParseException if the fixture text cannot be parsed
   */
  @Test
  public void testWellFormed() throws ParseException {
    BlockStmt block =
        JUnitCreator.parseFixture(
            Arrays.asList("int x = 1;", "// a comment", "System.out.println(x);"));
    assertEquals(2, block.getStatements().size());
  }

  /**
   * Tests that null fixture text yields null.
   *
   * @throws ParseException if the fixture text cannot be parsed
   */
  @Test
  public void testNull() throws ParseException {
    assertNull(JUnitCreator.parseFixture(null));
  }

  /**
   * Tests that fixture text that uses features of recent Java versions is parsed.
   *
   * @throws ParseException if the fixture text cannot be parsed
   */
  @Test
  public void testRecentJavaFeatures() throws ParseException {
    BlockStmt block =
        JUnitCreator.parseFixture(
            Arrays.asList(
                "Object o = 1;",
                "if (o instanceof Integer i) {}",
                "String s = \"\"\"",
                "    text block",
                "    \"\"\";",
                "int k = switch (s.length()) { case 0 -> 1; default -> 2; };",
                "record R(int x) {}",
                "var v = 3;"));
    assertEquals(6, block.getStatements().size());
  }

  /** Tests that malformed fixture text is rejected rather than partially parsed. */
  @Test
  public void testMalformed() {
    ParseException e =
        assertThrows(
            ParseException.class,
            () -> JUnitCreator.parseFixture(Arrays.asList("int y = 2;", "int x = ; foo(")));
    String message = e.getMessage();
    assertTrue(message, message.contains("Line 2, column 9: "));
    assertTrue(message, message.contains("At end of text: "));
    assertTrue(e.getCause() instanceof ParseException);
    assertThrows(
        ParseException.class, () -> JUnitCreator.parseFixture(Arrays.asList("this is not java")));
  }

  /** Tests that the column of an error on the first line is a column of the fixture text. */
  @Test
  public void testErrorOnFirstLine() {
    ParseException e =
        assertThrows(
            ParseException.class, () -> JUnitCreator.parseFixture(Arrays.asList("int x = ;")));
    assertTrue(e.getMessage(), e.getMessage().startsWith("Line 1, column 9: "));
  }

  /** Tests that a lexical error is reported at its position in the fixture text. */
  @Test
  public void testLexicalError() {
    ParseException e =
        assertThrows(
            ParseException.class, () -> JUnitCreator.parseFixture(Arrays.asList("char c = 'ab';")));
    assertTrue(e.getMessage(), e.getMessage().startsWith("Line 1, column 12: "));
    assertFalse(e.getMessage(), e.getMessage().contains("at line"));

    e =
        assertThrows(
            ParseException.class,
            () -> JUnitCreator.parseFixture(Arrays.asList("String s = \"\"\"", "abc")));
    assertTrue(e.getMessage(), e.getMessage().startsWith("At end of text: "));
  }

  /** Tests that an unmatched "}" is reported at its position in the fixture text. */
  @Test
  public void testUnmatchedCloseBrace() {
    ParseException e =
        assertThrows(
            ParseException.class,
            () -> JUnitCreator.parseFixture(Arrays.asList("int y = 2;", "}", "int z = 3;")));
    assertEquals("Line 2, column 1: Unmatched \"}\".", e.getMessage());
  }

  /** Tests that an unmatched "{" is reported at the end of the fixture text. */
  @Test
  public void testUnmatchedOpenBrace() {
    ParseException e =
        assertThrows(
            ParseException.class,
            () -> JUnitCreator.parseFixture(Arrays.asList("int y = 2;", "{")));
    assertTrue(e.getMessage(), e.getMessage().startsWith("At end of text: "));
  }
}
