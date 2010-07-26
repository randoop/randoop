package lpf.commands;

import lpf.model.core.Cell;
import lpf.model.core.Value;

/**
 * 
 * @author Peter Kalauskas
 * @author Nam Do
 *
 */
public class RemoveMarkCommand extends Command {
	final Value value;
	final Cell cell;

	public RemoveMarkCommand(Cell cell, Value value) {
		super();
		this.cell = cell;
		this.value = value;
	}

	@Override
	protected void unperform() {
		this.cell.addMark(this.value);
	}

	@Override
	protected void perform() {
		this.cell.removeMark(this.value);
	}
}
