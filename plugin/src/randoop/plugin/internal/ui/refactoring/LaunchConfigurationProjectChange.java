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

  public LaunchConfigurationProjectChange(ILaunchConfiguration launchConfiguration, String newProjectName,
      boolean selectedProjectChange, boolean typeChange) throws CoreException {
    fLaunchConfiguration = launchConfiguration;
    fNewProjectName = newProjectName;
    fOldProjectName = RandoopArgumentCollector.getProjectName(fLaunchConfiguration);
    
    fSelectedProjectChange = selectedProjectChange;
    fTypeChange = typeChange;
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

  @Override
  public Change perform(IProgressMonitor pm) throws CoreException {
    final ILaunchConfigurationWorkingCopy wc = fLaunchConfiguration.getWorkingCopy();

    if (fSelectedProjectChange) {
      RandoopArgumentCollector.setProjectName(wc, fNewProjectName);
    }
    
    
    if(fTypeChange) {
      HashMap<String, String> newTypeMnemonicByOldTypeMnemonic = new HashMap<String, String>();
      List<String> availableTypeMnemonics = RandoopArgumentCollector.getAvailableTypes(wc);
      List<String> grayedTypeMnemonics = RandoopArgumentCollector.getGrayedTypes(wc);
      List<String> checkedTypeMnemonics = RandoopArgumentCollector.getCheckedTypes(wc);

      createNewMnemonicsFromTypes(newTypeMnemonicByOldTypeMnemonic, availableTypeMnemonics);

      RandoopRefactoringUtil.updateTypeMnemonics(newTypeMnemonicByOldTypeMnemonic, availableTypeMnemonics);
      RandoopRefactoringUtil.updateTypeMnemonics(newTypeMnemonicByOldTypeMnemonic, checkedTypeMnemonics);
      
      RandoopRefactoringUtil.updateMethodMnemonicKeys(wc, newTypeMnemonicByOldTypeMnemonic);
      
      RandoopArgumentCollector.setAvailableTypes(wc, availableTypeMnemonics);
      RandoopArgumentCollector.setGrayedTypes(wc, grayedTypeMnemonics);
      RandoopArgumentCollector.setCheckedTypes(wc, checkedTypeMnemonics);
    }
    
    if (wc.isDirty()) {
      fLaunchConfiguration = wc.doSave();
    }
    
    // create the undo change
    return new LaunchConfigurationProjectChange(fLaunchConfiguration, fOldProjectName, fSelectedProjectChange, fTypeChange);
  }
  
  protected void createNewMnemonicsFromTypes(HashMap<String, String> newTypeMnemonicByOldTypeMnemonic, List<String> typeMnemonics) {
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

}
