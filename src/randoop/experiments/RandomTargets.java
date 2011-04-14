package randoop.experiments;

import java.util.Random;

public class RandomTargets implements TargetMaker {

  String prefix;
  String suffix;
  int maxtargets;
  int targets;

  Random rand = new Random();

  /**
   * First argument is the prefix, second argument is the name of the file
   * containing the targets.
   */
  public RandomTargets(String[] args) {
    if (args.length != 3)
      throw new IllegalArgumentException("Argument length must be 3 but was " + args.length);

    prefix = args[0];
    suffix = args[1];
    maxtargets = Integer.parseInt(args[2]);
    targets = 0;
  }

  public String getNextTarget() {
    if (hasMoreTargets()) {
      targets++;
      return prefix + pad(rand.nextInt(100000)) + suffix;
    }
    return null;
  }
  
  // Adds 0's to the left of the given num
  // to make it a 5-character string.
  static String pad(int num) {
    assert num < 100000;
    String str = Integer.toString(num);
    while (str.length() < 5) {
      str = "0" + str;
    }
    return str;
  }

  public boolean hasMoreTargets() {
    return (targets + 1 <= maxtargets);
  }

  public int targetsLeft() {
    return maxtargets - targets;
  }

}
