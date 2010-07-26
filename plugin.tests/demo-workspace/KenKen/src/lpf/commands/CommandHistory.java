package lpf.commands;

import java.util.Stack;

public class CommandHistory {
	Stack<Command> undoStack;
	Stack<Command> redoStack;

	public CommandHistory() {
		undoStack = new Stack<Command>();
		redoStack = new Stack<Command>();
	}

	public void perform(Command c) {
		c.perform();
		if (c.canUndo()) {
			undoStack.add(c);

			// Clear the redo stack
			redoStack = new Stack<Command>();
		}
	}

	public boolean canRedo() {
		return redoStack.size() != 0;
	}

	public boolean canUndo() {
		return undoStack.size() != 0;
	}

	public boolean undo() {
		if (canUndo()) {
			Command c = undoStack.pop();
			c.undo();
			if (c.canRedo()) {
				redoStack.push(c);
			}
			return true;
		} else {
			return false;
		}
	}

	public boolean redo() {
		if (canRedo()) {
			Command c = redoStack.pop();
			c.redo();
			if (c.canUndo()) {
				undoStack.push(c);
			}
			return true;
		} else {
			return false;
		}
	}
}
