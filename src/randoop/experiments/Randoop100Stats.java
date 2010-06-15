package randoop.experiments;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import plume.UtilMDE;
import randoop.util.CollectionsExt;

public class Randoop100Stats implements StatsComputer {

  public static String exp = null;

  public static int seqs = 0;
  public static int ops = 0;
  public static long gtime = 0;
  public static long etime = 0;
  public static int oplimit = 0;

  public static Map<Integer,Long> tot;
  public static Map<Integer,Long> num;

  public static int faults = 0;
  public static Set<String> reducedfaults = new LinkedHashSet<String>();

  static {
    tot = new LinkedHashMap<Integer,Long>();
    num = new LinkedHashMap<Integer,Long>();
    for (int i = 0 ; i < 60 ; i++) {
      tot.put(i,0L);
      num.put(i,0L);
    }
  }

  public boolean processOneRecord(StatsWriter writer) {

    seqs++;
    gtime += writer.gentime;
    etime += writer.exectime;

    if (exp.startsWith("om")) {
      ops += writer.size;
    } else {
      ops += 1;
    }

    num.put(writer.size, num.get(writer.size) + 1);
    tot.put(writer.size, tot.get(writer.size) + writer.exectime);

    for(int i = 0 ; i < writer.numclassifs ; i++) {

      String kind = writer.classifNames[i];
      String key = writer.classifNames[i] + "/" + writer.classifSources[i];

      // If not a failure, we're done processing record.
      if (kind.equals("normal") || kind.equals("exception")) {
        assert writer.numclassifs == 1;
        break;
      }

      if (kind.equals("StatementThrowsNPE")) {
        break;
      }

      faults++;
      reducedfaults.add(key);

    }
    if (ops >= oplimit) {
      return false;
    }
    return true;
  }

  private static int occurrences(String fault, Map<String,int[]> data) {
    assert data.containsKey(fault);
    int[] occs = data.get(fault);
    assert occs != null;
    int sum = 0;
    for (int i = 0 ; i < occs.length ; i++) {
      sum += occs[i];
    }
    return sum;
  }

  // Unchecked warning in this method due to mixing varargs and generics.
  @SuppressWarnings("unchecked")
  public static void main(String[] args) throws IOException {

    CombinedStats omjc_data = CombinedStats.readFromMFile("matlab/omjc_data.m");
    // CombinedStats fcjc_data = CombinedStats.readFromMFile("matlab/fcjc_data.m");
    CombinedStats fdjc_data = CombinedStats.readFromMFile("matlab/fdjc_data.m");

    CombinedStats omcc_data = CombinedStats.readFromMFile("matlab/omcc_data.m");
    // CombinedStats fccc_data = CombinedStats.readFromMFile("matlab/fccc_data.m");
    CombinedStats fdcc_data = CombinedStats.readFromMFile("matlab/fdcc_data.m");

    CombinedStats ompr_data = CombinedStats.readFromMFile("matlab/ompr_data.m");
    // CombinedStats fcpr_data = CombinedStats.readFromMFile("matlab/fcpr_data.m");
    CombinedStats fdpr_data = CombinedStats.readFromMFile("matlab/fdpr_data.m");

    CombinedStats omma_data = CombinedStats.readFromMFile("matlab/omma_data.m");
    // CombinedStats fcma_data = CombinedStats.readFromMFile("matlab/fcma_data.m");
    CombinedStats fdma_data = CombinedStats.readFromMFile("matlab/fdma_data.m");

    CombinedStats omjf_data = CombinedStats.readFromMFile("matlab/omjf_data.m");
    // CombinedStats fcjf_data = CombinedStats.readFromMFile("matlab/fcjf_data.m");
    CombinedStats fdjf_data = CombinedStats.readFromMFile("matlab/fdjf_data.m");

    CombinedStats omtr_data = CombinedStats.readFromMFile("matlab/omtr_data.m");
    // CombinedStats fctr_data = CombinedStats.readFromMFile("matlab/fctr_data.m");
    CombinedStats fdtr_data = CombinedStats.readFromMFile("matlab/fdtr_data.m");


    Map<String,int[]> oms = new LinkedHashMap<String, int[]>();
    oms.putAll(omjc_data.data);
    oms.putAll(omcc_data.data);
    oms.putAll(ompr_data.data);
    oms.putAll(omma_data.data);
    oms.putAll(omjf_data.data);
    oms.putAll(omtr_data.data);

    Map<String,int[]> fds = new LinkedHashMap<String, int[]>();
    fds.putAll(fdjc_data.data);
    fds.putAll(fdcc_data.data);
    fds.putAll(fdpr_data.data);
    fds.putAll(fdma_data.data);
    fds.putAll(fdjf_data.data);
    fds.putAll(fdtr_data.data);

//     System.out.println("OMS:" + oms.size());
//     System.out.println("FDS:" + fds.size());
    for (String s : CollectionsExt.union(oms.keySet(), fds.keySet())) {
      System.out.print(oms.containsKey(s)? occurrences(s, oms) : 0);
      System.out.print(" ");
      System.out.println(fds.containsKey(s)? occurrences(s, fds) : 0);
    }

    if (args.length != 2) {
      System.out.println("Must give two arguments.");;
      System.exit(1);
    }
    if (args[0].length() != 4) {
      System.out.println("Argument string not an experiment description string.");;
      System.exit(1);
    }

    oplimit = Integer.parseInt(args[1]);

    exp = args[0];
    File dir = new File(exp);
    if (!dir.isDirectory()) {
      System.out.println("Expected directory: " + dir);
      System.exit(1);
    }

    for (File datafile :
           dir.listFiles(
                    new FilenameFilter() {
                      public boolean accept(File dir, String name) {
                        return name.endsWith(".data.gz");
                      }
                    })) {

      if (ops >= oplimit) break;

      StatsWriter s = new StatsWriter();
      Randoop100Stats comp = new Randoop100Stats();

      BufferedReader br = UtilMDE.bufferedFileReader(datafile.getAbsolutePath());
      try {
        for (;;) {
          if (!s.read(br, comp)) {
            break;
          }
        }
      } catch (Error e) {
        System.out.println("Error while reading " + datafile.getAbsolutePath());
        throw new Error(e);
      }
    }

    System.out.print(" " + args[0]);
    System.out.print(" " + seqs);
    System.out.print(" " + ops);
    System.out.print(" un " + faults);
    System.out.print(" " + reducedfaults.size());
    System.out.print(" \\\\");
    System.out.println();

  }
}
