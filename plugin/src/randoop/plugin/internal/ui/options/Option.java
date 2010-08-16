package randoop.plugin.internal.ui.options;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * Abstract implementation of <code>IOption</code> that implements listener
 * functionality.
 * 
 * @author Peter Kalauskas
 */
public abstract class Option implements IOption {
  
  private boolean fListeneredDisabled = false;

  List<IOptionChangeListener> fListeners = new ArrayList<IOptionChangeListener>();

  /**
   * Adds the given change listener to this option's list of listeners
   */
  public void addChangeListener(IOptionChangeListener listener) {
    fListeners.add(listener);
  }

  /**
   * Removes the given change listener from this option's list of listeners
   */
  public void removeChangeListener(IOptionChangeListener listener) {
    fListeners.remove(listener);
  }

  /**
   * Notifies all of this option change-listeners of the given event
   * 
   * @param event
   *          event object to pass to listeners
   */
  protected void notifyListeners(IOptionChangeEvent event) {
    if (!fListeneredDisabled) {
      for (IOptionChangeListener listener : fListeners) {
        listener.attributeChanged(event);
      }
    }
  }

  /**
   * Disables the listeners and calls
   * <code>initializeWithoutListenersFrom</code>. Listeners are disabled because
   * it is common for listeners to be used for saving launch configurations. If
   * a the configuration is saved during initialization, it is possible for
   * attributes necessary for other options to be erased.
   */
  public final void initializeFrom(ILaunchConfiguration config) {
    fListeneredDisabled = true;
    
    initializeWithoutListenersFrom(config);
    
    fListeneredDisabled = false;
  }

  /**
   * A replacement for <code>initializeFrom</code> that is called after
   * listeners have been disabled.
   * 
   * @param config
   */
  public abstract void initializeWithoutListenersFrom(ILaunchConfiguration config);
  
}
