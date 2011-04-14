package randoop.experiments;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import randoop.StatementKind;
import randoop.util.PrimitiveTypes;

/**
 * Given a file with a list of classes, creates a driver
 * for exploring method sequences in JPF or jCUTE.
 *
 * The classes must be on the classpath: we use reflection
 * to extract information about each class.
 */
public class WriteModelCheckerDriver {

  private static String driverClassName = null; // Set in main method.
  private static List<Class<?>> classes = null; // Set in main method.
  private static List<StatementKind> statements = null; // Set in main method.
  private static PrintStream out = null; // Set in main method.
  private static int maxSequenceSize = -1; // Set in main method.
  private static Target target = Target.RANDOM;
  private static Set<Class<?>> allTypes = null; // Set in main method.

  public static enum Target { RANDOM, JPF, JCUTE }

  public static void writeDriver(Target t, String className, List<Class<?>> classList) throws IOException {
    target = t;
    driverClassName = className;
    classes = classList;

    maxSequenceSize = 10;
    findAllTypes();
    out = new PrintStream(new File(driverClassName + ".java"));
    out.println("import java.util.List;");
    out.println("import java.util.ArrayList;");
    out.println("import java.util.Random;");
    if (target == Target.JPF) out.println("import gov.nasa.jpf.jvm.Verify;");
    out.println("public class " + driverClassName + "{");
    printFields();
    printInputFinderMethods();
    printCallerMethods();
    printMain();
    out.println("}");
    out.close();
  }

  private static void findAllTypes() {
    allTypes = new LinkedHashSet<Class<?>>();
    for (int i = 0 ; i < statements.size() ; i++) {
      StatementKind s = statements.get(i);
      allTypes.add(s.getOutputType());
      List<Class<?>> inputTypes = s.getInputTypes();
      for (int ti = 0 ; ti < inputTypes.size() ; ti++) {
        allTypes.add(inputTypes.get(ti));
      }
    }
  }

  private static String getInteger(String upperBoundExclusive)  {
    if (target == Target.JCUTE) {
      return "cute.Cute.input.Integer()";
    } else if (target == Target.JPF) {
      // the call of random in JPF returns a number between 0 and upperBound.
      return "Verify.random(" + upperBoundExclusive + "-1)";
    } else if (target == Target.RANDOM) {
      return "random.nextInt(" + upperBoundExclusive + ")";
    } else {
      throw new RuntimeException();
    }
  }

  private static void printFields() {
    out.println("private static List values = new ArrayList();");
    if (target == Target.RANDOM) out.println("private static Random random = new Random();");
  }

  private static void printInputFinderMethods() {
    for (Class<?> c : allTypes) {
      if (c.equals(void.class)) continue;
      out.println(" public static List find_" + toIdentifierString(c) + "() { ");
      out.println("   List retval = new ArrayList();");
      out.println("   for (int i = 0 ; i < values.size() ; i++) {");
      out.println("     Object val = values.get(i);");
      out.println("     if (val == null) continue;");
      out.println("     if (!(val instanceof " + toSourceString(c) + ")) continue;");
      out.println("     retval.add(val);");
      out.println("   }");
      out.println("    return retval;");
      out.println("  }");
    }
  }

