package lpf.commands;

public abstract class Command {
	boolean canUndo;

	protected Command() {
		this.canUndo = true;
	}

	public final boolean canUndo() {
		return canUndo;
	}

	public final boolean canRedo() {
		return !canUndo;
	}

	public final boolean undo() {
		if (!canUndo()) {
			return false;
		} else {
			unperform();
			canUndo = false;
			return true;
		}
	}

	public final boolean redo() {
		if (!canRedo()) {
			return false;
		} else {
			perform();
			canUndo = true;
			return true;
		}
	}

	protected abstract void perform();

	protected abstract void unperform();
}
