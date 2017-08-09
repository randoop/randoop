package randoop.condition;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/** Tests for {@link NameReplacementMap}. */
public class NameReplacementTest {

  @Test
  public void testConflictingNames() {
    NameReplacementMap map = new NameReplacementMap();
    String condition = "result.equals(e)";
    map.put("e", "x1");
    map.put("result", "x2");

    assertThat(
        "should not replace e in result",
        map.replaceNames(condition),
        is(equalTo("x2.equals(x1)")));
  }

  @Test
  public void testCyclicReplacements() {
    NameReplacementMap map = new NameReplacementMap();
    String condition = "a == b && c == d";
    map.put("b", "c");
    map.put("a", "b");

    assertThat(
        "substitution should not interfere",
        map.replaceNames(condition),
        is(equalTo("b == c && c == d")));
  }
}
