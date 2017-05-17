package randoop.instrument;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantUtf8;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.CodeExceptionGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InstructionTargeter;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.LineNumberGen;
import org.apache.bcel.generic.LocalVariableGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import plume.BCELUtil;
import plume.SimpleLog;
import plume.StrTok;
import plume.UtilMDE;

import static java.lang.System.out;

/**
 * The Instrument class is responsible for modifying another class's bytecode. It replaces calls to
 * methods with calls to other methods as specified by a replacements file.
 * This allows arbitrary calls to be modified as desired. For example, all calls to
 * JOptionPane.showConfirmDialog (which would otherwise hang a regressions test) can be changed to
 * throw an exception, or simply print a message and continue.
 */
public class Instrument implements ClassFileTransformer {

  /** current Constant Pool */
  private static ConstantPoolGen pgen = null;

  /** Debug information about which classes are transformed and why */
  private static SimpleLog debug_transform = new SimpleLog(false);

  private static SimpleLog debug_instrument_inst = new SimpleLog(false);

  /** Debug information on method maping */
  private static SimpleLog debug_map = new SimpleLog("method_mapping.txt", true);

  /** the map of method replacements */
  private final Map<MethodDef, MethodDef> replacementMap;

  /** the map of class name prefix replacements */
  private final Map<String, String> prefixReplacementMap;

  Instrument() {
    this.replacementMap = new HashMap<>();
    this.prefixReplacementMap = new HashMap<>();
  }

  /**
   * {@inheritDoc}
   *
   * <p>Transforms class by replacing calls to methods with corresponding calls defined in this
   * class.
   */
  @Override
  public byte[] transform(
      ClassLoader loader,
      String className,
      Class<?> classBeingRedefined,
      ProtectionDomain protectionDomain,
      byte[] classfileBuffer)
      throws IllegalClassFormatException {

    debug_transform.log("In Transform: class = %s%n", className);
    String fullClassName = className.replace("/", ".");

    if (isBootClass(loader, fullClassName) || isRandoopClass(fullClassName)) {
      return null;
    }

    debug_transform.log(
        "transforming class %s, loader %s - %s%n", className, loader, loader.getParent());

    // Parse the bytes of the classfile, die on any errors
    JavaClass c;
    try {
      ClassParser parser = new ClassParser(new ByteArrayInputStream(classfileBuffer), className);
      c = parser.parse();
    } catch (Exception e) {
      throw new RuntimeException("Unexpected error", e);
    }

    try {
      // Get the class information
      ClassGen cg = new ClassGen(c);
      if (mapCalls(cg)) {
        if (Premain.debug) {
          JavaClass njc = cg.getJavaClass();
          njc.dump("/tmp/ret/" + njc.getClassName() + ".class");
        }
        return (cg.getJavaClass().getBytes());
      } else {
        debug_transform.log("class %s not transformed", className);
        return null;
      }
    } catch (Throwable e) {
      out.format("Unexpected error %s in transform", e);
      e.printStackTrace();
      return (null);
    }
  }

  /**
   * Don't instrument boot classes. We only want to instrument user classes classpath. Most boot
   * classes have the null loader, but some generated classes (such as those in sun.reflect) will
   * have a non-null loader. Some of these have a null parent loader, but some do not. The check for
   * the sun.reflect package is a hack to catch all of these. A more consistent mechanism to
   * determine boot classes would be preferrable.
   *
   * @param loader the class loader for the method
   * @param fullClassName the fully qualified class name of the method
   * @return true if the method should not be transformed, false, otherwise
   */
  private boolean isBootClass(ClassLoader loader, String fullClassName) {
    if (loader == null) {
      debug_transform.log("ignoring system class %s, class loader == null", fullClassName);
      return true;
    } else if (loader.getParent() == null) {
      debug_transform.log("ignoring system class %s, parent loader == null\n", fullClassName);
      return true;
    } else if (fullClassName.startsWith("sun.reflect")) {
      debug_transform.log("ignoring system class %s, in sun.reflect package", fullClassName);
      return true;
    } else if (fullClassName.startsWith("com.sun")) {
      System.out.printf("Class from com.sun package %s with nonnull loaders\n", fullClassName);
    }
    return false;
  }

  /**
   * Indicate whether the class with the given name is a Randoop class.
   *
   * @param fullClassName the class name to check
   * @return true if the class name begins with {@code "randoop."}, false otherwise
   */
  private boolean isRandoopClass(String fullClassName) {
    if (fullClassName.startsWith("randoop.")) {
      debug_transform.log("Not considering randoop class %s%n", fullClassName);
      return true;
    }
    return false;
  }

