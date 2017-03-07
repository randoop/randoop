package randoop.output;

import java.util.List;

import plume.UtilMDE;
import randoop.Globals;

/**
 * Common behavior for source builder classes.
 */
abstract class SourceBuilder {

  /** The current indent for this {@link randoop.output.SourceBuilder} */
  private int indentCount;
  private String indentText;

  /**
   * Creates a {@link randoop.output.SourceBuilder} with no indentation.
   *
   */
  SourceBuilder() {
    this.indentCount = 0;
    this.indentText = "";
  }

  /**
   * Creates a line from the given tokens with the current indentation and separated by spaces.
   *
   * @param toks  the tokens to include on the line
   * @return the {@code String} constructed from the tokens
   */
  String createLine(String... toks) {
    return indentText + UtilMDE.join(toks, " ");
  }

  /**
   * Increase the indentation.
   */
  void increaseIndent() {
    indentCount += 2;
    indentText = indentText();
  }

  /**
   * Decrease the indentation.
   */
  void reverseIndent() {
    indentCount = indentCount - 2 > 0 ? indentCount - 2 : 0;
    indentText = indentText();
  }

  private String indentText() {
    String indentText = "";
    for (int i = 0; i < indentCount; i++) {
      indentText += "  ";
    }
    return indentText;
  }

  /**
   * Return the source for this declaration as lines of {@code Strings}
   * @return the list of lines in this declaration
   */
  abstract List<String> toLines();

  /**
   * Return this declaration as a {@code String}.
   *
   * @return the {@code String} for this declaration
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    for (String line : toLines()) {
      builder.append(line).append(Globals.lineSep);
    }
    return builder.toString();
  }
}
