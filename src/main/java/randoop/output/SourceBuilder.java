package randoop.output;

import java.util.List;
import org.plumelib.util.UtilPlume;
import randoop.Globals;

/** Common behavior for source builder classes. */
abstract class SourceBuilder {

  /**
   * Creates a line from the given tokens with the current indentation and separated by spaces.
   *
   * @param toks the tokens to include on the line
   * @return the {@code String} constructed from the tokens
   */
  String createLine(String... toks) {
    return UtilPlume.join(" ", toks);
  }

  /**
   * Return the source for this declaration as lines of {@code Strings}
   *
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