  /** Processes each method in cg replacing any specified calls with static user calls. */
  private boolean mapCalls(ClassGen cg) {
    boolean transformed = false;
    try {
      pgen = cg.getConstantPool();

      // Loop through each method in the class
      Method[] methods = cg.getMethods();
      for (Method method : methods) {
        MethodGen mg = new MethodGen(method, cg.getClassName(), pgen);

        // Get the instruction list and skip methods with no instructions
        InstructionList instructionList = mg.getInstructionList();
        if (instructionList == null) {
          continue;
        }

        if (Premain.debug) {
          out.format("Original code: %s%n", mg.getMethod().getCode());
        }

        instrumentMethod(mg, new InstructionFactory(cg));

        // Remove the Local variable type table attribute (if any).
        // Evidently, some changes we make require this to be updated, but
        // without BCEL support, that would be hard to do. Just delete it
        // for now (since it is optional, and we are unlikely to be used by
        // a debugger)
        for (Attribute a : mg.getCodeAttributes()) {
          if (isLocalVariableTypeTable(a)) {
            mg.removeCodeAttribute(a);
          }
        }

        // Update the instruction list
        mg.setInstructionList(instructionList);
        mg.update();

        // Update the max stack and Max Locals
        mg.setMaxLocals();
        mg.setMaxStack();
        mg.update();

        // Update the method in the class
        cg.replaceMethod(method, mg.getMethod());
        if (Premain.debug) {
          out.format("Modified code: %s%n", mg.getMethod().getCode());
        }

        transformed = true;
      }

      cg.update();
    } catch (Exception e) {
      out.format("Unexpected exception encountered: " + e);
      e.printStackTrace();
    }

    return transformed;
  }

  /**
   * Instrument the specified method to replace mapped calls.
   *
   * @param mg the method generator
   * @param ifact the instrument factory for the enclosing class of this method
   */
  private void instrumentMethod(MethodGen mg, InstructionFactory ifact) {

    // Loop through each instruction, making substitutions
    InstructionList instructionList = mg.getInstructionList();
    for (InstructionHandle instructionHandle = instructionList.getStart(); instructionHandle != null; ) {
      if (debug_instrument_inst.enabled()) {
        debug_instrument_inst.log("instrumenting instruction %s%n", instructionHandle);
      }

      // Remember the next instruction to process
      InstructionHandle nextHandle = instructionHandle.getNext();

      // Get the translation for this instruction (if any)
      InstructionList new_il = transformInstruction(mg, instructionHandle.getInstruction(), ifact);
      if (debug_instrument_inst.enabled()) {
        debug_instrument_inst.log("  new inst: %s%n", new_il);
      }

      // If this instruction was modified, replace it with the new
      // instruction list. If this instruction was the target of any
      // jumps or line numbers, replace them with the first
      // instruction in the new list.
      replaceInstructions(instructionList, instructionHandle, new_il);

      instructionHandle = nextHandle;
    }
  }

  /**
   * Transforms invoke instructions that match a replacement so that it calls the
   * specified method instead.
   *
   * @param mg the BCEL representation of a method
   * @param ifact the instruction factory for the enclosing class
   */
  private InstructionList transformInstruction(
      MethodGen mg, Instruction inst, InstructionFactory ifact) {

    switch (inst.getOpcode()) {
      case Const.INVOKEDYNAMIC:
      case Const.INVOKEINTERFACE:
      case Const.INVOKESPECIAL:
      case Const.INVOKESTATIC:
      case Const.INVOKEVIRTUAL:
        InstructionList instructionList = new InstructionList();
        InvokeInstruction invocation = (InvokeInstruction)inst;
        MethodDef orig = MethodDef.of(invocation);
        MethodDef call = getReplacement(orig);
        if (call != null) {
          debug_map.log(
                  "%s.%s: Replacing method %s with %s%n",
                  mg.getClassName(), mg.getName(), orig.toString(), call.getQualifiedName());
          instructionList.append(
                  ifact.createInvoke(
                          call.getClassname(),
                          call.getName(),
                          invocation.getReturnType(pgen),
                          invocation.getArgumentTypes(pgen),
                          invocation.getOpcode()));
        }
        return instructionList;

      default:
        return (null);
    }
  }

