package randoop.plugin.internal.ui.options;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
    
    fFullyQualifiedTestName.addModifyListener(new ModifyListener() {
      
      public void modifyText(ModifyEvent e) {
        String fqname = fFullyQualifiedTestName.getText();
        String classname = RandoopCoreUtil.getClassName(fqname);
        String packagename = RandoopCoreUtil.getPackageName(fqname);
        
        notifyListeners(new OptionChangeEvent(IRandoopLaunchConfigurationConstants.ATTR_JUNIT_CLASS_NAME, classname));
        notifyListeners(new OptionChangeEvent(IRandoopLaunchConfigurationConstants.ATTR_JUNIT_PACKAGE_NAME, packagename));
      }
    });
    
    fPackageName = null;
    fClassName = null;
  }
  
  public JUnitTestClassNameOption(Text packageName, Text className) {
    fPackageName = packageName;
    fClassName = className;
    
    
    fPackageName.addModifyListener(new ModifyListener() {
      
      public void modifyText(ModifyEvent e) {
        notifyListeners(new OptionChangeEvent(IRandoopLaunchConfigurationConstants.ATTR_JUNIT_PACKAGE_NAME, null));
      }
    });
    
    fClassName.addModifyListener(new ModifyListener() {
      
      public void modifyText(ModifyEvent e) {
        notifyListeners(new OptionChangeEvent(IRandoopLaunchConfigurationConstants.ATTR_JUNIT_CLASS_NAME, null));
      }
    });
    
    fFullyQualifiedTestName = null;
  }

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
          IConstants.DEFAULT_COMPLIANCE_LEVEL, IConstants.DEFAULT_SOURCE_LEVEL);
      if (packageStatus.getSeverity() == IStatus.ERROR) {
        return packageStatus;
      }
    }

    IStatus classStatus = JavaConventions.validateIdentifier(className,
        IConstants.DEFAULT_COMPLIANCE_LEVEL, IConstants.DEFAULT_SOURCE_LEVEL);
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

  public void initializeFrom(ILaunchConfiguration config) {
    setDisableListeners(true);
    
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
    
    setDisableListeners(false);
  }

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

  public void setDefaults(ILaunchConfigurationWorkingCopy config) {
    RandoopArgumentCollector.setJUnitPackageName(config, IRandoopLaunchConfigurationConstants.DEFAULT_JUNIT_PACKAGE_NAME);
    RandoopArgumentCollector.setJUnitClassName(config, IRandoopLaunchConfigurationConstants.DEFAULT_JUNIT_CLASS_NAME);
  }
  
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
