package randoop.experiments;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import plume.Pair;
import plume.UtilMDE;
import randoop.Globals;
import randoop.Sequence;
import randoop.util.RecordListReader;
import randoop.util.RecordProcessor;
import cov.Branch;

/**
 * A collection of frontier branches and sequences that reach them.
 * A DataFlowInput is the input to the DataFlow analysis. 
 */
public class DataFlowInput implements Serializable {

  private static final long serialVersionUID = -3010962783887743548L;

  // Maps a branch to a set of sequences that cover the branch.
  public Map<Branch, Set<Sequence>> frontierMap;

  public DataFlowInput(Map<Branch, Set<Sequence>> frontierMap) {
    this.frontierMap = frontierMap;
  }

  /**
   * Create a new DataFlowInput from a text file.
   * 
   * The text file consists of a list of records. Each record is as follows:
   * 
   * START RECORD
   * BRANCH
   * <branch-description>
   * SEQUENCE
   * <sequence-description>
   * END RECORD
   *
   * Where <branch-description> is a string that can be parsed
   * by the method cov.OneBranchInfo.parse(String), and <sequence-description>
   * is a string that can be parsed by the method
   * randoop.SequenceParser.parse(String).
   * 
   * Blank lines and comments between records are allowed and ignored.
   * Comments are lines starting with "#".
   * 
   * The records are read and used to populate the frontierMap field of the
   * new DataFlowInput object. If two records specify the same branch, the
   * two sequences are added to the same set.
   * 
   * @param inFile A text file containing a description of branches and sequences.
   *        If inFile ends in ".gz" it will be read as a compressed file.
   */
  public static DataFlowInput parse(String inFile) {
    
    final Map<Branch, Set<Sequence>> map =
      new LinkedHashMap<Branch, Set<Sequence>>();

    RecordProcessor processor = new RecordProcessor() {
      public void processRecord(List<String> lines) {
        Pair<Branch, Sequence> brAndSeq = parseRecord(lines);
        Set<Sequence> set = map.get(brAndSeq.a);
        if (set == null) {
          set = new LinkedHashSet<Sequence>();
          map.put(brAndSeq.a, set);
        }
        set.add(brAndSeq.b);
      }
    };
    
    RecordListReader reader = new RecordListReader("RECORD", processor);
    reader.parse(inFile);
    
    return new DataFlowInput(map);
  }
    
  
  private static Pair<Branch, Sequence> parseRecord(List<String> lines) {
    try {
      assert lines.get(0).equals("BRANCH");
      // Next line is the branch.
      Branch branch = Branch.parse(lines.get(1));
      assert lines.get(2).equals("SEQUENCE");
      // Line indices 3--end is the sequence.
      Sequence sequence = Sequence.parse(lines.subList(3, lines.size()));

      return new Pair<Branch, Sequence>(branch, sequence);
    } catch (Exception e) {
      throw new Error(e);
    }
  }

  /**
   * Output this DataFlowInput as a text file.
   * 
   * @param outFile The file where output is to be written.
   * If outFile ends in ".gz" it will be output as a compressed file.
   * 
   * @param outputCodeRep If true, also output the code representation of
   * each sequence,  as a comment following each record.
   */
  public void toParseableFile(String outFile, boolean outputCodeRep) {
    if (outFile == null || outFile.length() == 0)
      throw new IllegalArgumentException("Illegal output file name: " + outFile);

    try {
      BufferedWriter out = UtilMDE.bufferedFileWriter(outFile);
      for (Map.Entry<Branch, Set<Sequence>> e : frontierMap.entrySet()) {
        for (Sequence seq : e.getValue()) {
          // Print one record.
          out.append("START RECORD" + Globals.lineSep + Globals.lineSep);
          out.append("BRANCH" + Globals.lineSep);
          out.append(e.getKey().toString());
          out.append(Globals.lineSep + Globals.lineSep);
          out.append("SEQUENCE" + Globals.lineSep);
          out.append(seq.toParseableString());
          out.append(Globals.lineSep + Globals.lineSep);
          out.append("END RECORD" + Globals.lineSep + Globals.lineSep);

          if (outputCodeRep) {
            out.append("# Code representation:" + Globals.lineSep);
            for (String line : seq.toCodeString().split(Globals.lineSep)) {
              out.append("# " + line);
              out.append(Globals.lineSep);
            }
            out.append(Globals.lineSep + Globals.lineSep);
          }
        }
      }
      out.close();
    } catch (IOException e) {
      throw new Error(e);
    }
  }
}
