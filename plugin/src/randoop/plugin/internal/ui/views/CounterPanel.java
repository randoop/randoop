package randoop.plugin.internal.ui.views;


import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * A panel with counters for the number of tests generated and errors revealed
 * during a run of Randoop.
 */
public class CounterPanel extends Composite {
	protected Text fNumberOfTests;
	protected Text fNumberOfErrors;
	private int fErrorCount;

	public CounterPanel(Composite parent) {
		super(parent, SWT.WRAP);
		GridLayout gridLayout= new GridLayout();
		gridLayout.numColumns= 4;
		gridLayout.makeColumnsEqualWidth= false;
		gridLayout.marginWidth= 0;
		setLayout(gridLayout);

		fErrorCount = 0;
		fNumberOfTests= createLabel("Tests generated:", "0"); //$NON-NLS-2$
		fNumberOfErrors= createLabel("Failures:", "0"); //$NON-NLS-2$
	}

	private Text createLabel(String name, String init) {

		Label label= new Label(this, SWT.NONE);
		label.setText(name);
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));

		Text value= new Text(this, SWT.READ_ONLY);
		value.setText(init);
		value.setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		value.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_BEGINNING));
		return value;
	}

	public void reset() {
	  fErrorCount = 0;
    fNumberOfErrors.setText(Integer.toString(fErrorCount));
	  setNumSequences(0);
	}

  public void setNumSequences(int value) {
    fNumberOfTests.setText(Integer.toString(value));
    redraw();
  }
  
  public void setErrorCount(int count) {
    fErrorCount = count;
    fNumberOfErrors.setText(Integer.toString(fErrorCount));
    redraw();
  }
  
  public void incrementErrorCount() {
    fErrorCount++;
    fNumberOfErrors.setText(Integer.toString(fErrorCount));
    redraw();
  }
  
}
