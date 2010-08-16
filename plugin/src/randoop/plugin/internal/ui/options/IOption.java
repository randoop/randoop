package randoop.plugin.internal.ui.options;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

/**
 * A piece of a UI page capable of self-verification and saving to a launch
 * configuration. Because Options are generally not created until the
 * <code>createControl</code> method of a dialog is called, it may be necessary
 * to allow an empty constructor to be used as a placeholder so the defaults can
 * be obtained from
 * {@link Option#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)}
 * 
 * @author Peter Kalauskas
 */
public interface IOption {

  /**
   * Returns an OK status if this option can save to a launch configuration.
   * 
   * @return
   */
  public IStatus canSave();

  /**
   * Returns an OK status if the given launch configuration can be run without
   * throwing an expected error
   * 
   * @param config
   * @return
   */
  public IStatus isValid(ILaunchConfiguration config);

  /**
   * Initializes this options UI from the given launch configuration
   * 
   * @param config
   */
  public void initializeFrom(ILaunchConfiguration config);

  /**
   * Applies values from this options UI to the given launch configuration.
   * 
   * @param config
   */
  public void performApply(ILaunchConfigurationWorkingCopy config);

  /**
   * Sets this option's default attributes to the launch configuration without
   * changing the UI
   * 
   * @param config
   */
  public void setDefaults(ILaunchConfigurationWorkingCopy config);

  public void addChangeListener(IOptionChangeListener listener);

  public void removeChangeListener(IOptionChangeListener listener);

  /**
   * Restores defaults to the UI without affecting the launch configuration
   */
  public void restoreDefaults();

}
