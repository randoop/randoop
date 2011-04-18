package randoop.experimental;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import plume.UtilMDE;

import randoop.ExecutableSequence;
import randoop.Globals;
import randoop.RMethod;
import randoop.Sequence;
import randoop.StatementKind;
import randoop.util.Files;

public class SequencePrettyPrinter {
	
	/**
	 * The list of sequences to print
	 * */
	public final List<ExecutableSequence> outputSequences;
	/**
	 * The output package name
	 * */
	public final String packageName;
	/**
	 * The output class name
	 * */
	public final String className;
	
	public SequencePrettyPrinter(List<ExecutableSequence> seqs, String packageName, String clzName) {
		assert seqs != null : "The sequences can not be null.";
		assert clzName != null : "The clazzName can not be null.";
		//note that the package name can be null, which means in a default package
		this.outputSequences = seqs;
		this.packageName = packageName;
		this.className = clzName;
	}
	
	/**
	 * Prints the given list of sequences in a string
	 * */
	public String prettyPrintSequences () {
		String[] all_import_classes = this.extractImportClasses();
		StringBuilder sb = new StringBuilder();
		//print package
		if(this.packageName != null && !this.packageName.trim().equals("")) {
			sb.append("package " + packageName + ";");
			sb.append(Globals.lineSep);
			sb.append(Globals.lineSep);
		}
		//print import
		for(String import_clz : all_import_classes) {
			sb.append("import ");
			sb.append(import_clz);
			sb.append(";");
			sb.append(Globals.lineSep);
		}
		sb.append(Globals.lineSep);
		sb.append("import junit.framework.TestCase;");
		sb.append(Globals.lineSep);
		sb.append(Globals.lineSep);
		
		//print class header
		sb.append("public class " + this.className + " extends TestCase { ");
		sb.append(Globals.lineSep);
		sb.append(Globals.lineSep);

                sb.append("  public static boolean debug = false;");
		sb.append(Globals.lineSep);
		sb.append(Globals.lineSep);
		
		int count = 0;
		for(ExecutableSequence eseq : this.outputSequences) {
			VariableRenamer renamer = new VariableRenamer(eseq.sequence);
			//print the test method
			sb.append(indent("public void test" + (count++) + "() throws Throwable {", 2));
			sb.append(Globals.lineSep);
			sb.append(Globals.lineSep);
                        sb.append("    if(debug) System.out.println(\"%n"+ this.className + ".test"+ count + "\");");
			sb.append(Globals.lineSep);
			sb.append(Globals.lineSep);
			//makes 4 indentation here
			SequenceDumper printer = new SequenceDumper(eseq, renamer);
			String codelines = printer.printSequenceAsCodeString();
			String[] all_code_lines = codelines.split(Globals.lineSep);
			for(int lineNum = 0; lineNum < all_code_lines.length; lineNum++) {
			    String codeline = all_code_lines[lineNum];
			    sb.append(indent(codeline, 4));
			    sb.append(Globals.lineSep);
			}
			
			sb.append(indent("}", 2));
			sb.append(Globals.lineSep);
			sb.append(Globals.lineSep);
		}
		
		sb.append(Globals.lineSep);
		sb.append("}");
		
		return sb.toString();
	}
	
	public File createFile(String output_dir) {
		assert output_dir != null : "The output dir can not be null.";
		File f = new File(output_dir);
		if(f.exists()) {
			assert f.isDirectory() : "The output dir: " + output_dir + " should be a dir.";
		} else {
			f.mkdirs();
		}
		File outputFile = new File(output_dir + System.getProperty("file.separator") + this.className + ".java");
		String content = this.prettyPrintSequences();
		try {
		   Files.writeToFile(content, outputFile);
		   return outputFile;
		} catch (IOException e) {
			throw new Error("Can not write in file: " + outputFile);
		}
	}
	
	private String[] extractImportClasses() {
		//keep all needed import
		Set<String> import_clz_set = new HashSet<String>();
		for(ExecutableSequence eseq : this.outputSequences) {
			Sequence sequence = eseq.sequence;
			int length = sequence.size();
			for(int i = 0; i < length; i++) {
				StatementKind statement = sequence.getStatementKind(i);
				Class<?> outputType = statement.getOutputType();
				outputType = this.getComponentType(outputType);
				
				//add return type
				if(needImport(outputType)) {
					import_clz_set.add(convertToCompilableNames(outputType));
				}
				//add input types
				for(Class<?> inputType : statement.getInputTypes()) {
					inputType = this.getComponentType(inputType);
					if(needImport(inputType)) {
						import_clz_set.add(convertToCompilableNames(inputType));
					}
				}
				//if it is a RMethod, consider the case it may be
				//static method
				if(statement instanceof RMethod) {
					RMethod rmethod = (RMethod)statement;
					if(rmethod.isStatic()) {
						Class<?> declaring_class = rmethod.getMethod().getDeclaringClass();
						if(needImport(declaring_class)) {
							import_clz_set.add(convertToCompilableNames(declaring_class));
						}
					}
				}
			}
		}
		//return the import class array
		String[] retArray = import_clz_set.toArray(new String[0]);
		Arrays.sort(retArray);
		return retArray;
	}
	
	/**
	 * Needs import a class?
	 * */
	private boolean needImport(Class<?> clazz) {
		return !clazz.equals(void.class) && !clazz.isPrimitive();
	}
	
	private String indent(String str, int num) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < num; i++) {
			sb.append(" ");
		}
		sb.append(str);
		return sb.toString();
	}
	
	private Class<?> getComponentType(Class<?> type) {
		if(type.isArray()) {
			while(type.isArray()) {
				type = type.getComponentType();
			}
		}
		return type;
	}
	
	private String convertToCompilableNames(Class<?> type) {
		 String retval = type.getName();
	     // If it's an array, it starts with "[".
	     if (retval.charAt(0) == '[') {
	       // Class.getName() returns a a string that is almost in JVML
	       // format, except that it slashes are periods. So before calling
	       // classnameFromJvm, we replace the period with slashes to
	       // make the string true JVML.
	       retval = UtilMDE.classnameFromJvm(retval.replace('.', '/'));
	     }

	     // If inner classes are involved, Class.getName() will return
	     // a string with "$" characters. To make it compilable, must replace with
	     // dots.
	     retval = retval.replace('$', '.');
	     return retval;
	}
}
