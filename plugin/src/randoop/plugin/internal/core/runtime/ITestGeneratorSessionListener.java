package randoop.plugin.internal.core.runtime;

import org.eclipse.jdt.core.ICompilationUnit;

import randoop.runtime.ErrorRevealed;

public interface ITestGeneratorSessionListener {
  
  public void sessionStarted();
  
  public void sessionEnded();
  
  public void sessionTerminated();
  
  public void errorRevealed(ErrorRevealed error);
  
  public void madeProgress(double percentDone);
  
  public void madeSequences(int count);
  
  public void madeJUnitDriver(ICompilationUnit driverFile);

}
