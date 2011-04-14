package randoop.experiments;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import randoop.Sequence;
import plume.Option;
import plume.Options;
import plume.Options.ArgException;
import cov.CoverageAtom;

/**
 * Used to combine the coverage results of multiple Randoop runs.
 * 
 * Input: a list of serialized objects of type
 *        Map<CoverageAtom,Set<Sequence>>
 *        
 * Output: a serialized object of the above type
 *         that combines all the entries from the inputs.
 */
public class CombineCovMaps {
  
  @Option("Input map")
  public static List<String> inputmap = new ArrayList<String>();
  
  @Option("Output map")
  public static String outputmap = null;

  @SuppressWarnings("unchecked")
  public static void main(String[] args) {
    
    Options options = new Options(CombineCovMaps.class);
    
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
    
    if (inputmap.size() == 0) {
      System.out.println("ERROR: you must specify at least one --inputmap argument.");
      System.exit(1);
    }
    if (outputmap == null) {
      System.out.println("ERROR: you must specify an --outputmap argument.");
      System.exit(1);
    }
    
    Map<CoverageAtom, Set<Sequence>> newmap = new LinkedHashMap<CoverageAtom, Set<Sequence>>();
 
    for (String arg : inputmap) {
      if (!arg.endsWith(".covmap.gz")) throw new IllegalArgumentException(arg);
      Map<CoverageAtom,Set<Sequence>> onemap = null;
      try {
        FileInputStream fileis = new FileInputStream(arg);
        ObjectInputStream objectis = new ObjectInputStream(new GZIPInputStream(fileis));
        onemap = (Map<CoverageAtom, Set<Sequence>>) objectis.readObject();
        objectis.close();
        fileis.close();
      } catch (Exception e) {
        throw new Error(e);
      }
      
      for (Map.Entry<CoverageAtom, Set<Sequence>> entry : onemap.entrySet()) {
        CoverageAtom covatom = entry.getKey();
        
        Set<Sequence> seqs = newmap.get(covatom);
        if (seqs == null) {
          seqs = new LinkedHashSet<Sequence>();
          newmap.put(covatom, seqs);
        }
        seqs.addAll(entry.getValue());
      }
    }

    System.out.println("+++ COMBINED BRANCHES: " + newmap.keySet().size());
    
    try {
      FileOutputStream fileos = new FileOutputStream(outputmap);
      ObjectOutputStream objectos = new ObjectOutputStream(new GZIPOutputStream(fileos));
      objectos.writeObject(newmap);
      objectos.close();
      fileos.close();
    } catch (Exception e) {
      throw new Error(e);
    }
  }
}
