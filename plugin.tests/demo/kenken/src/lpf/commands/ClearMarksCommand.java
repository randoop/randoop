package lpf.commands;

import java.util.HashSet;
import java.util.Set;

import lpf.model.core.Cell;
import lpf.model.core.Value;

/**
 * 
 * @author Peter Kalauskas
 * @author Nam Do
 *
 */
public class ClearMarksCommand extends Command {
	final Cell cell;

	private Set<Character> oldMarks;
	
	public ClearMarksCommand(Cell cell)
	{
		super();
		this.cell = cell;
		
		this.oldMarks = new HashSet<Character>();
		Set<Value> marks = cell.getMarks();
		for (Value v : marks) {
			this.oldMarks.add(v.value);
		}
	}

	@Override
	protected void unperform() {
		for (Character c : this.oldMarks) {
			this.cell.addMark(new Value(c));
		}
	}

	@Override
	protected void perform() {
		this.cell.clearMarks();
	}
}