  /**
   * Returns the replacement method for the given method if one is determined by a method, class or
   * package replacement.
   *
   * @param orig the method to replace, must not be null
   * @return the replacement method, null if there is none
   */
  private MethodDef getReplacement(MethodDef orig) {
    MethodDef replacement = replacementMap.get(orig);
    if (replacement != null) {
      return replacement;
    }

    // check for a class or package prefix.
    String prefix = orig.getClassname();
    String prefixReplacement = prefixReplacementMap.get(prefix);
    // if not null, the class name has a replacement
    if (prefixReplacement == null) { // strip off class name and search
      int dotPos = prefix.lastIndexOf('.');
      if (dotPos > 0) {
        prefix = prefix.substring(0, dotPos);
        prefixReplacement = prefixReplacementMap.get(prefix);
      }
    }
    // if prefixReplacement is not null, then replace prefix in class name
    if (prefixReplacement != null) {
      String replacementName = prefixReplacement + orig.getClassname().substring(prefix.length());
      replacement = new MethodDef(replacementName, orig.getName(), orig.getArgTypes());
      replacementMap.put(orig, replacement);
    }

    return replacement;
  }

  /**
   * Replace instruction ih in list il with the instructions in new_il. If new_il is null, do
   * nothing.
   *
   * @param il the instruction list
   * @param ih the instruction
   * @param new_il the new instructions to substitute
   */
  private static void replaceInstructions(
      InstructionList il, InstructionHandle ih, InstructionList new_il) {

    if ((new_il == null) || new_il.isEmpty()) {
      return;
    }

    // If there is only one new instruction, just replace it in the handle
    if (new_il.getLength() == 1) {
      ih.setInstruction(new_il.getEnd().getInstruction());
      return;
    }

    // Get the start and end instruction of the new instructions
    InstructionHandle new_end = new_il.getEnd();
    InstructionHandle new_start = il.insert(ih, new_il);

    // Move all of the branches from the old instruction to the new start
    il.redirectBranches(ih, new_start);

    // Move other targets to the new instuctions.
    if (ih.hasTargeters()) {
      for (InstructionTargeter it : ih.getTargeters()) {
        if (it instanceof LineNumberGen) {
          it.updateTarget(ih, new_start);
        } else if (it instanceof LocalVariableGen) {
          it.updateTarget(ih, new_end);
        } else if (it instanceof CodeExceptionGen) {
          CodeExceptionGen exc = (CodeExceptionGen) it;
          if (exc.getStartPC() == ih) exc.updateTarget(ih, new_start);
          else if (exc.getEndPC() == ih) exc.updateTarget(ih, new_end);
          else if (exc.getHandlerPC() == ih) exc.setHandlerPC(new_start);
          else System.out.printf("Malformed CodeException: %s%n", exc);
        } else {
          System.out.printf("unexpected target %s%n", it);
        }
      }
    }

    // Remove the old handle. There should be no targeters left to it.
    try {
      il.delete(ih);
    } catch (Exception e) {
      throw new Error("Can't delete instruction", e);
    }
  }

  private boolean isLocalVariableTypeTable(Attribute a) {
    return (getAttributeName(a).equals("LocalVariableTypeTable"));
  }

  /**
   * Returns the attribute name for the specified attribute.
   *
   * @param a the attribute
   * @return the name for the attribute
   */
  private String getAttributeName(Attribute a) {
    int con_index = a.getNameIndex();
    Constant c = pgen.getConstant(con_index);
    return ((ConstantUtf8) c).getBytes();
  }



  /**
   * Parse a method declaration. The declaration should be in the following format:
   *
   * <p>fully-qualified-method-name (args)
   *
   * <p>where the arguments are comma separated and all arguments other than primitives should have
   * fully-qualified names. Arrays are indicating by trailing brackets. For example:
   *
   * <p>int int[] int[][] java.lang.String java.util.Date[]
   *
   * <p>The arguments are translated into BCEL types and a MethodDef is returned.
   */
  private MethodDef parseMethod(StrTok st) {

    // Get the method name
    String fullMethodName = st.need_word();
    String methodName = fullMethodName;
    String classname = "";
    int dotPos = fullMethodName.lastIndexOf('.');
    if (dotPos > 0) {
      methodName = fullMethodName.substring(dotPos + 1);
      classname = fullMethodName.substring(0, dotPos);
    }

    // Get the opening paren
    st.need("(");

    // Read the arguments
    ArrayList<String> args = new ArrayList<>();
    String tok = st.nextToken();
    if (tok != ")") { // interned
      st.pushBack();
      do {
        tok = st.need_word();
        args.add(tok);
      } while (st.nextToken() == ","); // interned
      st.pushBack();
      st.need(")");
    }

    // Convert the arguments to Type
    Type[] argTypes = new Type[args.size()];
    for (int i = 0; i < args.size(); i++) {
      argTypes[i] = BCELUtil.classname_to_type(args.get(i));
    }

    return new MethodDef(classname, methodName, argTypes);
  }

