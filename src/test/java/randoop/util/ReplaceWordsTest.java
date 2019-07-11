package randoop.util;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

/** Tests for {@link Util#replaceWords}. */
public class ReplaceWordsTest {

  @Test
  public void testConflictingNames() {
    String condition = "result.equals(e)";
    Map<String, String> map = new HashMap<>();
    map.put("e", "x1");
    map.put("result", "x2");

    assertEquals(
        "should not replace e in \"result\"", "x2.equals(x1)", Util.replaceWords(condition, map));
  }

  @Test
  public void testCyclicReplacements() {
    String condition = "a == b && c == d";
    Map<String, String> map = new HashMap<>();
    map.put("b", "c");
    map.put("a", "b");

    assertEquals(
        "substitution should not interfere", "b == c && c == d", Util.replaceWords(condition, map));
  }
}
