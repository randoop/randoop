package randoop.experiments;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import randoop.util.Files;

/**
 * A target maker that takes as input a file name with one target per line, and
 * give back the targets (preprended with user-specified prefix/suffix) in the file.
 *
 * If prefix == "<none>" then no prefix is used. Same for suffix.
 */
public class OneTargetPerLine implements TargetMaker {

  String prefix;
  String suffix;
  List<String> targets;

  /**
   * First argument is the prefix, second argument is the name of the file
   * containing the targets.
   */
  public OneTargetPerLine(String[] args) {
    if (args.length != 3)
      throw new IllegalArgumentException("Argument length must be 3 but was " + args.length);

    prefix = args[0];
    if (prefix.equals("<none>")) {
      prefix = "";
    }

    suffix = args[1];
    if (suffix.equals("<none>")) {
      suffix = "";
    }

    try {
      targets = new ArrayList<String>(Files.readWhole(args[2]));
    } catch (IOException e) {
      throw new Error(e);
    }
  }

  public String getNextTarget() {
    if (targets.isEmpty())
      return null;
    return prefix + targets.remove(0) + suffix;
  }

  public boolean hasMoreTargets() {
    return !targets.isEmpty();
  }

  public int targetsLeft() {
    return targets.size();
  }

}
