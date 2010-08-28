package randoop.plugin.internal.ui.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;
import randoop.plugin.RandoopPlugin;

public class RandoopPreferencePage extends FieldEditorPreferencePage implements
    IWorkbenchPreferencePage {

  BooleanFieldEditor fRemeberParameters;
  
  RadioGroupFieldEditor fParameterStorageLocation;
  
  public RandoopPreferencePage() {
    super(GRID);
  }

  /**
   * Creates the field editors. Field editors are abstractions of the common GUI
   * blocks needed to manipulate various types of preferences. Each field editor
   * knows how to save and restore itself.
   */
  @Override
  public void createFieldEditors() {
    Composite comp = getFieldEditorParent();
    
    fRemeberParameters = new BooleanFieldEditor(
        IPreferenceConstants.P_REMEMBER_PARAMETERS,
        "&Remember Launch Configuration Parameters between uses of the launch shortcut",
        comp);
    addField(fRemeberParameters);

    fParameterStorageLocation = new RadioGroupFieldEditor(
        IPreferenceConstants.P_PARAMETER_STORAGE_LOCATION, "Remember parameters in:", 1,
        new String[][] { { "Workspace", RandoopPreferences.WORKSPACE_VALUE },
            { "Project being tested", RandoopPreferences.PROJECT_VALUE } },
            comp, true);
    Composite combo = fParameterStorageLocation.getRadioBoxControl(comp);
    GridData gd = (GridData) combo.getLayoutData();
    if (gd == null)
      gd = new GridData();
    gd.horizontalIndent = 10;
    combo.setLayoutData(gd);
    addField(fParameterStorageLocation);
    
    final Button b = (Button) fRemeberParameters.getDescriptionControl(comp);
    b.addSelectionListener(new SelectionListener() {

      public void widgetSelected(SelectionEvent e) {
        boolean enabled = fRemeberParameters.getBooleanValue();
        fParameterStorageLocation.setEnabled(enabled, getFieldEditorParent());
      }

      public void widgetDefaultSelected(SelectionEvent e) {
      }
    });
    
    boolean enabled = RandoopPreferences.doRememberParameters(getPreferenceStore());
    fParameterStorageLocation.setEnabled(enabled, comp);
  }
  
  public void init(IWorkbench workbench) {
    setDescription("General Randoop Settings:");
    setPreferenceStore(RandoopPlugin.getDefault().getPreferenceStore());
  }

}