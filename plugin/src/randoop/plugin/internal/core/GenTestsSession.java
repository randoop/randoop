package randoop.plugin.internal.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.debug.core.ILaunch;

import randoop.plugin.internal.core.launching.RandoopArgumentCollector;
import randoop.runtime.ErrorRevealed;

public class GenTestsSession {

  private static GenTestsSession activeSession = null;

  private ILaunch fLaunch;

  private RandoopArgumentCollector fArguments;

  private Set<ISessionChangeListener> fListeners;

  private List<ErrorRevealed> fRevealedErrors;

  private double fPercentDone;
  
  private int fSequenceCount;
  
  public GenTestsSession(ILaunch launch, RandoopArgumentCollector args) {
    fLaunch = launch;
    fArguments = args;
    fListeners = new HashSet<ISessionChangeListener>();
    fRevealedErrors = new ArrayList<ErrorRevealed>();
    
    fPercentDone = 0;
    fSequenceCount = 0;
  }

  public ILaunch getLaunch() {
    return fLaunch;
  }
  
  public RandoopArgumentCollector getArguments() {
    return fArguments;
  }
  
  public boolean hasError() {
    return fRevealedErrors.size() > 0;
  }

  public int getErrorCount() {
    return fRevealedErrors.size();
  }
  
  public int getSequenceCount() {
    return fSequenceCount;
  }
  
  public double getPercentDone() {
    return fPercentDone;
  }

  public void setSequenceCount(int sequenceCount) {
    fSequenceCount = sequenceCount;
    notifyListeners();
  }

  public void setPercentDone(double percentDone) {
    if (percentDone > 1) {
      fPercentDone = 1;
    } else if (percentDone < 0) {
      fPercentDone = 0;
    }

    fPercentDone = percentDone;
    notifyListeners();
  }
  
  public void addRevealedError(ErrorRevealed revealedError) {
    fRevealedErrors.add(revealedError);
    
    notifyListeners();
  }
  
  private void notifyListeners() {
    for (ISessionChangeListener listener : fListeners) {
      // do something
    }
  }
  
  public boolean removeChangeListener(ISessionChangeListener listener) {
    return fListeners.remove(listener);
  }

  public boolean addChangeListener(ISessionChangeListener listener) {
    return fListeners.add(listener);
  }

  public static GenTestsSession getActiveSession() {
    return activeSession;
  }

  public static void setActiveSession(GenTestsSession session) {
    activeSession = session;
  }

}
