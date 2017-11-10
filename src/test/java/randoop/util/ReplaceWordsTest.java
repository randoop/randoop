package randoop.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

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

    assertThat(
        "should not replace e in \"result\"",
        Util.replaceWords(condition, map),
        is(equalTo("x2.equals(x1)")));
  }

  @Test
  public void testCyclicReplacements() {
    String condition = "a == b && c == d";
    Map<String, String> map = new HashMap<>();
    map.put("b", "c");
    map.put("a", "b");

    assertThat(
        "substitution should not interfere",
        Util.replaceWords(condition, map),
        is(equalTo("b == c && c == d")));
  }
}
