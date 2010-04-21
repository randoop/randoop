package randoop.experiments;

import java.util.ArrayList;
import java.util.List;

public class RandWalkExpMaker implements TargetMaker {

  private List<RandWalkExpState> experiments;

  /**
   * @param args
   *          Fist argument is either "count" or a number. Each argument is a 4-character
   *          experiment descriptor, like "fcjc" (fc=technique, jc=subject).
   */
  public RandWalkExpMaker(String[] args) {
    if (args.length < 2)
      throw new IllegalArgumentException("RandWalkExpMaker requires at least 2 arguments.");
    int explimit = -1;
    boolean countonly = false;
    if (args[0].equals("count")) {
      countonly = true;
    } else {
      explimit = Integer.parseInt(args[0]);
    }
    this.experiments = new ArrayList<RandWalkExpState>();
    for (int i = 1  ; i < args.length ; i++) {
      experiments.add(new RandWalkExpState(explimit, args[i], countonly));
    }
  }
  
  public String getNextTarget() {
    for (RandWalkExpState exp : experiments) {
      if (exp.hasMoreTargets()) {
        return exp.nextTarget();
      }
    }
    return null;
  }

  public boolean hasMoreTargets() {
    for (RandWalkExpState exp : experiments) {
      if (exp.hasMoreTargets()) {
        return true;
      }
    }
    return false;
  }

  public int targetsLeft() {
    return 1000000; // true value not known.
  }

}
