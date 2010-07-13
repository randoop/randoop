package edu.jas.editorengine;

import jscl.editor.Engine;
import jscl.editor.EngineFactory;

public class EditorEngineFactory extends EngineFactory {
	public Engine getEngine() {
		return new EditorEngine();
	}
}
