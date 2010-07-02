package randoop.main;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import randoop.Sequence;
import randoop.util.CollectionsExt;
import cov.Branch;
import cov.CoverageAtom;


public class CovUtils extends CommandHandler {

  private static String command = "cov";
  
  public CovUtils() {
    super(command, null, null, null, null, null, null, null, null, null);
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean handle(String[] args) throws RandoopTextuiException {
    
    if (args.length < 2) {
      System.out.println("Command " + command + " requires at least 2 arguments.");
      return false;
    }
    
    String subcommand = args[0];

    if (subcommand.equals("summary")) {
      Set<Branch> s1 = covset(args[1]);
      System.out.println("Set size: " + s1.size());
      return true;
    }
    
    if (subcommand.equals("intersection")) {
      Set<Branch> s1 = covset(args[1]);
      Set<Branch> s2 = covset(args[2]);
      System.out.println("Set 1 size: " + s1.size());
      System.out.println("Set 2 size: " + s2.size());
      System.out.println("Intersection size: " + CollectionsExt.intersection(s1, s2).size());
      return true;
    }

    if (subcommand.equals("union")) {
      Set<Branch> s1 = covset(args[1]);
      Set<Branch> s2 = covset(args[2]);
      System.out.println("Set 1 size: " + s1.size());
      System.out.println("Set 2 size: " + s2.size());
      System.out.println("Union size: " + CollectionsExt.union(s1, s2).size());
      return true;
    }
    
    System.out.println("Invalid sub-command:" + subcommand);
    return false;
  }

  @SuppressWarnings("unchecked")
  private static Set<Branch> covset(String filename) {

    Map<CoverageAtom,Set<Sequence>> inputmap =
      new LinkedHashMap<CoverageAtom, Set<Sequence>>();

      try {
        FileInputStream fileis = new FileInputStream(filename);
        ObjectInputStream objectis = new ObjectInputStream(new GZIPInputStream(fileis));
        inputmap = (Map<CoverageAtom, Set<Sequence>>) objectis.readObject();
        objectis.close();
        fileis.close();
      } catch (Exception e) {
        throw new Error(e);
      }
  
      Set<Branch> covset = new LinkedHashSet<Branch>();
      for (CoverageAtom ca : inputmap.keySet()) {
        covset.add((Branch) ca);
      }
      return covset;
  }
  
}