  // Each statement has associated with it a method that takes no arguments.
  // The method is in charge of finding inputs to the statement, calling
  // the statement, and storing its return value in the  "values" list.
  private static void printCallerMethods() {
    for (int i = 0 ; i < statements.size() ; i++) {
      StatementKind s = statements.get(i);
      out.println(" public static boolean call_" + i + "() { ");

      // Select inputs to statement.
      out.println("    List possibleInputs = null;");
      List<Class<?>> inputTypes = s.getInputTypes();
      String[] inputStrings = new String[inputTypes.size()];
      for (int ti = 0 ; ti < inputTypes.size() ; ti++) {
        Class<?> t = inputTypes.get(ti);
        out.println("    possibleInputs = find_" + toIdentifierString(t) + "();");
        out.println("    if (possibleInputs.size() == 0) return false;");
        out.println("    " + toSourceString(t) + " input" + ti +
            " = (" + toSourceString(t) + ")possibleInputs.get(" + getInteger("possibleInputs.size()") + ");");
        inputStrings[ti] = getVariableString(t, "input" + ti);
      }
      StringBuilder b = new StringBuilder();
      
      // NEED TO UPDATE. /////////////////////////////////
      // s.appendCode("result", inputStrings, b);
      if (true) throw new RuntimeException("unimplemented.");
      ////////////////////////////////////////////
      
      if (target == Target.JPF) out.println("Verify.beginAtomic();");
      out.println("    try {");
      out.println("      " + b.toString());
      Class<?> returnVariableType = s.getOutputType();
      if (!returnVariableType.equals(void.class)) out.println("values.add(" + asObject(returnVariableType, "result") + ");");
      out.println("    } catch (Throwable t) {");
      out.println("        if (t instanceof NullPointerException) {  }");
      out.println("    }");
      if (target == Target.JPF) out.println(" Verify.endAtomic(); ");
      out.println("  return true;");
      out.println("  }");
    }
  }

  private static String getVariableString(Class<?> t, String varName) {
    if (t.isPrimitive()) {
      if (t.equals(byte.class)) {
        return varName + ".byteVariable()";
      } else if (t.equals(short.class)) {
        return varName + ".shortVariable()";
      } else if (t.equals(int.class)) {
        return varName + ".intValue()";
      } else if (t.equals(long.class)) {
        return varName + ".longValue()";
      } else if (t.equals(float.class)) {
        return varName + ".floatVariable()";
      } else if (t.equals(double.class)) {
        return varName + ".doubleValue()";
      } else if (t.equals(char.class)) {
        return varName + ".charVariable()";
      } else {
        assert t.equals(boolean.class);
        return varName + ".booleanVariable()";
      }
    }
    return varName;
  }

  private static String asObject(Class<?> t, String varName) {
    if (t.isPrimitive()) {
      if (t.equals(byte.class)) {
        return "Byte.valueOf(" + varName + ")";
      } else if (t.equals(short.class)) {
        return "Short.valueOf(" + varName + ")";
      } else if (t.equals(int.class)) {
        return "Integer.valueOf(" + varName + ")";
      } else if (t.equals(long.class)) {
        return "Long.valueOf(" + varName + ")";
      } else if (t.equals(float.class)) {
        return "Float.valueOf(" + varName + ")";
      } else if (t.equals(double.class)) {
        return "Double.valueOf(" + varName + ")";
      } else if (t.equals(char.class)) {
        return "Character.valueOf(" + varName + ")";
      } else {
        assert t.equals(boolean.class);
        return "Boolean.valueOf(" + varName + ")";
      }
    }
    return varName;
  }


  private static void printMain() {
    out.println("public static void main(String[] args) { ");
    out.println("  for (int i = 0 ; i < " + maxSequenceSize + "; i++) {");
    out.println("    int nextMethod = " + getInteger(Integer.toString(statements.size())) + ";");
    out.println("    switch(nextMethod) {");
    for (int i = 0 ; i < statements.size() ; i++) {
      StatementKind s = statements.get(i);
      out.println("      case " + i + " : ");
      out.println("        if (!call_" + i + "()) { return; }");
      out.println("        break;");
    }
    out.println("      default : throw new RuntimeException(\"Execution shouldn't have reached here. This is a bug in the driver.\");");
    out.println("    }");
    out.println("  }");
    out.println("}");

  }


  private static String toSourceString(Class<?> t) {
    if (t.isPrimitive()) return PrimitiveTypes.getBoxedType(t.getName()).getName();
    return t.getName();
  }

  private static String toIdentifierString(Class<?> t) {
    return t.getName().replace('.','_').replace('$','_').replace(';','_').replace('[','_')
    + "_list";
  }
}
