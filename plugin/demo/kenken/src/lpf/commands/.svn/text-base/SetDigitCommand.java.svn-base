package lpf.commands;

import lpf.model.core.Cell;
import lpf.model.core.Value;

/**
 * 
 * @author Peter Kalauskas
 * @author Nam Do
 *
 */
public class SetDigitCommand extends Command {
	final Value oldValue;
	final Value newValue;
	final Cell cell;

	public SetDigitCommand(Cell cell, Value newValue)
	{
		super();
		this.cell = cell;
		this.oldValue = cell.getDigit();
		this.newValue = newValue;
	}

	protected void perform() {
		this.cell.setDigit(this.newValue);
	}

	protected void unperform() {
		this.cell.setDigit(this.oldValue);
	}
}
