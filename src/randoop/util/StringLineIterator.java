package randoop.util;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Given a string consisting of words separated by white space, lets the
 * user ask for the words as lines of text of a desired length, until there
 * are no more words.
 */
public class StringLineIterator {

  private final Queue<String> words;

  public StringLineIterator(String words) {
    if (words == null)
      throw new IllegalArgumentException("words cannot be null.");
    this.words = new LinkedList<String>(Arrays.asList(words.split("\\s")));
  }

  /**
   * True if there are more words remaining.
   */
  public boolean hasMoreWords() {
    return this.words.size() > 0;
  }

  /**
   * Returns a line of words, ensuring that the length of the line
   * is no greater than the given length. If such a line is not possible,
   * returns an empty String.
   * 
   * Note that if length is always too small for the iterator to make
   * progress by returning non-empty lines, the iterator will not
   * terminate.
   */
  public String nextLine(int length) {
    if (length < 0)
      throw new IllegalArgumentException("length cannot be negative");
    if (!hasMoreWords())
      throw new IllegalStateException("no more words.");

    StringBuffer b = new StringBuffer("");
    for (;;) {
      // Loop invariant
      assert b.length() <= length;

      String nextWord = words.peek();
      if (nextWord == null) {
        // No more words. Return current line.
        return b.toString();
      }
      int lengthWithNextWord = b.length() + /* potential space*/ (b.length() > 0 ? 1 : 0) +  nextWord.length();
      if (lengthWithNextWord > length) {
        // Adding another word (plus space to separate from the
        // last word currently in the line) would make the line too long.
        // Return the current line.
        return b.toString();
      }
      // Add another word and remove the word from queue.
      if (b.length() > 0)
        b.append(" ");
      b.append(nextWord);
      words.remove();
    }
  }
}
