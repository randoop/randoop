package randoop.output;

import plume.UtilMDE;
import randoop.Globals;

/**
 * Created by bjkeller on 3/2/17.
 */
public class SourceBuilder {
  private StringBuilder builder;

  SourceBuilder() {
    builder = new StringBuilder();
  }

  void appendLine(String... toks) {
    builder.append(UtilMDE.join(toks, " ")).append(Globals.lineSep);
  }

  @Override
  public String toString() {
    return builder.toString();
  }
}
