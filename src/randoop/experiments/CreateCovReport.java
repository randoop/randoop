package randoop.experiments;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import randoop.Sequence;
import randoop.util.Reflection;
import plume.Option;
import plume.Options;
import plume.Options.ArgException;
import cov.Branch;
import cov.Coverage;
import cov.CoverageAtom;

/**
 * Creates a textual report of the coverage achieved for a collections
 * of classes.
 */
public class CreateCovReport {
  
  @Option("Input coverage map")
  public static String input_map = null;
  
  @Option("File with list of coverage-instrumented classes")
  public static String input_cov_class_list = null;
  
  @Option("Output file (will output a text file)")
  public static String output_report = null;
  
  @SuppressWarnings("unchecked")
  public static void main(String[] args) {
    
    // Parse options and ensure that a scratch directory was specified.
    Options options = new Options(CreateCovReport.class);
    try {
      options.parse(args);
    } catch (ArgException e) {
      throw new Error(e);
    }
    if (input_map == null) {
      System.out.println("ERROR: missing required argument --input-map.");
      System.exit(1);
    }
    if (input_cov_class_list == null) {
      System.out.println("ERROR: missing required argument --input-cov-class-list.");
      System.exit(1);
    }
    if (output_report == null) {
      System.out.println("ERROR: missing required argument --output-report.");
      System.exit(1);      
    }

    // Read list of coverage-instrumented classes.
    List<Class<?>> covClasses = new ArrayList<Class<?>>();
    File covClassesFile = new File(input_cov_class_list);
    try {
      covClasses = Reflection.loadClassesFromFile(covClassesFile);
    } catch (IOException e) {
      throw new Error(e);
    }
    for (Class<?> cls : covClasses) {
      assert Coverage.isInstrumented(cls) : cls.toString();
    }
    
    // Read coverage map.
    Map<CoverageAtom, Set<Sequence>> covmap = null;
    try {
      FileInputStream fileis = new FileInputStream(input_map);
      ObjectInputStream objectis = new ObjectInputStream(new GZIPInputStream(fileis));
      covmap = (Map<CoverageAtom, Set<Sequence>>) objectis.readObject();
      objectis.close();
      fileis.close();
    } catch (Exception e) {
      throw new Error(e);
    }
    
    // Touch all covered branches (they may have been reset during generation).
    for (CoverageAtom br : covmap.keySet()) {
      Coverage.touch((Branch) br);
    }
    
    // Output report.
    Set<String> sourceFilesProcessed = new LinkedHashSet<String>();
    try {
      BufferedWriter writer = new BufferedWriter(new FileWriter(output_report));
      for (Class<?> cls : covClasses) {
        
        String filename = Coverage.getSourceFileName(cls);
        if (sourceFilesProcessed.contains(filename))
          continue;

        sourceFilesProcessed.add(filename);
        
        for (String s : Coverage.getCoverageAnnotatedSource(cls)) {
          writer.append(s);
          writer.newLine();
        }
      }
      writer.close();
    } catch (IOException e) {
      throw new Error(e);
    }
  }
}
