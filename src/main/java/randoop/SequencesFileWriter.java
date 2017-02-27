package randoop;

import randoop.main.GenAllTests;
import randoop.main.GenInputsAbstract;
import randoop.sequence.ExecutableSequence;
import randoop.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

/**
 * SequencesFileWriter is a class that for a collection of sequences, outputs text files containing one
 * line corresponding to the sequence's methods. Note that if a sequence file already exists, new sequences
 * will only be appeded to it.
 */
public class SequencesFileWriter {

  // The directory where the sequence files should be written to.
  private final String dirName;

  public SequencesFileWriter(String outputDirectory) {
    this.dirName = outputDirectory;
  }

  private static FileOutputStream createTextOutputStream(File file, boolean append) {
    try {
      return new FileOutputStream(file, append);
    } catch (IOException e) {
      Log.out.println("Exception thrown while creating text print stream:" + file.getName());
      e.printStackTrace();
      System.exit(1);
      throw new Error("This can't happen");
    }
  }
  /*
   *
   */

  public void writeSequences(List<ExecutableSequence> sequences) {
    String mainCUT = GenAllTests.testclass.get(0);
    if (mainCUT.indexOf(".") >= 0) {
      mainCUT = mainCUT.substring(mainCUT.lastIndexOf(".") + 1);
    }
    File allSequencesFile =
        new File(
            getDir(), mainCUT + "_" + "sequences_max_size_" + GenInputsAbstract.maxsize + ".txt");
    PrintStream psSeq = null;
    FileOutputStream outSequences;
    outSequences = createTextOutputStream(allSequencesFile, allSequencesFile.exists());
    psSeq = new PrintStream(outSequences);
    for (ExecutableSequence s : sequences) {
      String cutSequence = getSequenceOfCallsOnCut(s, false);
      psSeq.println(cutSequence);
    }

    if (psSeq != null) {
      psSeq.close();
    }
  }

  private String getSequenceOfCallsOnCut(ExecutableSequence s, boolean fullOperationName) {
    int cutConstructionIndex;
    String tracedVariable = "";

    for (cutConstructionIndex = 0;
        cutConstructionIndex < s.sequence.size();
        cutConstructionIndex++) {
      if (s.sequence.getStatement(cutConstructionIndex).isConstructorCall()) {
        String cs = s.statementToCodeString(cutConstructionIndex);
        // Find the variable name between blank space and equals sign, given the pattern type var = <value>';
        tracedVariable = cs.substring(cs.indexOf(" ") + 1, cs.indexOf("=") - 1).trim();
        break;
      }
    }

    String cutSequence =
        s.sequence.getStatement(cutConstructionIndex).getOperation().toParsableString() + ";";

    for (int i = cutConstructionIndex; i < s.sequence.size(); i++) {
      if (s.statementToCodeString(i).indexOf(tracedVariable + ".") >= 0) {
        if (fullOperationName) {
          cutSequence += s.sequence.getStatement(i).getOperation().toParsableString() + ";";
        } else {
          cutSequence += s.sequence.getStatement(i).getOperation().getName() + ";";
        }
      }
    }
    if (cutSequence.length() > 1 && cutSequence.lastIndexOf(";") >= 0) {
      cutSequence = cutSequence.substring(0, cutSequence.lastIndexOf(";"));
    }
    return cutSequence;
  }

  private File getDir() {
    File dir;
    if (dirName == null || dirName.length() == 0) {
      dir = new File(System.getProperty("user.dir"));
    } else {
      dir = new File(dirName);
    }

    return dir;
  }
}
