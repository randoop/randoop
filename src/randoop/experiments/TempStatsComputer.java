package randoop.experiments;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import randoop.util.Files;

public class TempStatsComputer implements StatsComputer {

  static final int maxsize = 50;

  int[] seqs = new int[maxsize];

  Map<String,int[]> failures = new LinkedHashMap<String, int[]>();

  long[] gentime = new long[maxsize];
  long[] exectime = new long[maxsize];

  Map<String,int[]> times = new LinkedHashMap<String, int[]>();

  public boolean processOneRecord(StatsWriter writer) {

    int size = Math.min(writer.size, 49);
    seqs[size]++;
    gentime[size] += writer.gentime;
    exectime[size] += writer.exectime;

    for(int i = 0 ; i < writer.numclassifs ; i++) {

      // If not a failure, we're done processing record.
      if (writer.classifNames[i].equals("normal") ||
          writer.classifNames[i].equals("exception")) {
        assert writer.numclassifs == 1;
        return false;
      }

      String key = writer.classifNames[i] + "/" + writer.classifSources[i];
      int[] failuresForSize = failures.get(key);
      if (failuresForSize == null) {
        failuresForSize = new int[maxsize];
        failures.put(key, failuresForSize);
      }
      failuresForSize[size]++;
    }

    if (writer.failureswriter != null) {
      writeToFile(writer);
    }
    return true;
  }

  private void writeToFile(StatsWriter writer) throws Error {
    assert writer.failureswriter != null;
    for (int r = 0 ; r < writer.recordsize ; r++) {
      try {
        writer.failureswriter.append(writer.record[r] + "\n");
      } catch (IOException e) {
        throw new Error(e);
      }
    }
    try {
      writer.failureswriter.flush();
    } catch (IOException e) {
      throw new Error(e);
    }
  }

  public void results(String filename) {

    // Write sequence size histogram.
    StringBuilder b = new StringBuilder();
    b.append("[");
    for (int i = 0 ; i < seqs.length ; i++) {
      if (i > 0) b.append(", ");
      b.append(seqs[i]);
    }
    b.append("]\n");

    // Write generation time histogram.
    b.append("[");
    for (int i = 0 ; i < gentime.length ; i++) {
      if (i > 0) b.append(", ");
      b.append(gentime[i]);
    }
    b.append("]\n");

    // Write execution time histogram.
    b.append("[");
    for (int i = 0 ; i < exectime.length ; i++) {
      if (i > 0) b.append(", ");
      b.append(exectime[i]);
    }
    b.append("]\n");

    // Write failure histograms.
    for (Map.Entry<String, int[]> e : failures.entrySet()) {
      b.append(e.getKey() + "\n");
      b.append(Arrays.toString(e.getValue()) + "\n");
    }

    try {
      Files.writeToFile(b.toString(), new File(filename));
    } catch (IOException e) {
      throw new Error(e);
    }
  }
}
