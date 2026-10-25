package randoop.output;

import static org.junit.Assert.assertEquals;
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

  /** Tests that malformed fixture text is rejected rather than partially parsed. */
  @Test
  public void testMalformed() {
    ParseException e =
        assertThrows(
            ParseException.class,
            () -> JUnitCreator.parseFixture(Arrays.asList("int y = 2;", "int x = ; foo(")));
    assertTrue(e.getMessage(), e.getMessage().contains("line 2"));
    assertThrows(
        ParseException.class, () -> JUnitCreator.parseFixture(Arrays.asList("this is not java")));
  }
}
