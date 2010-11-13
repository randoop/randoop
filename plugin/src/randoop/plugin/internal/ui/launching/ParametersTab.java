package randoop.plugin.internal.ui.launching;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import randoop.plugin.RandoopPlugin;
import randoop.plugin.internal.ui.RandoopPluginImages;
import randoop.plugin.internal.ui.SWTFactory;
import randoop.plugin.internal.ui.options.IOption;
import randoop.plugin.internal.ui.options.OptionFactory;

public class ParametersTab extends OptionLaunchConfigurationTab {

  public ParametersTab() {
    addOptions(OptionFactory.createStoppingCriterionOptionGroupPlaceholders());
    addOptions(OptionFactory.createOutputParametersOptionGroupPlaceholders());
    addOptions(OptionFactory.createAdvancedOptionGroupPlaceholders());
  }
  
  public void createControl(Composite parent) {
    removeAllOptions();
    
    Composite comp = SWTFactory.createComposite(parent, 1, 1, SWT.HORIZONTAL);
    setControl(comp);
    
    GridLayout ld = (GridLayout) comp.getLayout();
    ld.marginWidth = 5;
    ld.marginHeight = 5;
    
    List<IOption> options = new ArrayList<IOption>();
    
    options.addAll(OptionFactory.createStoppingCriterionOptionGroup(comp, getBasicOptionChangeListener()));
    createSeparator(comp, 1);
    options.addAll(OptionFactory.createOutputParametersOptionGroup(comp, getBasicOptionChangeListener()));
    createSeparator(comp, 1);
    options.addAll(OptionFactory.createAdvancedOptionGroup(comp, getBasicOptionChangeListener()));
    
    for (IOption option : options) {
      addOption(option);
    }

    Button restoreDefaults = new Button(comp, 0);
    restoreDefaults.setText("Restore &Defaults");
    restoreDefaults.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
    restoreDefaults.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        restoreDefaults();
      }
    });
    
    restoreDefaults.setEnabled(true);
  }

  public String getName() {
    return "&Parameters";
  }

  @Override
  public String getId() {
    return "randoop.plugin.ui.launching.parametersTab"; //$NON-NLS-1$
  }
  
  @Override
  public Image getImage() {
    return RandoopPlugin.getDefault().getImageRegistry().get(RandoopPluginImages.IMG_VIEW_ARGUMENTS_TAB);
  }
  
}
