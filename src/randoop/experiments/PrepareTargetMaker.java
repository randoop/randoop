package randoop.experiments;

import java.util.ArrayList;
import java.util.List;

public class PrepareTargetMaker implements TargetMaker {

  List<String> experiments;

  public PrepareTargetMaker(String[] args) {
    experiments = new ArrayList<String>();
    for (String a : args) {
      experiments.add("prepare-" + a);
    }
  }

  public String getNextTarget() {
    if (experiments.isEmpty())
      return null;
    return experiments.remove(0);
  }

  public boolean hasMoreTargets() {
    return !experiments.isEmpty();
  }

  public int targetsLeft() {
    return experiments.size();
  }

}
