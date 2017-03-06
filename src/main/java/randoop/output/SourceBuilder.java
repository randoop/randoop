package randoop.output;

import java.util.List;

import plume.UtilMDE;
import randoop.Globals;

/**
 * Common behavior for source builder classes.
 */
abstract class SourceBuilder {

  /** The current indent for this {@link randoop.output.SourceBuilder} */
  private String indent;

  /**
   * Creates a {@link randoop.output.SourceBuilder} with no indent.
   *
   */
  SourceBuilder() {
    this.indent = "";
  }

  /**
   * Creates a line from the given tokens with the current indent and separated by spaces.
   *
   * @param toks  the tokens to include on the line
   * @return the {@code String} constructed from the tokens
   */
  String createLine(String... toks) {
    return indent + UtilMDE.join(toks, " ");
  }

  /**
   * Increase the indent.
   */
  void indent() {
    indent = indent + "  ";
  }

  /**
   * Decrease the indent.
   */
  void reverseIndent() {
    if (!indent.isEmpty()) {
      indent = indent.substring(indent.length() - 2);
    }
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
