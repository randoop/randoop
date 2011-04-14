package randoop.experiments;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import randoop.util.Files;

public class CombinedStats {

  public Map<String,int[]> data = new LinkedHashMap<String, int[]>();
  private int elems = 50;
  private int[] seqs = new int[elems];
  private long[] gentime = new long[elems];
  private long[] exectime = new long[elems];
  
  public static void main(String[] args) throws IOException {
    if (args.length != 2) {
      System.out.println("Expected 2 arguments.");
      System.exit(1);
    }
    
    int max_ops = Integer.parseInt(args[0]); 

    String exp = args[1];
    File dir = new File(exp);
    if (!dir.isDirectory()) {
      System.out.println("Expected directory: " + dir);
      System.exit(1);
    }

   File[] files = dir.listFiles(new FilenameFilter() {
       public boolean accept(File dir, String name) {
         if (name.endsWith(".stats")) {
           System.out.println(name);
           return true;
         }
         return false;
       }
     });

    System.out.println((files.length - 1) + " files to process.");
    
    CombinedStats cs = new CombinedStats();

    for (int i = 1 ; i < files.length ; i++) {
      List<String> lines = Files.readWhole(files[i]);
      cs.processOneSeed(lines);
      if (cs.getTotalOperations() > max_ops) {
        break;
      }
    }

    StringBuilder allsizes = new StringBuilder(exp + ".s = [ ");
    for (int i : cs.seqs) {
      allsizes.append(i + " ");
    }
    allsizes.append("];\n");

    StringBuilder gentimeb = new StringBuilder(exp + ".gt = [ ");
    for (long i : cs.gentime) {
      gentimeb.append(i + " ");
    }
    gentimeb.append("];\n");

    StringBuilder exectimeb = new StringBuilder(exp + ".et = [ ");
    for (long i : cs.exectime) {
      exectimeb.append(i + " ");
    }
    exectimeb.append("];\n");

    StringBuilder descriptions = new StringBuilder(exp + ".n = { ");
    StringBuilder numbers = new StringBuilder(exp + ".f = [ ");
    for (Map.Entry<String, int[]> e : cs.data.entrySet()) {
      descriptions.append("'" + e.getKey() + "' ");
      for (int i : e.getValue()) {
        numbers.append(i + " ");
      }
      numbers.append("; ");
    }
    descriptions.append("};\n");
    numbers.append("];");
    String out =
      allsizes.toString() +
      gentimeb.toString() +
      exectimeb.toString() +
      descriptions.toString() +
      numbers.toString();

    Files.writeToFile(out, "matlab/" + exp + "_data.m");
  }
  
  

  public void processOneSeed(List<String> lines) {

    // Remove any empty lines.
    List<String> lines2 = new ArrayList<String>();
    for (String l : lines) {
      if (l.trim().length() != 0) {
        lines2.add(l);
      }
    }

    assert lines2.size() % 2 == 1 : lines2.size(); // odd number of lines.

    // First line is sequence sizes data.
    int[] sizesarray = readArray(lines2.get(0), ",");
    for (int i = 0 ; i < seqs.length ; i++) {
      seqs[i] += sizesarray[i];
    }
    
    // Currently have two formats, one with timing information and one without.
    if (lines2.size() > 1 && lines2.get(1).startsWith("[")) {
      // data has two lines with timing information.
      // Second line is generation time.
      long[] gentimearray = readArrayLong(lines2.get(1));
      for (int i = 0 ; i < seqs.length ; i++) {
        gentime[i] += gentimearray[i];
      }

      // Third line is execution time.
      long[] exectimearray = readArrayLong(lines2.get(2));
      for (int i = 0 ; i < seqs.length ; i++) {
        exectime[i] += exectimearray[i];
      }
    } else {
      // data has no timing information. Empty body.
    }

    for (int i = 3 ; i < lines2.size() ; i = i + 2) {
      // Read description.
      String desc = lines2.get(i);
      assert desc.length() != 0;
      String array = lines2.get(i + 1);
      int[] intarray = readArray(array, ",");
      // Add the numbers to the totals for the given description.
      int[] totals = data.get(desc);
      if (totals == null) {
        totals = new int[elems];
        data.put(desc, totals);
      }
      assert totals.length == intarray.length;
      for (int a = 0 ; a < totals.length ; a++) {
        totals[a] += intarray[a];
      }
    }
  }

