package randoop.plugin.internal.ui.options;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.swt.widgets.Text;

import randoop.plugin.internal.IConstants;
import randoop.plugin.internal.core.RandoopCoreUtil;
import randoop.plugin.internal.core.StatusFactory;
import randoop.plugin.internal.core.launching.IRandoopLaunchConfigurationConstants;
import randoop.plugin.internal.core.launching.RandoopArgumentCollector;

public class JUnitTestClassNameOption extends Option {
  private Text fFullyQualifiedTestName;
  private Text fPackageName;
  private Text fClassName;

  public JUnitTestClassNameOption(Text fullyQualifiedTestName) {
    fFullyQualifiedTestName = fullyQualifiedTestName;
    
    fPackageName = null;
    fClassName = null;
  }
  
  public JUnitTestClassNameOption(Text packageName, Text className) {
    fPackageName = packageName;
    fClassName = className;
    
    fFullyQualifiedTestName = null;
  }

  @Override
  public IStatus canSave() {
    if (fFullyQualifiedTestName != null && fPackageName == null && fClassName == null) {
      // fFullyQualifiedTestName can be null if fPackageName and fClassName are not null
      String fqname = fFullyQualifiedTestName.getText();
      String packageName = RandoopCoreUtil.getPackageName(fqname);
      String className = RandoopCoreUtil.getClassName(fqname);
      
      return validate(packageName, className);
    } else if (fFullyQualifiedTestName == null && fPackageName != null && fClassName != null) {
      // fPackageName and fClassName can be null if fFullyQualifiedTestName is not null
      
      String packageName = fPackageName.getText();
      String className = fClassName.getText();
      return validate(packageName, className);
    } else {
      return StatusFactory.createErrorStatus(JUnitTestClassNameOption.class
          .getName() + " incorrectly initialized"); //$NON-NLS-1$
    }
  }

  @Override
  public IStatus isValid(ILaunchConfiguration config) {
    String packageName = RandoopArgumentCollector.getJUnitPackageName(config);
    String className = RandoopArgumentCollector.getJUnitClassName(config);
    
    return validate(packageName, className);
  }
  
  protected IStatus validate(String packageName, String className) {
    if (packageName.contains("$") || className.contains("$")) {  //$NON-NLS-1$//$NON-NLS-2$
      return StatusFactory.createErrorStatus("JUnit class name cannot use secondary types");
    }
    IStatus packageStatus = StatusFactory.OK_STATUS;
    if (!packageName.isEmpty()) {
      packageStatus = JavaConventions.validatePackageName(packageName,
          IConstants.EMPTY_STRING, IConstants.EMPTY_STRING);
      if(packageStatus.getSeverity() == IStatus.ERROR) {
        return packageStatus;
      }
    }
    
    IStatus classStatus = JavaConventions.validateIdentifier(className,
        IConstants.EMPTY_STRING, IConstants.EMPTY_STRING);
    if(classStatus.getSeverity() == IStatus.ERROR) {
      return classStatus;
    }
    
    if (packageStatus.isOK() && classStatus.isOK()) {
      return StatusFactory.OK_STATUS;
    } else if (packageStatus.isOK()) {
      return classStatus;
    } else {
      return packageStatus;
    }
  }

  @Override
  public void initializeFrom(ILaunchConfiguration config) {
    if (fFullyQualifiedTestName != null) {
      String packageName = RandoopArgumentCollector.getJUnitPackageName(config);
      String className = RandoopArgumentCollector.getJUnitClassName(config);

      if (packageName.isEmpty()) {
        fFullyQualifiedTestName.setText(className);
      } else {
        fFullyQualifiedTestName.setText(packageName + '.' + className);
      }
    } else if (fPackageName != null && fClassName != null) {
      fPackageName.setText(RandoopArgumentCollector.getJUnitPackageName(config));
      fClassName.setText(RandoopArgumentCollector.getJUnitClassName(config));
    }
  }

  @Override
  public void performApply(ILaunchConfigurationWorkingCopy config) {
    if (fFullyQualifiedTestName != null) {
      String fqname = fFullyQualifiedTestName.getText();
      String packageName = RandoopCoreUtil.getPackageName(fqname);
      String className = RandoopCoreUtil.getClassName(fqname);
      
      RandoopArgumentCollector.setJUnitPackageName(config, packageName);
      RandoopArgumentCollector.setJUnitClassName(config, className);
    } else if (fPackageName != null && fClassName != null) {
      RandoopArgumentCollector.setJUnitPackageName(config, fPackageName.getText());
      RandoopArgumentCollector.setJUnitClassName(config, fClassName.getText());
    }
  }

  @Override
  public void setDefaults(ILaunchConfigurationWorkingCopy config) {
    RandoopArgumentCollector.restoreJUnitPackageName(config);
    RandoopArgumentCollector.restoreJUnitClassName(config);
  }

  @Override
  public void restoreDefaults() {
    String packageName = IRandoopLaunchConfigurationConstants.DEFAULT_JUNIT_PACKAGE_NAME;
    String className = IRandoopLaunchConfigurationConstants.DEFAULT_JUNIT_CLASS_NAME;
    if (fFullyQualifiedTestName != null && fPackageName == null && fClassName == null) {
      fFullyQualifiedTestName.setText(RandoopCoreUtil.getFullyQualifiedName(packageName, className));
    } else if (fFullyQualifiedTestName == null && fPackageName != null && fClassName != null) {
      fPackageName.setText(packageName);
      fClassName.setText(className);
    }
  }
}
