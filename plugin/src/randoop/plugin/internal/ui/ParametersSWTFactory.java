package randoop.plugin.internal.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import randoop.plugin.internal.core.TestKinds;
import randoop.plugin.internal.ui.options.IOption;
import randoop.plugin.internal.ui.options.OptionFactory;

public class ParametersSWTFactory {
  private final static int MARGIN = 5;
  private final static int INDENTATION = 6;
  private final static int VERTICAL_LABEL_SPACING = 9;
  private final static int VERTICAL_TEXT_SPACING = 6;
  
  public static List<IOption> createGenerationLimitComposite(Composite parent, ModifyListener modifyListener) {
    Composite comp = new Composite(parent, SWT.NONE);
    
    FormLayout formLayout = new FormLayout();
    formLayout.marginTop = MARGIN;
    formLayout.marginBottom = MARGIN;
    formLayout.marginRight = MARGIN;
    formLayout.marginLeft = MARGIN;
    formLayout.spacing = 4;
    comp.setLayout(formLayout);
    
    List<IOption> options = new ArrayList<IOption>();
    Font boldFont = getBoldFont(comp);
    
    Label stoppingCriterionTitle = new Label(comp, SWT.NONE);
    stoppingCriterionTitle.setText("Stopping criterion.");
    stoppingCriterionTitle.setFont(boldFont);
    Label stoppingCriterionSubtitle = new Label(comp, SWT.NONE);
    stoppingCriterionSubtitle.setText("Stop test generation after:");
    
    Label inputLimitLabel = new Label(comp, SWT.NONE);
    inputLimitLabel.setText("Randoop has generated");
    Text inputLimitText = new Text(comp, SWT.BORDER);
    Label inputLimitLabel2 = new Label(comp, SWT.NONE);
    inputLimitLabel2.setText("tests, OR");
    
    Label timeLimitLabel = new Label(comp, SWT.NONE);
    timeLimitLabel.setText("Randoop has generated tests for ");
    Text timeLimitText = new Text(comp, SWT.BORDER);
    Label timeLimitLabel2 = new Label(comp, SWT.NONE);
    timeLimitLabel2.setText(" seconds");
    Label convertedTimeLimit = new Label(comp, SWT.NONE);
    
    FormData data = new FormData();
    stoppingCriterionTitle.setLayoutData(data);
    
    data = new FormData();
    data.left = new FormAttachment(stoppingCriterionTitle);
    stoppingCriterionSubtitle.setLayoutData(data);
    
    // The input limit phrase
    data = new FormData();
    data.top = new FormAttachment(stoppingCriterionTitle, VERTICAL_LABEL_SPACING);
    data.left = new FormAttachment(0, INDENTATION);
    inputLimitLabel.setLayoutData(data);
    
    data = new FormData(computeWidth(inputLimitText, 8), SWT.DEFAULT);
    data.top = new FormAttachment(stoppingCriterionTitle, VERTICAL_TEXT_SPACING);
    data.left = new FormAttachment(inputLimitLabel);
    inputLimitText.setLayoutData(data);
    
    data = new FormData();
    data.top = new FormAttachment(stoppingCriterionTitle, VERTICAL_LABEL_SPACING);
    data.left = new FormAttachment(inputLimitText);
    inputLimitLabel2.setLayoutData(data);
    
    // The time limit phrase
    data = new FormData();
    data.top = new FormAttachment(inputLimitLabel, VERTICAL_LABEL_SPACING);
    data.left = new FormAttachment(0, INDENTATION);
    timeLimitLabel.setLayoutData(data);
    
    data = new FormData(computeWidth(timeLimitText, 5), SWT.DEFAULT);
    data.top = new FormAttachment(inputLimitLabel, VERTICAL_TEXT_SPACING);
    data.left = new FormAttachment(timeLimitLabel);
    timeLimitText.setLayoutData(data);
    
    data = new FormData();
    data.top = new FormAttachment(inputLimitLabel, VERTICAL_LABEL_SPACING);
    data.left = new FormAttachment(timeLimitText);
    timeLimitLabel2.setLayoutData(data);
    
    data = new FormData(computeWidth(convertedTimeLimit, 13), SWT.DEFAULT);
    data.top = new FormAttachment(inputLimitLabel, VERTICAL_LABEL_SPACING);
    data.left = new FormAttachment(timeLimitLabel2);
    convertedTimeLimit.setLayoutData(data);
    
    options.add(OptionFactory.createInputsLimitOption(inputLimitText));
    options.add(OptionFactory.createTimeLimitOption(timeLimitText, convertedTimeLimit));

    inputLimitText.addModifyListener(modifyListener);
    timeLimitText.addModifyListener(modifyListener);
    
    return options;
  }
  
