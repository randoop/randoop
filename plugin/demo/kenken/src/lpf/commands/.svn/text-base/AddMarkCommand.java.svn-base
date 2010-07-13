package lpf.commands;

import lpf.model.core.Cell;
import lpf.model.core.Value;

/**
 * 
 * @author Peter Kalauskas
 * @author Nam Do
 *
 */
public class AddMarkCommand extends Command {
	final Value value;
	
	final Cell cell;

	public AddMarkCommand(Cell cell, Value value) {
		super();
		this.cell = cell;
		this.value = value;
	}

	protected void unperform() {
		this.cell.removeMark(this.value);
	}

	protected void perform() {
		this.cell.addMark(this.value);
	}
}
