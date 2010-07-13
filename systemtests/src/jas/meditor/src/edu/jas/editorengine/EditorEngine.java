package edu.jas.editorengine;

import jscl.editor.Engine;
import org.python.util.PythonInterpreter;

public class EditorEngine extends Engine {
	PythonInterpreter interp=new PythonInterpreter();

	public EditorEngine() {
                //interp.execfile(EditorEngine.class.getResourceAsStream("/jas.py"));
	}

	public String eval(String str) {
		if(str.lastIndexOf("\n")==str.length()-1) {
			interp.exec(str);
			return str;
		} else return interp.eval(str).__str__().toString();
	}
}