  /**
   * Reads the file that specifies method calls that should be replaced by other method calls. The
   * file is of the form:
   *
   * <p>[orig-package-name] [new-package-name]
   *
   * <p>Blank lines and // comments are ignored. A call to a method in a class of the
   * orig-package-name is replaced by a call to the same method within the new-package-name. All
   * method names and argument types should be fully-qualified.
   *
   * @param map_file the file with map of method substitutions
   * @throws IOException if file has missing regex
   */
  void readMapFile(File map_file) throws IOException {
    readMapFile(new FileReader(map_file), map_file.toString());
  }

  /**
   * Pattern for checking replacement file line for method.
   * Has two groups, first matching everything up to first left parenthesis, and second matching
   */
  private static final Pattern SIGNATURE_PATTERN = Pattern.compile("\"([^\\\\(]+)\\\\(([^\\\\)]*)\\\\)\"");

  void readMapFile(Reader in, String filename) throws IOException {
    LineNumberReader lr = new LineNumberReader(in);
    MapFileErrorHandler errorHandler = new MapFileErrorHandler(lr, filename);
    for (String line = lr.readLine(); line != null; line = lr.readLine()) {
      line = line.replaceFirst("//.*$", "").trim();
      if (line.length() == 0) {
        continue;
      }
      Matcher matcher = SIGNATURE_PATTERN.matcher(line);
      if (matcher.find()) {
        StrTok st = new StrTok(line, errorHandler);
        st.stok.wordChars('.', '.'); // make '.' a word constituent
        MethodDef orig = parseMethod(st);
        MethodDef replacement = parseMethod(st);
        replacementMap.put(orig, replacement);
      }
    }

    dumpMapCallsFile();
  }

  /** Dumps out the map list to the debug_map logger */
  private static void dumpMapCallsFile() {
    /*
    if (debug_map.enabled()) {
      for (MethodMapInfo mmi : map_calls_file) {
        debug_map.log("Class re '%s': %n", mmi.class_regex);
        for (MethodDef md : mmi.map.keySet()) {
          ReplacementClass mi = mmi.map.get(md);
          debug_map.log("  %s - %s [%d replacements]%n", md, mi.method_class, mi.cnt);
        }
      }
    }
    */
  }

  /** Adds a shutdown hook that prints out the results of the method maps */
  void addMapFileShutdownHook() {
    /*
    // Add a shutdown hook to printout some debug information
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread() {
              @Override
              public void run() {
                for (MethodMapInfo mmi : map_calls_file) {
                  dumpMapCallsFile();
                }
              }
            });
            */
  }

  /** Class that defines a method (by its fully-qualified name and argument types) */
  private static class MethodDef {

    /** fully-qualified class name */
    private final String classname;

    /** simple method name */
    private final String name;

    /** The argument types */
    private final Type[] argTypes;

    MethodDef(String classname, String name, Type[] argTypes) {
      this.classname = classname;
      this.name = name;
      this.argTypes = argTypes;
    }

    static MethodDef of(InvokeInstruction invocation) {
      return new MethodDef(
              invocation.getClassName(pgen),
              invocation.getMethodName(pgen),
              invocation.getArgumentTypes(pgen));
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof MethodDef)) {
        return false;
      }
      MethodDef md = (MethodDef) obj;
      if (!md.name.equals(this.name)) {
        return false;
      }
      if (this.argTypes.length != md.argTypes.length) {
        return false;
      }
      for (int i = 0; i < md.argTypes.length; i++) {
        if (!md.argTypes[i].equals(this.argTypes[i])) {
          return false;
        }
      }
      return true;
    }

    @Override
    public int hashCode() {
      return Objects.hash(name, argTypes);
    }

    @Override
    public String toString() {
      return String.format("%s.%s (%s)", classname, name, UtilMDE.join(argTypes, ", "));
    }

    String getClassname() {
      return classname;
    }

    String getName() {
      return name;
    }

    String getQualifiedName() {
      return classname + "." + name;
    }

    Type[] getArgTypes() {
      return argTypes;
    }
  }

  /** Class that reports tokenizing errors from the map file. */
  static class MapFileErrorHandler extends StrTok.ErrorHandler {
    LineNumberReader lr;
    String filename;

    MapFileErrorHandler(LineNumberReader lr, String filename) {
      this.lr = lr;
      this.filename = filename;
    }

    @Override
    public void tok_error(String s) {
      throw new RuntimeException(
              String.format("Error on line %d of %s: %s", lr.getLineNumber(), filename, s));
    }
  }
}
