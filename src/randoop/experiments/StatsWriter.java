package randoop.experiments;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import plume.Option;
import plume.Options;
import plume.UtilMDE;
import plume.Options.ArgException;
import randoop.ExecutableSequence;
import randoop.FailureSet;

public class StatsWriter {

  private static int recordsread = 0;
  private static int lines = 0;

  @Option("Write failing tests to file")
  public static boolean writefailures = false;

  public BufferedWriter failureswriter = null;

  public static void main(String[] args2) throws IOException {

    Options options = new Options(StatsWriter.class);
    String[] args = null;
    try {
      args = options.parse(args2);
    } catch (ArgException e) {
      throw new Error(e);
    }

    if (args.length != 1) {
      System.out.println("Must give a single argument.");;
      System.exit(1);
    }

    String exp = args[0];
    String dir = exp.substring(0,4);

    // expfile should be something like "fcjc12345"
    if (!exp.matches("\\D\\D\\D\\D\\d\\d\\d\\d\\d")) {
      throw new IllegalArgumentException("Invalid experiment file name: " + exp);
    }

    StatsWriter s = new StatsWriter();
    TempStatsComputer comp = new TempStatsComputer();

    if (writefailures) {
      try {
        s.failureswriter =
          UtilMDE.bufferedFileWriter(dir + "/" + exp + ".failures.gz");
      } catch (IOException e) {
        throw new Error(e);
      }
    }

    System.out.print(".");
    String datafile = dir + "/" + exp + ".data.gz";
    BufferedReader br = UtilMDE.bufferedFileReader(datafile);
    try {
    for (;;) {
      if (!s.read(br, comp)) {
        break;
      }
    }
    } catch (Error e) {
      System.out.println("Error while reading " + datafile);
      throw new Error(e);
    }

    System.out.println("Read " + recordsread + " records.");
    comp.results(dir + "/" + exp + ".stats");

    if (s.failureswriter != null) {
      s.failureswriter.close();
    }
  }

  // Keep in synch with read method below.
  public static void write(FileWriter writer, ExecutableSequence seq, FailureSet fa) throws IOException {
    StringBuilder b = new StringBuilder();

    // Number of executed statements ("real" size).
    b.append(seq.executedSize() + "\n");

    // Generation time.
    b.append(seq.gentime + "\n");

    // Execution time.
    b.append(seq.exectime + "\n");

    // Sequence. Output only if failure.
    if (fa.getFailures().size() > 0) {
      b.append(seq.sequence.toParseableString(";"));
      b.append("\n");
    } else {
      b.append("\n");
    }

    // Compute classifications.
    StringBuilder classif = new StringBuilder();
    int numclassifications = 0;

    if (fa.getFailures().size() > 0) {
      for (FailureSet.Failure f : fa.getFailures()) {
        classif.append(f.viocls.getSimpleName() + "\n");
        classif.append(f.st + "\n");
        classif.append("0" + "\n"); // Means nothing but keeping for backwards compatibility.
        numclassifications++;
      }
    } else if (seq.isNormalExecution()) {
      classif.append("normal\nn/a\n");
      classif.append(-1 + "\n");
      numclassifications = 1;
    } else {
      // int idx = seq.getExceptionIndex(Throwable.class);
      // assert idx >= 0 : seq;// TODO this fails for FDRT sometimes.
      classif.append("exception\nn/a\n");
      classif.append(-1 + "\n");
      numclassifications = 1;
    }

    // Number of classifications.
    b.append(numclassifications + "\n");

    // Classifications.
    b.append(classif.toString());

    writer.write(b.toString());
  }

  private static int maxclassifs = 10;
  public int size = -1;
  public int depsize = -1;
  public long gentime;
  public long exectime;
  public String seq_str = null;
  public int numclassifs = -1;
  public String[] classifNames = new String[maxclassifs];
  public String[] classifSources = new String[maxclassifs];
  public int[] classifdepsizes = new int[maxclassifs];

  public String[] record = new String[300]; // TODO parameterize.
  public int recordsize;

  // Keep in sync with write method above.
  public boolean read(BufferedReader reader, StatsComputer comp) throws IOException {
    recordsread++;
    String line = null;

    recordsize = 0;
    try {

      // Number of executed statements.
      size = Integer.parseInt(readLine(reader));

      // Read the next 2 lines to determine if we're parsing new or old file.
      String l1 = readLine(reader);
      String l2 = readLine(reader);

      if (l2.charAt(0) == 'v') {
        // Old file; only one number preceding (depsize) the sequence.
        depsize = Integer.parseInt(l1);
        // Sequence.
        seq_str = l2;
      } else {
        // New file; two numbers predecing the sequence.
        // Generation time.
        gentime = Long.parseLong(l1);
        // Execution time.
        exectime = Long.parseLong(l2);
        // Sequence.
        seq_str = readLine(reader);
      }

      numclassifs = Integer.parseInt(readLine(reader));
      for (int i = 0 ; i < numclassifs ; i++) {

        // Read one classification.
        // Read name.
        classifNames[i] = readLine(reader);
        // Read source.
        classifSources[i] = readLine(reader);
        // Read dep size.
        classifdepsizes[i] = Integer.parseInt(readLine(reader));
      }

    } catch (EndOfFile e) {
      return false;
    } catch (Exception e) {
      System.out.println("Error while reading line:" + line);
      System.out.println("(records read=" + recordsread + ", lines = " + lines + ")");
      throw new Error(e);
    }

    return comp.processOneRecord(this);
  }

  private String readLine(BufferedReader reader) throws EndOfFile, IOException {
    String ret = reader.readLine();
    if (ret == null) {
      throw new EndOfFile();
    }
    lines++;
    record[recordsize++] = ret;
    return ret;
  }

  public static class EndOfFile extends Exception {
    private static final long serialVersionUID = 1L;
  }
}
