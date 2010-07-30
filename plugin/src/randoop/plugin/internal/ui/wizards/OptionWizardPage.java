package randoop.plugin.internal.ui.wizards;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;

import randoop.plugin.internal.IConstants;
import randoop.plugin.internal.ui.options.IOption;

public abstract class OptionWizardPage extends WizardPage {
  private List<IOption> fOptions;
  
  private ILaunchConfigurationWorkingCopy fConfig;

  private ModifyListener fBasicModifyListener = new ModifyListener() {
    @Override
    public void modifyText(ModifyEvent e) {
      update();
    }
  };

  private SelectionListener fBasicSelectionListener = new SelectionListener() {
    @Override
    public void widgetSelected(SelectionEvent e) {
      setErrorMessage(null);
      update();
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
      setErrorMessage(null);
      update();
    }
  };
  
  protected OptionWizardPage(String pageName, ILaunchConfigurationWorkingCopy config) {
    super(pageName);
    fOptions = new ArrayList<IOption>();
    fConfig = config;
  }
  
  protected void update() {
    performApply(fConfig);
    if (isCurrentPage()) {
      getContainer().updateButtons();
    }
  }
  
  @Override
  public boolean isPageComplete() {
    return isValid(fConfig);
  }
  
  public void restoreDefualts() {
    for (IOption option : fOptions) {
      option.restoreDefaults();
    }
  }
  
  // public void setDefaults(ILaunchConfigurationWorkingCopy config) {
  // for (IOption option : fOptions) {
  // option.setDefaults(config);
  // }
  // }
  
  // public void initializeFrom(ILaunchConfiguration config) {
  // for (IOption option : fOptions) {
  // option.initializeFrom(config);
  // }
  // }
  
  @Override
  public void createControl(Composite parent) {
    update();
  }
  
  protected OptionWizardPage(String pageName, String title, ImageDescriptor titleImage, ILaunchConfigurationWorkingCopy config) {
    super(pageName, title, titleImage);
    fOptions = new ArrayList<IOption>();
    fConfig = config;
  }

  protected SelectionListener getSelectionListener() {
    return fBasicSelectionListener;
  }
  
  protected ModifyListener getModifyListener() {
    return fBasicModifyListener;
  }

  /**
   * Adds the specified option to this page if it is not already present.
   * 
   * @param option
   *          option to be added to this tab
   * @return <code>true</code> if this page did not already contain the
   *         specified option
   */
  protected boolean addOption(IOption option) {
    return fOptions.add(option);
  }
  
  public boolean isValid(ILaunchConfiguration config) {
    setErrorMessage(null);
    setMessage(null);
    
    boolean isMessageSet = false;
    
    for (IOption option : fOptions) {
      IStatus status = option.isValid(config);

      String message = status.getMessage();
      if (status.getSeverity() == IStatus.ERROR) {
        setErrorMessage(message);
        return false;
      } else {
        if (!isMessageSet) {
          isMessageSet = setReadableMessage(message);
        }
      }
    }
    
    return true;
  }
  
  public void performApply(ILaunchConfigurationWorkingCopy config) {
    for (IOption option : fOptions) {
      option.performApply(config);
    }
  }
  
  /**
   * Sets the message so long as it is non-<code>null</code> and non-empty.
   * 
   * @param message
   *          <code>String</code> to pass to <code>setMessage</code> if it is
   *          valid
   * @return <code>true</code> if the message is set
   */
  protected boolean setReadableMessage(String message) {
    String msg = message;
    if (msg != null && !msg.isEmpty()) {
      setMessage(message);
      return true;
    }
    
    return false;
  }
}
