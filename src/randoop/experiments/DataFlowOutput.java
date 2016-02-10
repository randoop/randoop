package randoop.experiments;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import plume.UtilMDE;
import randoop.Globals;
import randoop.util.RecordListReader;
import randoop.util.RecordProcessor;

/**
 * The output created by DataFlow consists of a collection of results, one for
 * each sequence. Each result is a DFResultsOneSeq. A DFResultsOneSeq contains
 * the sequence analyzed, the frontier branch that the sequence reaches, and the
 * set of "interesting" variables affecting the outcome of the frontier branch.
 */
public class DataFlowOutput implements Serializable {

  private static final long serialVersionUID = -4008574058179157696L;

  public List<DFResultsOneSeq> results;

  public DataFlowOutput(List<DFResultsOneSeq> results) {
    this.results = results;
  }

  /**
   * Create a new DataFlowOutput from a test file.
   * 
   * The text file consists of a list of records, each as follows:
   * 
   * START DFRESULT <em>branch-description</em> <em>sequence-description</em>
   * VARS <em>num-vars</em> <em>var</em> <em>val</em> ... <em>val</em> ...
   * <em>var</em> <em>val</em> ... <em>val</em> END DFRESULT
   * 
   * Where:
   * 
   * <em>branch-description</em> is a string that can be parsed by the method
   * cov.OneBranchInfo.parse(String).
   * 
   * <em>sequence-description</em> is a string that can be parsed by the method
   * randoop.SequenceParser.parse(String).
   * 
   * <em>num-vars</em> is the number of "interesting" variables in the sequence.
   * 
   * Each <em>var</em> <em>val</em> ... <em>val</em> is an interesting variable
   * followed by interesting values for the variable, all on a single line and
   * separated with whitespace. There are <em>num-vars</em> lines.
   * 
   * Blank lines and comments between records are allowed and ignored. Comments
   * are lines starting with "#".
   * 
   * The records are used to create the list of DFResultsOneSeq objects.
   * 
   * TODO potential problems: (1) last statement is sequence begins with "VARS";
   * (2) some <em>val</em> is a string with whitespace.
   */
  public static DataFlowOutput parse(String filename) {

    final List<DFResultsOneSeq> results = new ArrayList<DFResultsOneSeq>();

    RecordProcessor p = new RecordProcessor() {
      @Override
      public void processRecord(List<String> record) {
        results.add(DFResultsOneSeq.parse(record));
      }
    };

    RecordListReader reader = new RecordListReader("DFRESULT", p);
    reader.parse(filename);

    return new DataFlowOutput(results);
  }

  /**
   * Output this DataFlowOutput object as a text file.
   * 
   * @param outFile
   *          The file where output is to be written. If outFile ends in ".gz"
   *          it will be output as a compressed file.
   */
  public void toParseableFile(String outFile) {
    if (outFile == null || outFile.length() == 0)
      throw new IllegalArgumentException("Illegal output file name: " + outFile);

    try {
      BufferedWriter out = UtilMDE.bufferedFileWriter(outFile);
      for (DFResultsOneSeq r : results) {
        out.append("START DFRESULT");
        out.newLine();
        out.append(r.toParseableString());
        out.append("END DFRESULT");
        out.newLine();
        out.newLine();
      }
      out.close();
    } catch (IOException e) {
      throw new Error(e);
    }
  }

  @Override
  public String toString() {
    StringBuilder b = new StringBuilder();
    for (DFResultsOneSeq r : results) {
      b.append(r.toString());
      b.append(Globals.lineSep);
      b.append(Globals.lineSep);
    }
    return b.toString();
  }

}
