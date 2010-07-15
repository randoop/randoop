package randoop.plugin.internal.ui.options;

import java.util.ArrayList;
import java.util.List;

public abstract class Option implements IOption {
  
  List<IOptionChangeListener> fListeners = new ArrayList<IOptionChangeListener>();

  @Override
  public void addChangeListener(IOptionChangeListener listener) {
    fListeners.add(listener);
  }

  protected void notifyListeners(IOptionChangeEvent event) {
    for (IOptionChangeListener listener : fListeners) {
      listener.handleEvent(event);
    }
  }
}
