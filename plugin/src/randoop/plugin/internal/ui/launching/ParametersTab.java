package randoop.plugin.internal.ui.launching;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jdt.internal.debug.ui.JavaDebugImages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import randoop.plugin.internal.core.launching.RandoopArgumentCollector;
import randoop.plugin.internal.ui.ParametersSWTFactory;
import randoop.plugin.internal.ui.options.IOption;

public class ParametersTab extends OptionLaunchConfigurationTab {

  @Override
  public void setDefaults(ILaunchConfigurationWorkingCopy config) {
    RandoopArgumentCollector.restoreRandomSeed(config);
    RandoopArgumentCollector.restoreMaxTestSize(config);
    RandoopArgumentCollector.restoreUseThreads(config);
    RandoopArgumentCollector.restoreThreadTimeout(config);
    RandoopArgumentCollector.restoreUseNull(config);
    RandoopArgumentCollector.restoreNullRatio(config);
    
    RandoopArgumentCollector.restoreInputLimit(config);
    RandoopArgumentCollector.restoreTimeLimit(config);

    RandoopArgumentCollector.restoreTestKinds(config);
    // RandoopArgumentCollector.restoreMaxTestsWritten(config);
    RandoopArgumentCollector.restoreMaxTestsPerFile(config);
  }
  
  public void createControl(Composite parent) {
    Composite comp = SWTFactory.createComposite(parent, 1, 1, SWT.HORIZONTAL);
    setControl(comp);
    
    GridLayout ld = (GridLayout) comp.getLayout();
    ld.marginWidth = 5;
    ld.marginHeight = 5;
    
    List<IOption> options = new ArrayList<IOption>();
    
    options.addAll(ParametersSWTFactory.createGenerationLimitComposite(comp, getBasicModifyListener()));
    createSeparator(comp, 1);
    options.addAll(ParametersSWTFactory.createOutputParametersComposite(comp, getBasicModifyListener()));
    createSeparator(comp, 1);
    options.addAll(ParametersSWTFactory.createAdvancedComposite(comp, getBasicModifyListener(), getBasicSelectionListener()));
    
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
    return JavaDebugImages.get(JavaDebugImages.IMG_VIEW_ARGUMENTS_TAB);
  }
  
}
