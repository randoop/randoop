package randoop.plugin.internal.core.runtime;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.jdt.core.ICompilationUnit;

import randoop.plugin.internal.core.launching.RandoopArgumentCollector;
import randoop.plugin.model.resultstree.RunResultsTree;
import randoop.runtime.ErrorRevealed;

public class TestGeneratorSession {

  private static TestGeneratorSession activeSession = null;

  private ILaunch fLaunch;
  
  private RandoopArgumentCollector fArguments;

  private ICompilationUnit fJUnitDriver;

  private RunResultsTree fRandoopErrors;

  private int fErrorCount;

  private double fPercentDone;
  
  private int fSequenceCount;

  private boolean fIsStarted;
  
  private boolean fIsStopped;

  private boolean fIsTerminated;
  
  private ListenerList/*<ISessionChangeListener>*/ fListeners;

  public TestGeneratorSession(ILaunch launch, RandoopArgumentCollector args) {
    fLaunch = launch;
    fArguments = args;
    
    fRandoopErrors = new RunResultsTree();
    fErrorCount = 0;
    
    fPercentDone = 0;
    fSequenceCount = 0;

    fListeners = new ListenerList/*<ISessionChangeListener>*/();
    
    fIsStarted = false;
    fIsStopped = false;
    fIsTerminated = false;
  }
  
  public boolean isStarted() {
    return fIsStarted;
  }
  
  public boolean isRunning() {
    return fIsStarted && !isStopped();
  }
  
  public void start() {
    Assert.isLegal(!fIsStarted, "Session has already been started");
    
    fIsStarted = true;
    for (Object o : fListeners.getListeners()) {
      ((ITestGeneratorSessionListener) o).sessionStarted();
    }
  }
  
  public ILaunch getLaunch() {
    return fLaunch;
  }
  
  public RandoopArgumentCollector getArguments() {
    return fArguments;
  }

  public ICompilationUnit getJUnitDriver() {
    return fJUnitDriver;
  }
  
  public void setJUnitDriver(ICompilationUnit junitDriver) {
    Assert.isLegal(isStarted(), "Session has not been started");
    
    fJUnitDriver = junitDriver;
    
    for (Object o : fListeners.getListeners()) {
      ((ITestGeneratorSessionListener) o).madeJUnitDriver(fJUnitDriver);
    }
  }
  
  public boolean hasError() {
    return fErrorCount > 0;
  }

  public int getErrorCount() {
    return fErrorCount;
  }
  
  public RunResultsTree getRandoopErrors() {
    return fRandoopErrors;
  }
  
  public void addRevealedError(ErrorRevealed revealedError) {
    Assert.isLegal(isStarted(), "Session has not been started");
    
    fErrorCount++;
    fRandoopErrors.add(revealedError);
    
    for (Object o : fListeners.getListeners()) {
      ((ITestGeneratorSessionListener) o).errorRevealed(revealedError);
    }
  }
  
  public int getSequenceCount() {
    return fSequenceCount;
  }
  
  public void setSequenceCount(int sequenceCount) {
    Assert.isLegal(isStarted(), "Session has not been started");
    
    fSequenceCount = sequenceCount;
    
    for (Object o : fListeners.getListeners()) {
      ((ITestGeneratorSessionListener) o).madeSequences(fSequenceCount);
    }
  }
  
  public double getPercentDone() {
    return fPercentDone;
  }
  
  public void setPercentDone(double percentDone) {
    Assert.isLegal(isStarted(), "Session has not been started");
    
    if (percentDone > 1) {
      fPercentDone = 1;
    } else if (percentDone < 0) {
      fPercentDone = 0;
    } else {
      fPercentDone = percentDone;
    }
    
    for (Object o : fListeners.getListeners()) {
      ((ITestGeneratorSessionListener) o).madeProgress(fPercentDone);
    }
  }

  public boolean isStopped() {
    return fIsStopped;
  }
  
  public boolean isTerminated() {
    return fIsTerminated;
  }
  
  public void stop(boolean force) {
    fIsStopped = true;
    fIsTerminated = force;
    
    if (force) {
      for (Object o : fListeners.getListeners()) {
        ((ITestGeneratorSessionListener) o).sessionTerminated();
      }
    } else {
      for (Object o : fListeners.getListeners()) {
        ((ITestGeneratorSessionListener) o).sessionEnded();
      }
    }
  }
  
  public void removeListener(ITestGeneratorSessionListener listener) {
    fListeners.remove(listener);
  }

  public void addListener(ITestGeneratorSessionListener listener) {
    fListeners.add(listener);
  }

  // TODO: Use ViewHistory instead, and remove these static methods
  public static TestGeneratorSession getActiveSession() {
    return activeSession;
  }

  public static void setActiveSession(TestGeneratorSession session) {
    activeSession = session;
  }

}
