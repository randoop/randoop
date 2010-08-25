package randoop.plugin.internal.ui.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import randoop.plugin.RandoopPlugin;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {
  
  /*
   * (non-Javadoc)
   * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
   */
  @Override
  public void initializeDefaultPreferences() {
    IPreferenceStore store = RandoopPlugin.getDefault().getPreferenceStore();
    store.setDefault(IPreferenceConstants.P_BOOLEAN, true);
    store.setDefault(IPreferenceConstants.P_CHOICE, "choice2"); //$NON-NLS-1$
    store.setDefault(IPreferenceConstants.P_STRING, "Default value"); //$NON-NLS-1$
  }
}