  public static List<IOption> createOutputParametersComposite(Composite parent, ModifyListener modifyListener) {
    Composite comp = new Composite(parent, SWT.NONE);
    
    FormLayout formLayout = new FormLayout();
    formLayout.marginTop = MARGIN;
    formLayout.marginBottom = MARGIN;
    formLayout.marginRight = MARGIN;
    formLayout.marginLeft = MARGIN;
    formLayout.spacing = 4;
    comp.setLayout(formLayout);
    
    List<IOption> options = new ArrayList<IOption>();
    Font boldFont = getBoldFont(comp);
    
    Label outputParametersTitle = new Label(comp, SWT.NONE);
    outputParametersTitle.setText("Test output parameters.");
    outputParametersTitle.setFont(boldFont);
    
    Label testKindsLabel = new Label(comp, SWT.NONE);
    testKindsLabel.setText("Output tests that:");
    
    Combo testKindsCombo = new Combo(comp, SWT.READ_ONLY);
    testKindsCombo.setItems(TestKinds.getTranslatableNames());
    testKindsCombo.select(0);
    
    testKindsLabel.setToolTipText(RandoopMessages.RandoopOption_output_tests_tooltip);
    testKindsCombo.setToolTipText(RandoopMessages.RandoopOption_output_tests_tooltip);
    
    FormData data = new FormData();
    outputParametersTitle.setLayoutData(data);
    
    // The input limit phrase
    data = new FormData();
    data.top = new FormAttachment(outputParametersTitle, VERTICAL_LABEL_SPACING);
    data.left = new FormAttachment(0, INDENTATION);
    testKindsLabel.setLayoutData(data);
    
    data = new FormData();
    data.top = new FormAttachment(outputParametersTitle, VERTICAL_TEXT_SPACING);
    data.left = new FormAttachment(testKindsLabel);
    testKindsCombo.setLayoutData(data);
    
    options.add(OptionFactory.createTestKindsOption(testKindsCombo));
    
    testKindsCombo.addModifyListener(modifyListener);
    
    return options;
  }
  
