package randoop.experimental;

import java.util.HashMap;
import java.util.Map;

import randoop.Sequence;

class VariableRenamer {

	/**
	 * The sequence in which every variable will be renamed
	 * */
	public final Sequence sequence;
	
	/**
	 * A map storing the variable id to its name (after renaming)
	 * */
	public final Map<Integer, String> name_mapping;
	
	public VariableRenamer(Sequence sequence) {
		assert sequence != null : "The given sequence to rename can not be null";
		this.sequence = sequence;
		this.name_mapping = this.renameVarsInSequence();
	}
	
	/**
	 * Gets the name for the index-th variable (output by the i-th statement)
	 * */
	public String getRenamedVar(int index) {
		String name = this.name_mapping.get(index);
		if(name == null) {
			assert sequence.getStatementKind(index).getOutputType().equals(void.class) :
					"The index: " + index + "-th output should be void.";
			throw new Error("Error in Randoop, please report it.");
		}
		return name;
	}
	
	/**
	 * The map storing the occurrence number of the same class. The key is the class name,
	 * and the value is the number of variables with the given type. This field is only
	 * used in <code>rename</code> method.
	 * */
	private Map<String, Integer> name_counting_map = new HashMap<String, Integer>();
	private Map<Integer, String> renameVarsInSequence() {
		Map<Integer, String> index_var_map = new HashMap<Integer, String>();
		for(int i = 0; i < this.sequence.size(); i++) {
			Class<?> outputType = this.sequence.getStatementKind(i).getOutputType();
			if(outputType.equals(void.class)) {
				continue;
			}
			String rename = getVariableName(outputType);
			if(!name_counting_map.containsKey(rename)) {
				index_var_map.put(new Integer(i), rename + "0");
				//update and increase the counting in name map
				name_counting_map.put(rename, 1);
			} else {
				int num = name_counting_map.get(rename);
				index_var_map.put(new Integer(i), rename + num);
				//update and increase the counting in name map
				name_counting_map.put(rename, num + 1);
			}
		}
		return index_var_map;
	}
	
	/**
	 * Heuristically transforms variables to better names based on its type name.
	 * Here are some examples:
	 * int var0 = 1 will be transformed to int i0 = 1
	 * ClassName var0 = new ClassName() will be transformed to ClassName className = new ClassName()
	 * Class var0 = null will be transformed to Class clazz = null
	 * */
	private static String getVariableName(Class<?> clz) {
		assert !clz.equals(void.class) : "The given variable type can not be void!";
		// renaming for array type
		if (clz.isArray()) {
			while (clz.isArray()) {
				clz = clz.getComponentType();
			}
			return getVariableName(clz) + "_array";
		}
		//for object, string, class types
		if (clz.equals(Object.class)) {
			return "obj";
		} else if (clz.equals(String.class)) {
			return "str";
		} else if (clz.equals(Class.class)) {
			return "clazz";
		}
		//for primtivie types (including boxing or unboxing types
		else if (clz.equals(int.class) || clz.equals(Integer.class)) {
			return "i";
		} else if (clz.equals(double.class) || clz.equals(Double.class)) {
			return "d";
		} else if (clz.equals(float.class) || clz.equals(Float.class)) {
			return "f";
		} else if (clz.equals(short.class) || clz.equals(Short.class)) {
			return "s";
		} else if (clz.equals(boolean.class) || clz.equals(Boolean.class)) {
			return "b";
		} else if (clz.equals(char.class) || clz.equals(Character.class)) {
			return "char";
		} else if (clz.equals(long.class) || clz.equals(Long.class)) {
			return "long";
		} else if (clz.equals(byte.class) || clz.equals(Byte.class)) {
			return "byte";
		} else {
			//for other object types
			String name = clz.getSimpleName();
			if (Character.isUpperCase(name.charAt(0))) {
				return name.substring(0, 1).toLowerCase() + name.substring(1);
			} else {
				return name + "_instance";
			}
		}
	}
}