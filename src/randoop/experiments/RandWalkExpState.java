package randoop.experiments;

import java.io.File;
import java.io.FilenameFilter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import randoop.util.Files;
// import plume.UtilMDE;

/**
 * Represents the state of an experiment in the random walk vs. Randoop
 * experiments.
 */
public class RandWalkExpState {
  
  private final int limit;
  private final String exp;
  private CombinedStats cs;
  private Random rand = new Random();
  private List<String> seedsToCount = new ArrayList<String>();
  private boolean countonly;
  
  public RandWalkExpState(int explimit, final String exp, boolean countonly) {
    if (!countonly && explimit < 0)
      throw new IllegalArgumentException("Invalid experiment limit: " + explimit);
    if (exp == null || exp.length() != 4 || !exp.matches("\\D\\D\\D\\D"))
      throw new IllegalArgumentException("Not an experiment descriptor string: " + exp);

    this.limit = explimit;
    this.exp = exp;
    this.countonly = countonly;

    File dir = new File(exp);
    if (!dir.isDirectory()) {
      throw new RuntimeException("Expected directory: " + dir);
    }

    File[] statfiles = dir.listFiles(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        if (name.endsWith(".stats")) {
          if (!name.startsWith(exp)) {
            throw new RuntimeException("Invalid experiment file found in directory " + dir + ": " + name);
          }
          return true;
        }
        return false;
      }
    });
    
    this.cs = new CombinedStats();
    
    for (File statfile : statfiles) {
      try {
        List<String> lines = Files.readWhole(statfile);
        cs.processOneSeed(lines);
      } catch (Exception e) {
        throw new Error(e);
      }
    }
    printOps();

  }
  
  private void printOps() {
    System.out.print(exp + " Total operations: " 
        + plume.UtilMDE.lpad(new DecimalFormat("###,###,###,###,###").format(cs.getTotalOperations()), 15));
    System.out.print("  ");
    System.out.println(exp + " Total sequences:  " 
        + plume.UtilMDE.lpad(new DecimalFormat("###,###,###,###,###").format(cs.getTotalSequences()), 15));
  }

  public String nextTarget() {
    
    if (countonly)
      return null;
    
    if (cs.getTotalOperations() < limit) {
      printOps();
      String lastSeed = RandomTargets.pad(rand.nextInt(100000));
      seedsToCount.add(lastSeed);
      return exp + "/" + exp +  lastSeed + ".data.gz";
    }
    return null;
  }
  
  public boolean hasMoreTargets() {
    
    if (countonly)
      return false;

    // Are there more targets? To find out, first check
    // if there are new stat files we can add to the totals.
    for (Iterator<String> it = seedsToCount.iterator() ; it.hasNext() ; ) {
      String seed = it.next();
      File statfile = new File(exp + "/" + exp +  seed + ".stats");
      if (statfile.exists()) {
        it.remove();
        try {
          List<String> lines = Files.readWhole(statfile);
          cs.processOneSeed(lines);
        } catch (Exception e) {
          throw new Error(e);
        }
      }
    }

    if (cs.getTotalOperations() < limit) {
      return true;
    }
    
    System.out.println("Reached operation limit for " + exp);
    return false;
  }
  
}