  public static List<IOption> createAdvancedComposite(Composite parent, ModifyListener modifyListener, SelectionListener selectionListener) {
    Composite comp = SWTFactory.createComposite(parent, 1, 1, GridData.FILL_HORIZONTAL);

    GridLayout ld = (GridLayout) comp.getLayout();
    ld.marginWidth = 1;
    ld.marginHeight = 1;
    ld.marginLeft = MARGIN;
    ld.marginRight = MARGIN;
    ld.marginTop = MARGIN;
    ld.marginBottom = MARGIN;

    List<IOption> options = new ArrayList<IOption>();
    Font boldFont = getBoldFont(comp);
    
    Label advancedTitle = SWTFactory.createLabel(comp, "Advanced", 1);
    advancedTitle.setFont(boldFont);
    
    comp = SWTFactory.createComposite(comp, 2, 1, GridData.FILL_HORIZONTAL);
    ld = (GridLayout) comp.getLayout();
    ld.marginLeft = INDENTATION;
    
    Label randomSeedLabel = SWTFactory.createLabel(comp, RandoopMessages.RandoopOption_randomseed, 1);
    Text randomSeedText = SWTFactory.createSingleText(comp, 1);
    randomSeedLabel.setToolTipText(RandoopMessages.RandoopOption_randomseed_tooltip);
    randomSeedText.setToolTipText(RandoopMessages.RandoopOption_randomseed_tooltip);
    IOption randomSeed = OptionFactory.createRandomSeedOption(randomSeedText);

    Label maxTestSizeLabel = SWTFactory.createLabel(comp, RandoopMessages.RandoopOption_maxsize, 1);
    Text maxTestSizeText = SWTFactory.createSingleText(comp, 1);
    maxTestSizeLabel.setToolTipText(RandoopMessages.RandoopOption_maxsize_tooltip);
    maxTestSizeText.setToolTipText(RandoopMessages.RandoopOption_maxsize_tooltip);
    IOption maxTestSize = OptionFactory.createMaximumTestSizeOption(maxTestSizeText);

    Button threadTimeoutButton = SWTFactory.createCheckButton(comp, RandoopMessages.RandoopOption_usethreads, null, false, 1);
    Text threadTimeoutText = SWTFactory.createSingleText(comp, 1);
    threadTimeoutButton.setToolTipText(RandoopMessages.RandoopOption_usethreads_tooltip);
    threadTimeoutButton.setSelection(true);
    threadTimeoutText.setToolTipText(RandoopMessages.RandoopOption_timeout_tooltip);
    threadTimeoutText.setEnabled(threadTimeoutButton.getSelection());
    IOption threadTimeout = OptionFactory.createThreadTimeoutOption(threadTimeoutText);
    IOption useThreads = OptionFactory.createUseThreads(threadTimeout, threadTimeoutButton);
    
    Button nullRatioButton = SWTFactory.createCheckButton(comp, RandoopMessages.RandoopOption_forbid_null, null, false, 1);
    Text nullRatioText = SWTFactory.createSingleText(comp, 1);
    nullRatioButton.setToolTipText(RandoopMessages.RandoopOption_forbid_null_tooltip);
    nullRatioButton.setSelection(false);
    nullRatioText.setToolTipText(RandoopMessages.RandoopOption_null_ratio_tooltip);
    nullRatioText.setEnabled(nullRatioButton.getSelection());
    IOption nullRatio = OptionFactory.createNullRatioOption(nullRatioText);
    IOption useNull = OptionFactory.createUseNull(nullRatio, nullRatioButton);

    Label maxTestsPerFileLabel = SWTFactory.createLabel(comp,
        RandoopMessages.RandoopOption_testsperfile, 1);
    Text maxTestsPerFileText = SWTFactory.createSingleText(comp, 1);
    maxTestsPerFileLabel.setToolTipText(RandoopMessages.RandoopOption_testsperfile_tooltip);
    maxTestsPerFileText.setToolTipText(RandoopMessages.RandoopOption_testsperfile_tooltip);
    IOption maxTestsPerFile = OptionFactory.createMaximumTestsPerFileOption(maxTestsPerFileText);

    // Label maxTestsWrittenLabel = SWTFactory.createLabel(group,
    // RandoopMessages.RandoopOption_outputlimit, 1);
    // Text maxTestsWrittenText = SWTFactory.createSingleText(group, 1);
    // maxTestsWrittenLabel.setToolTipText(RandoopMessages.RandoopOption_outputlimit_tooltip);
    // maxTestsWrittenText.setToolTipText(RandoopMessages.RandoopOption_outputlimit_tooltip);
    // fMaxTestsWritten =
    // OptionFactory.createMaximumTestsWrittenOption(maxTestsWrittenText);

    options.add(randomSeed);
    options.add(maxTestSize);
    options.add(useThreads);
    options.add(useNull);
    options.add(maxTestsPerFile);
    // addOption(fMaxTestsWritten);
    
    randomSeedText.addModifyListener(modifyListener);
    maxTestSizeText.addModifyListener(modifyListener);
    threadTimeoutButton.addSelectionListener(selectionListener);
    threadTimeoutText.addModifyListener(modifyListener);
    nullRatioButton.addSelectionListener(selectionListener);
    nullRatioText.addModifyListener(modifyListener);
    maxTestsPerFileText.addModifyListener(modifyListener);
    // maxTestsWrittenText.addModifyListener(getBasicModifyListener());
    
    return options;
  }
  
  public static int computeWidth(Control control, int numChars) {
    GC gc = new GC(control);
    FontMetrics fm = gc.getFontMetrics();
    int charWidth = fm.getAverageCharWidth();
    int width = control.computeSize(charWidth * numChars, SWT.DEFAULT).x;
    gc.dispose();
    
    return width;
  }
  
  public static Font getBoldFont(Control c) {
    Font f = c.getFont();
    FontData[] fontData = f.getFontData();
    for (FontData fd : fontData) {
      fd.setStyle(fd.getStyle() | SWT.BOLD);
    }
    return new Font(f.getDevice(), fontData);
  }
}
