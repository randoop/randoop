package randoop.plugin.internal.ui.options;

import java.util.ArrayList;
import java.util.List;

public abstract class Option implements IOption {
  
  private boolean fListeneredDisabled = false;

  List<IOptionChangeListener> fListeners = new ArrayList<IOptionChangeListener>();

  public void addChangeListener(IOptionChangeListener listener) {
    fListeners.add(listener);
  }

  public void removeChangeListener(IOptionChangeListener listener) {
    fListeners.remove(listener);
  }

  protected void notifyListeners(IOptionChangeEvent event) {
    if (!fListeneredDisabled) {
      for (IOptionChangeListener listener : fListeners) {
        listener.attributeChanged(event);
      }
    }
  }

  protected void setDisableListeners(boolean disabled) {
    fListeneredDisabled = disabled;
  }
  
}