  public static CombinedStats readFromMFile(String filename) {
    
    assert filename.startsWith("matlab/"); 
    assert filename.endsWith("_data.m");
    String exp = filename.substring("matlab/".length(), "matlab/".length() + 4);
    
    List<String> lines;
    try {
      lines = Files.readWhole(filename);
    } catch (IOException e) {
      throw new Error(e);
    }
    assert lines.size() == 5;
    
    // Read failures.
    List<String> failures = new ArrayList<String>();
    String failuresLn = lines.get(3);
    assert failuresLn.startsWith(exp + ".n = { "); // length 11
    assert failuresLn.endsWith(" };"); // length 3
    for (String s : failuresLn.substring(11, failuresLn.length() - 3).split("\\s")) {
      assert s.startsWith("'") & s.endsWith("'") : s;
      failures.add(s = s.substring(1, s.length() - 1));
    }
    
   String failureCountsLn = lines.get(4);
   assert failureCountsLn.startsWith(exp + ".f = [ "); // length 11
   assert failureCountsLn.endsWith(" ; ];"); // length 5
   failureCountsLn = failureCountsLn.substring(11, failureCountsLn.length() - 5);
   
   CombinedStats c = new CombinedStats();

   List<int[]> counts = c.read2darray(failureCountsLn);
   
   assert counts.size() == failures.size();
   
   c.data = new LinkedHashMap<String, int[]>();
   
   for (int i = 0 ; i < failures.size() ; i++) {
     
     if (failures.get(i).startsWith("StatementThrowsNPE"))
       continue;
     
     c.data.put(failures.get(i), counts.get(i));
   }
   
   return c;
  }
  
  // [ 1 2 ; 3 4 ; 5 6 ] to an int[][] array.
  private List<int[]> read2darray(String array) {
    
    String[] rows = array.split("\\s;\\s");
    assert rows.length > 0;
    List<int[]> ret = new ArrayList<int[]>();
    for (int i = 0 ; i < rows.length ; i++) {
      ret.add(readArray("[" + rows[i] + "]", "\\s"));
    }
    return ret;
  }
  
  // [1, 2, 3] to an int array.
  private int[] readArray(String array, String sep) {
      assert array.charAt(0) == '[' && array.charAt(array.length() -1 ) == ']' : array;
      array = array.substring(1, array.length() - 1);
      String[] arrayels = array.split(sep);
      assert arrayels.length > 0;
      if (elems == -1) {
        // First time we've read an array. Initialize fields
        // based on length (all arrays should have same length).
        elems = arrayels.length;
        seqs = new int[elems];
        gentime = new long[elems];
        exectime = new long[elems];
      }
      if (arrayels.length != elems) {
        throw new IllegalArgumentException("Expected " + elems + " elements but got "
            + arrayels.length + " in line: " + array);
      }
      int[] intarray = new int[arrayels.length];
      for (int a = 0 ; a < arrayels.length ; a++) {
        intarray[a] = Integer.parseInt(arrayels[a].trim());
      }
      return intarray;
  }

  // [1, 2, 3] to a long array. Could merge with int array...
  private long[] readArrayLong(String array) {
      assert array.charAt(0) == '[' && array.charAt(array.length() -1 ) == ']' : array;
      array = array.substring(1, array.length() - 1);
      String[] arrayels = array.split(",");
      assert arrayels.length > 0;
      if (arrayels.length != elems) {
        throw new IllegalArgumentException("Expected " + elems + " elements but got "
            + arrayels.length + " in line: " + array);
      }
      long[] longarray = new long[arrayels.length];
      for (int a = 0 ; a < arrayels.length ; a++) {
        longarray[a] = Long.parseLong(arrayels[a].trim());
      }
      return longarray;
  }

  public int getTotalSequences() {
    int total = 0;
    for (int i = 0 ; i < seqs.length ; i++) {
      total += seqs[i];
    }
    return total;
  }
  
  public int getTotalOperations() {
    int total = 0;
    for (int i = 0 ; i < seqs.length ; i++) {
      total += seqs[i] * (i + 1); // i+1 == operations per seq. of length i+1.
    }
    return total;
  }
}
