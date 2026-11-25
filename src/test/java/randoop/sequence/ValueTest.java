package randoop.sequence;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import randoop.main.GenInputsAbstract;

/** Tests for {@link Value}. */
public class ValueTest {

  /**
   * Tests that the cache used by {@link Value#escapedStringLengthOk} is invalidated when {@link
   * GenInputsAbstract#string_maxlen} changes.
   */
  @Test
  public void testEscapedStringLengthOkCacheInvalidation() {
    int originalMaxlen = GenInputsAbstract.string_maxlen;
    try {
      // "\n" has length 1, but its escaped form "\\n" has length 2.
      GenInputsAbstract.string_maxlen = 1;
      assertFalse(Value.escapedStringLengthOk("\n"));
      GenInputsAbstract.string_maxlen = 2;
      assertTrue(Value.escapedStringLengthOk("\n"));
    } finally {
      GenInputsAbstract.string_maxlen = originalMaxlen;
    }
  }
}
