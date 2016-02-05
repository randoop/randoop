package randoop.main;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;

import plume.Option;
import plume.Options;
import plume.Options.ArgException;

import randoop.RandoopClassLoader;
import randoop.experiments.DataFlowInput;
import randoop.sequence.Sequence;
import randoop.sequence.Statement;
import randoop.types.TypeNames;
import randoop.util.Files;

import cov.Branch;
import cov.Coverage;
import cov.CoverageAtom;
import javassist.ClassPool;

public class ComputeFrontierBranches {

  @Option("Name of a covmap file")
  public static String input_map = null;

  @Option("If true, prints code representation of frontier sequences as comments in output file")
  public static boolean print_coderep_comments = false;

  @Option("For each frontier branch / reaching method, output at most N sequences")
  public static int seqs_per_method = 1000000;

  @Option("Print strings used by parallel DF execution")
  public static String experiment = null;

  @SuppressWarnings("unchecked")
  public static void main(String[] args) {

    Options options = new Options(ComputeFrontierBranches.class);

    try {
      String[] nonargs = options.parse(args);
      if (nonargs.length > 0)
        throw new ArgException("Unrecognized arguments: "
            + Arrays.toString(nonargs));
    } catch (ArgException ae) {
      System.out
      .println("ERROR while parsing command-line arguments (will exit): "
          + ae.getMessage());
      System.exit(-1);
    }
    if (input_map == null) {
      System.out.println("ERROR: must give --input-map argument.");
      System.exit(1);
    }
    if (experiment == null) {
      System.out.println("ERROR: must give --experiment argument.");
      System.exit(1);
    }

    ClassLoader contextLoader = ComputeFrontierBranches.class.getClassLoader();
    TypeNames.setClassLoader(new RandoopClassLoader(contextLoader, ClassPool.getDefault(), new TreeSet<String>()));

    Map<CoverageAtom,Set<Sequence>> covmap = new LinkedHashMap<CoverageAtom, Set<Sequence>>();

    System.out.print("Reading coverage map...");

    try {
      FileInputStream fileis = new FileInputStream(input_map);
      ObjectInputStream objectis = new ObjectInputStream(new GZIPInputStream(fileis));
      covmap = (Map<CoverageAtom, Set<Sequence>>) objectis.readObject();
      objectis.close();
      fileis.close();
    } catch (Exception e) {
      throw new Error(e);
    }

    System.out.println("done.");
    System.out.print("Computing frontier branches...");

    // We will create a sorted map to ensure that printing out the
    // sets will always result in the same output. This help with regression
    // testing.
    Comparator<Branch> branchComparator = new Comparator<Branch>() {
      @Override
      public int compare(Branch o1, Branch o2) {
        return o1.toString().compareTo(o2.toString());
      }
    };

    int frontierCounter = 1;
    List<String> targetstrings = new ArrayList<String>();

    for (CoverageAtom ca : covmap.keySet()) {

      // Maps a branch to a set of sequences that cover the branch.
      Map<Branch, Set<Sequence>> frontierMap =
        new TreeMap<Branch, Set<Sequence>>(branchComparator);

      Branch br = (Branch)ca;

      // Only put frontier branches that are inside methods or constructors.
      if (Coverage.getMemberContaining(br) == null)
        continue;

      // Both branches covered; not a frontier branch.
      if (covmap.keySet().contains(br.getOppositeBranch()))
        continue;

      Set<Sequence> candidates = covmap.get(br);
      assert candidates != null;
      assert !candidates.isEmpty();

      Map<Statement,Integer> statements = new LinkedHashMap<Statement, Integer>();

      Set<Sequence> ss = new LinkedHashSet<Sequence>();

      for (Sequence s : candidates) {
        Statement st = s.getLastStatement();
        Integer count = statements.get(st);
        if (count == null) {
          count = 0;
        }
        if (count == seqs_per_method)
          break;
        ss.add(s);
        statements.put(st, count + 1);
      }

      frontierMap.put(br, ss);

      DataFlowInput input = new DataFlowInput(frontierMap);

      String filename = "frontier" + frontierCounter++ + ".gz";

      targetstrings.add(experiment + "-" + filename);

      input.toParseableFile(filename, print_coderep_comments);

      System.out.print(".");
    }
    System.out.println("done.");

    if (experiment != null) {
      String targetsfile = experiment + ".dftargets.txt";
      System.out.print("Printing targets for parallel DF execution to " + targetsfile + "...");
      try {
        Files.writeToFile(targetstrings, targetsfile);
      } catch (IOException e) {
        throw new Error(e);
      }
      System.out.println("done.");

    }
  }

}
