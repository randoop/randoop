package randoop.runtime;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import randoop.FailureSet;
import randoop.IEventListener;
import randoop.ITestFilter;
import randoop.JunitFileWriter;
import randoop.NoExceptionCheck;
import randoop.main.GenInputsAbstract;
import randoop.sequence.AbstractGenerator;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Statement;

import plume.Pair;

public class PluginBridge implements ITestFilter, IEventListener {

  long timeOfLastUpdate = 0;
  public MessageSender msgSender;
  private AbstractGenerator generator;
  //public List<File> additionalJunitFiles;
  public List<String> additionalJunitClasses;
  
  public Set<Pair<Statement,Class<?>>> errors = new LinkedHashSet<>();
  
  public PluginBridge(AbstractGenerator generator, MessageSender sender) {
    if (generator == null) {
      throw new IllegalArgumentException("generator is null");
    }
    if (sender == null) {
      throw new IllegalArgumentException("sender is null");
    }
    this.generator = generator;  
    this.msgSender = sender;
    //this.additionalJunitFiles = new LinkedList<File>();
    this.additionalJunitClasses = new LinkedList<>();
  }

  @Override
  public boolean outputSequence(ExecutableSequence s, FailureSet f) {
    
    if (f.getFailures().isEmpty()) {
      return true;
    }

    for (FailureSet.Failure failure : f.getFailures()) {
      if (errors.add(new Pair<Statement, Class<?>>(failure.statement, failure.violationClass))) {
        String description = failure.violationClass.toString();
        if (failure.violationClass.equals(NoExceptionCheck.class)) {
          description = "NPEs / Assertion violations";
        }
        List<String> failureClassList = new LinkedList<String>();
        failureClassList.add(failure.statement.toString());

        List<ExecutableSequence> singleSequenceList = new LinkedList<ExecutableSequence>();
        singleSequenceList.add(s);
        File junitFile = JunitFileWriter.writeJUnitTestFile(GenInputsAbstract.junit_output_dir, "randoopFailures", s, GenInputsAbstract.junit_classname
            + "_failure_" + errors.size());

        //additionalJunitFiles.add(junitFile);
        additionalJunitClasses.add("randoopFailures." + GenInputsAbstract.junit_classname + "_failure_" + errors.size());
        ErrorRevealed msg = new ErrorRevealed(s.toCodeString(), description, failureClassList, junitFile);
        msgSender.send(msg);
      }
    }
    return false;
  }

  @Override
  public void generationStepPost(ExecutableSequence es) {
    // Nothing to do here.
  }

  @Override
  public void generationStepPre() {
    
    long timeSoFar = generator.timer.getTimeElapsedMillis();
    if (timeSoFar - timeOfLastUpdate > 250) {
      double percentTimeDone = timeSoFar / (double) generator.maxTimeMillis;
      double percentSequencesDone = generator.numSequences() / (double) generator.maxSequences;

      /*
       * Randoop has more than one stopping criteria. To determine how close
       * we are to finishing, we calculate how close we are wrt the time
       * limit, and wrt the input (sequence) limit. We report whichever is
       * greater, but never report more than 100% (which can happen if
       * Randoop went just a bit over before stopping).
       */
      double percentDone = Math.min(Math.max(percentTimeDone, percentSequencesDone), 1.0);

      // Convert to percentage, between 0-100.
      IMessage msg = new PercentDone(percentDone, generator.numSequences(), 0);
      msgSender.send(msg);
      timeOfLastUpdate = timeSoFar;
    }
    
  }

  @Override
  public void progressThreadUpdate() {
    // Nothing to do here.
  }

  @Override
  public boolean stopGeneration() {
    return false;
  }

  @Override
  public void explorationStart() {

      IMessage msg = new RandoopStarted();
      msgSender.send(msg);
      timeOfLastUpdate = generator.timer.getTimeElapsedMillis();
  }

  @Override
  public void explorationEnd() {
      IMessage msg = new PercentDone(1.0, generator.numSequences(), 0);
      msgSender.send(msg);
      msg = new RandoopFinished();
      msgSender.send(msg);
  }

}
