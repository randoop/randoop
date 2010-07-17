package randoop.plugin.internal.ui.refactoring;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import randoop.plugin.internal.core.MethodMnemonic;
import randoop.plugin.internal.core.TypeMnemonic;
import randoop.plugin.internal.core.launching.RandoopArgumentCollector;

/**
 * The change for the main type project change of a launch configuration
 */
public class LaunchConfigurationProjectChange extends Change {

  private ILaunchConfiguration fLaunchConfiguration;
  private String fOldProjectName;
  private String fNewProjectName;
  
  private boolean fSelectedProjectChange;
  private boolean fTypeChange;
  private boolean fMethodChange;

  public LaunchConfigurationProjectChange(ILaunchConfiguration launchConfiguration, String newProjectName,
      boolean selectedProjectChange, boolean typeChange, boolean methodChange) throws CoreException {
    fLaunchConfiguration = launchConfiguration;
    fNewProjectName = newProjectName;
    fOldProjectName = RandoopArgumentCollector.getProjectName(fLaunchConfiguration);
    
    fSelectedProjectChange = selectedProjectChange;
    fTypeChange = typeChange;
    fMethodChange = methodChange;
  }

  @Override
  public Object getModifiedElement() {
    return fLaunchConfiguration;
  }

  @Override
  public String getName() {
    return MessageFormat.format("Update project of launch configuration \"{0}\"", new String[] { fLaunchConfiguration.getName() });
  }

  @Override
  public void initializeValidationData(IProgressMonitor pm) {
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.eclipse.ltk.core.refactoring.Change#isValid(org.eclipse.core.runtime
   * .IProgressMonitor)
   */
  @Override
  public RefactoringStatus isValid(IProgressMonitor pm) throws CoreException,
      OperationCanceledException {
    if (fLaunchConfiguration.exists()) {
      if (!fSelectedProjectChange) {
        return new RefactoringStatus();
      }
      
      String projectName = RandoopArgumentCollector.getProjectName(fLaunchConfiguration);
      if (fOldProjectName.equals(projectName)) {
        return new RefactoringStatus();
      }
      return RefactoringStatus.createWarningStatus(MessageFormat.format("The project for launch configuration \"{0}\" is no longer \"{1}\".", new String[] { fLaunchConfiguration.getName(), fOldProjectName }));
    }
    return RefactoringStatus.createFatalErrorStatus(MessageFormat.format("The launch configuration \"{0}\" no longer exists.", new String[] { fLaunchConfiguration.getName() }));
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.eclipse.ltk.core.refactoring.Change#perform(org.eclipse.core.runtime
   * .IProgressMonitor)
   */
  @Override
  public Change perform(IProgressMonitor pm) throws CoreException {
    final ILaunchConfigurationWorkingCopy wc = fLaunchConfiguration.getWorkingCopy();

    if (fSelectedProjectChange) {
      RandoopArgumentCollector.setProjectName(wc, fNewProjectName);
    }
    
    HashMap<String, String> newTypeMnemonicByOldTypeMnemonic = new HashMap<String, String>();
    List<String> availableTypeMnemonics = RandoopArgumentCollector.getAvailableTypes(wc);
    List<String> availableMethodMnemonics = RandoopArgumentCollector.getAvailableMethods(wc);
    List<String> selectedTypeMnemonics = RandoopArgumentCollector.getSelectedTypes(wc);
    List<String> selectedMethodMnemonics = RandoopArgumentCollector.getSelectedMethods(wc);
    
    if(fTypeChange) {
      createNewMnemonicsFromTypes(newTypeMnemonicByOldTypeMnemonic, availableTypeMnemonics);
    }

    if (fMethodChange) {
      createNewMnemonicsFromMethods(newTypeMnemonicByOldTypeMnemonic, availableMethodMnemonics);
    }

    if (fTypeChange) {
      RandoopRefactoringUtil.updateTypeMnemonics(newTypeMnemonicByOldTypeMnemonic, availableTypeMnemonics);
      RandoopRefactoringUtil.updateTypeMnemonics(newTypeMnemonicByOldTypeMnemonic, selectedTypeMnemonics);
      
      RandoopArgumentCollector.setAvailableTypes(wc, availableTypeMnemonics);
      RandoopArgumentCollector.setSelectedTypes(wc, selectedTypeMnemonics);
    }

    if (fMethodChange) {
      RandoopRefactoringUtil.updateMethodMnemonics(newTypeMnemonicByOldTypeMnemonic, availableMethodMnemonics);
      RandoopRefactoringUtil.updateMethodMnemonics(newTypeMnemonicByOldTypeMnemonic, selectedMethodMnemonics);
      
      RandoopArgumentCollector.setAvailableMethods(wc, availableMethodMnemonics);
      RandoopArgumentCollector.setSelectedMethods(wc, selectedMethodMnemonics);
    }
    
    if (wc.isDirty()) {
      fLaunchConfiguration = wc.doSave();
    }
    
    // create the undo change
    return new LaunchConfigurationProjectChange(fLaunchConfiguration, fOldProjectName, fSelectedProjectChange, fTypeChange, fMethodChange);
  }
  
  public void createNewMnemonicsFromTypes(HashMap<String, String> newTypeMnemonicByOldTypeMnemonic, List<String> typeMnemonics) {
    for (String oldMnemonic : typeMnemonics) {
      if (!newTypeMnemonicByOldTypeMnemonic.containsKey(oldMnemonic)) {
        TypeMnemonic oldTypeMnemonic = new TypeMnemonic(oldMnemonic);

        String projectName = oldTypeMnemonic.getJavaProjectName();
        if (projectName.equals(fOldProjectName)) {
          int classpathKind = oldTypeMnemonic.getClasspathKind();
          IPath classpath = oldTypeMnemonic.getClasspath();
          String fqname = oldTypeMnemonic.getFullyQualifiedName();

          TypeMnemonic newTypeMnemonic = new TypeMnemonic(fNewProjectName, classpathKind, classpath, fqname);
          String newMnemonic = newTypeMnemonic.toString();
          newTypeMnemonicByOldTypeMnemonic.put(oldMnemonic, newMnemonic);
        }
      }
    }
  }

  public void createNewMnemonicsFromMethods(HashMap<String, String> newTypeMnemonicByOldTypeMnemonic, List<String> methodMnemonics) {
    for (String mnemonic : methodMnemonics) {
      MethodMnemonic methodMnemonic = new MethodMnemonic(mnemonic);
      TypeMnemonic oldTypeMnemonic = methodMnemonic.getDeclaringTypeMnemonic();

      if (!newTypeMnemonicByOldTypeMnemonic.containsKey(oldTypeMnemonic.toString())) {
        String projectName = oldTypeMnemonic.getJavaProjectName();
        if (projectName.equals(fOldProjectName)) {
          int classpathKind = oldTypeMnemonic.getClasspathKind();
          IPath classpath = oldTypeMnemonic.getClasspath();
          String fqname = oldTypeMnemonic.getFullyQualifiedName();

          TypeMnemonic newTypeMnemonic = new TypeMnemonic(fNewProjectName, classpathKind, classpath, fqname);

          String newMnemonic = newTypeMnemonic.toString();
          newTypeMnemonicByOldTypeMnemonic.put(oldTypeMnemonic.toString(), newMnemonic);
        }
      }
    }
  }

}
