package randoop.experiments;

import java.util.ArrayList;
import java.util.List;

/**
 * A target maker that creates targets by preprending the given
 * prefixes to the given arguments (one target per prefix/argument
 * pair).
 *
 * A prefix is an argument by the string "prefix=". All other
 * are considered regular arguments.
 */
public class OneTargetPerArgument implements TargetMaker {

  List<String> prefixes = new ArrayList<String>();

  List<String> targets;

  public OneTargetPerArgument(String[] args2) {

    // Parse options.
    List<String> args = new ArrayList<String>();
    for (String arg : args2) {
      if (arg.startsWith("prefix=")) {
        prefixes.add(arg.substring("prefix=".length()));
      } else {
        args.add(arg);
      }
    }

    if (args.size() == 0)
      throw new IllegalArgumentException("Argument length must be at least 0.");

    if (prefixes.size() == 0)
      throw new IllegalArgumentException("Must specify at least one prefix.");

    targets = new ArrayList<String>();
    for (String p : prefixes) {
      for (String s : args) {
        targets.add(p + s);
      }
    }
  }

  public String getNextTarget() {
    if (targets.isEmpty())
      return null;
    return targets.remove(0);
  }

  public boolean hasMoreTargets() {
    return !targets.isEmpty();
  }

  public int targetsLeft() {
    return targets.size();
  }

}
