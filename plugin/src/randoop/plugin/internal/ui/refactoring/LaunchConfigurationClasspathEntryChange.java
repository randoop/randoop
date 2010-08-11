package randoop.plugin.internal.ui.refactoring;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import randoop.plugin.internal.core.TypeMnemonic;
import randoop.plugin.internal.core.launching.RandoopArgumentCollector;

public class LaunchConfigurationClasspathEntryChange extends Change  {

  private ILaunchConfiguration fLaunchConfiguration;
  
  private final IPath fOldPath;
  private final IPath fNewPath;

  public LaunchConfigurationClasspathEntryChange(ILaunchConfiguration launchConfiguration, IPath oldPath, IPath newPath)
      throws CoreException {
    Assert.isLegal(launchConfiguration != null, "Launch configurtion cannot be null"); //$NON-NLS-1$
    Assert.isLegal(oldPath != null, "IPath cannot be null"); //$NON-NLS-1$
    Assert.isLegal(newPath != null, "IPath cannot be null"); //$NON-NLS-1$

    fLaunchConfiguration = launchConfiguration;
    
    fOldPath = oldPath;
    fNewPath = newPath;
  }

  @Override
  public Object getModifiedElement() {
    return fLaunchConfiguration;
  }

  @Override
  public String getName() {
    return MessageFormat.format("Update classpath entries of launch configuration \"{0}\"", fLaunchConfiguration.getName());
  }

  @Override
  public void initializeValidationData(IProgressMonitor pm) {
  }

  @Override
  public RefactoringStatus isValid(IProgressMonitor pm) throws CoreException,
      OperationCanceledException {
    if (fLaunchConfiguration.exists()) {
      return new RefactoringStatus();
    }
    return RefactoringStatus.createFatalErrorStatus(MessageFormat.format("The launch configuration \"{0}\" no longer exists.", fLaunchConfiguration.getName()));
  }

  @Override
  public Change perform(IProgressMonitor pm) throws CoreException {
    final ILaunchConfigurationWorkingCopy wc = fLaunchConfiguration.getWorkingCopy();

    HashMap<String, String> newTypeMnemonicByOldTypeMnemonic = new HashMap<String, String>();
    
    List<String> availableTypeMnemonics = RandoopArgumentCollector.getAvailableTypes(wc);
    List<String> grayedTypeMnemonics = RandoopArgumentCollector.getGrayedTypes(wc);
    List<String> checkedTypeMnemonics = RandoopArgumentCollector.getCheckedTypes(wc);
    
    List<String> typeMnemonics = new ArrayList<String>();
    typeMnemonics.addAll(availableTypeMnemonics);
    typeMnemonics.addAll(grayedTypeMnemonics);
    typeMnemonics.addAll(checkedTypeMnemonics);
    createNewMnemonicsFromTypes(newTypeMnemonicByOldTypeMnemonic, typeMnemonics);
    
    RandoopRefactoringUtil.updateTypeMnemonics(newTypeMnemonicByOldTypeMnemonic, availableTypeMnemonics);
    RandoopRefactoringUtil.updateTypeMnemonics(newTypeMnemonicByOldTypeMnemonic, grayedTypeMnemonics);
    RandoopRefactoringUtil.updateTypeMnemonics(newTypeMnemonicByOldTypeMnemonic, checkedTypeMnemonics);
    
    RandoopArgumentCollector.setAvailableTypes(wc, availableTypeMnemonics);
    RandoopArgumentCollector.setGrayedTypes(wc, grayedTypeMnemonics);
    RandoopArgumentCollector.setCheckedTypes(wc, checkedTypeMnemonics);
    
    if (wc.isDirty()) {
      fLaunchConfiguration = wc.doSave();
    }
    
    // create the undo change
    return new LaunchConfigurationClasspathEntryChange(fLaunchConfiguration, fNewPath, fOldPath);
  }

  public void createNewMnemonicsFromTypes(HashMap<String, String> newTypeMnemonicByOldTypeMnemonic, List<String> typeMnemonics) {
    for (String oldMnemonic : typeMnemonics) {
      if (!newTypeMnemonicByOldTypeMnemonic.containsKey(oldMnemonic)) {
        String newMnemonic = getNewMnemonicFromType(oldMnemonic);
        if (newMnemonic != null) {
          newTypeMnemonicByOldTypeMnemonic.put(oldMnemonic, newMnemonic);
        }
      }
    }
  }

  /**
   * 
   * @param typeMnemonicString
   * @return the new mnemonic, or <code>null</code> if no change was made
   */
  public String getNewMnemonicFromType(String typeMnemonicString) {
    TypeMnemonic oldTypeMnemonic = new TypeMnemonic(typeMnemonicString);

    int entryKind = oldTypeMnemonic.getClasspathKind();
    if (entryKind == IClasspathEntry.CPE_SOURCE || entryKind == IClasspathEntry.CPE_LIBRARY || entryKind == IClasspathEntry.CPE_PROJECT) {
      IPath oldPath = oldTypeMnemonic.getClasspath();
      
      if (oldPath.uptoSegment(fOldPath.segmentCount()).equals(fOldPath)) {
        IPath newPath = fNewPath.append(oldPath.removeFirstSegments(fOldPath.segmentCount()));
        String projectName = oldTypeMnemonic.getJavaProjectName();
        int classpathKind = oldTypeMnemonic.getClasspathKind();
        String fqname = oldTypeMnemonic.getFullyQualifiedName();

        TypeMnemonic newTypeMnemonic = new TypeMnemonic(projectName, classpathKind, newPath, fqname);
        return newTypeMnemonic.toString();
      }
    }

    return null;
  }

}
