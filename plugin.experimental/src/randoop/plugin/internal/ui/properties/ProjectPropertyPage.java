package randoop.plugin.internal.ui.properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import randoop.plugin.RandoopPlugin;
import randoop.plugin.internal.ui.SWTFactory;

public class ProjectPropertyPage extends PropertyPage implements
    IWorkbenchPropertyPage {
  private Preferences projectPreferences;

  /*
   * (non-Javadoc)
   * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control createContents(Composite parent) {
    // Get the selected project so that its Preferences object used be used
    IProject selectedProject = (IProject) getElement().getAdapter(IProject.class);
    if (selectedProject != null) {
      IScopeContext projectScope = new ProjectScope(selectedProject);
      projectPreferences = (Preferences) projectScope.getNode(RandoopPlugin.getPluginId());
    }

    Composite comp = new Composite(parent, SWT.NONE);
    comp.setLayout(new GridLayout(1, true));

    SWTFactory.createLabel(comp, "Placeholder", 1); //$NON-NLS-1$

    return comp;
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.jface.preference.PreferencePage#performOk()
   */
  @Override
  public boolean performOk() {
    // projectPreferences.put(IRandoopUIConstants.ATTR_, .getText());

    try {
      projectPreferences.flush();
    } catch (BackingStoreException e) {
      e.printStackTrace();
    }
    return super.performOk();
  }
}
