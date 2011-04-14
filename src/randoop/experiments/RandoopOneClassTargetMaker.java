package randoop.experiments;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import randoop.util.Files;

public class RandoopOneClassTargetMaker implements TargetMaker {

  private List<String> targets;

  /**
   * 
   * @param args first argument is a prefix. Second argument is an experiment name.
   * @throws IOException
   */
  public RandoopOneClassTargetMaker(String[] args) throws IOException {
    if (args.length < 2)
      throw new IllegalArgumentException("Must give at least two arguments.");
    String prefix = args[0];
    for (int i = 1 ; i < args.length ; i++) {
      ExperimentBase exp = new ExperimentBase("experiments/" + args[i] + ".experiment");
      List<String> classNames = Files.readWhole(exp.targetClassListFile);

      targets = new ArrayList<String>();
      for (String className : classNames) {
        String target = prefix + "-" + args[i] + "-" + className;
        System.out.println("Adding target: " + target);
        targets.add(target);
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
