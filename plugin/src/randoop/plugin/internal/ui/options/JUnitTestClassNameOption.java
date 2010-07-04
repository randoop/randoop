package randoop.plugin.internal.ui.options;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import randoop.plugin.internal.IConstants;
import randoop.plugin.internal.core.StatusFactory;
import randoop.plugin.internal.core.launching.RandoopArgumentCollector;

public class JUnitTestClassNameOption extends Option {
  private Text fFullyQualifiedTestName;

  public JUnitTestClassNameOption(Text fullyQualifiedTestName) {
    fFullyQualifiedTestName = fullyQualifiedTestName;
  }

  @Override
  public IStatus canSave() {
    if (fFullyQualifiedTestName == null) {
      return StatusFactory
          .createErrorStatus(JUnitTestClassNameOption.class.getName() + " incorrectly initialized"); //$NON-NLS-1$
    }

    String junitFullyQualifiedClassName = fFullyQualifiedTestName.getText();

    return validate(junitFullyQualifiedClassName);
  }

  @Override
  public IStatus isValid(ILaunchConfiguration config) {
    String junitFullyQualifiedName = RandoopArgumentCollector.getJUnitFullyQualifiedTypeName(config);
  
    return validate(junitFullyQualifiedName);
  }
  
  protected IStatus validate(String junitFullyQualifiedClassName) {
    IStatus status = JavaConventions.validateJavaTypeName(junitFullyQualifiedClassName,
        IConstants.EMPTY_STRING, IConstants.EMPTY_STRING);
    if(status.isOK()) {
      return StatusFactory.createOkStatus();
    }
    
    return status;
  }

  @Override
  public void initializeFrom(ILaunchConfiguration config) {
    if (fFullyQualifiedTestName != null)
      fFullyQualifiedTestName.setText(RandoopArgumentCollector
          .getJUnitFullyQualifiedTypeName(config));
  }

  @Override
  public void performApply(ILaunchConfigurationWorkingCopy config) {
    if (fFullyQualifiedTestName != null)
      RandoopArgumentCollector.setJUnitFullyQualifiedTypeName(config,
          fFullyQualifiedTestName.getText());
  }

  @Override
  public void setDefaults(ILaunchConfigurationWorkingCopy config) {
    RandoopArgumentCollector.restoreJUnitPackageName(config);
    RandoopArgumentCollector.restoreJUnitClassName(config);
  }

}
