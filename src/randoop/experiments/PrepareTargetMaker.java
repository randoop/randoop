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

  @Override
  public String getNextTarget() {
    if (experiments.isEmpty())
      return null;
    return experiments.remove(0);
  }

  @Override
  public boolean hasMoreTargets() {
    return !experiments.isEmpty();
  }

  @Override
  public int targetsLeft() {
    return experiments.size();
  }

}
