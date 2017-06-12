package randoop.instrument;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import org.checkerframework.checker.initialization.qual.Initialized;
import org.checkerframework.checker.interning.qual.UnknownInterned;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.UnknownKeyFor;
import plume.BCELUtil;
import plume.SimpleLog;
import plume.UtilMDE;

/**
 * The {@code CallReplacementTransformer} replaces calls in loaded classes to methods with calls to
 * other methods as specified by files loaded with {@link #readMapFile(File)} or {@link
 * #readMapFile(Reader)}.
 *
 * @see MapCallsAgent
 */
public class CallReplacementTransformer implements ClassFileTransformer {

  /** Current Constant Pool */
  private static ConstantPoolGen pgen = null;

  /** Debug information about which classes are transformed and why */
  private static SimpleLog debug_transform = new SimpleLog(MapCallsAgent.debug);

  private static SimpleLog debug_instrument_inst = new SimpleLog(MapCallsAgent.debug);

  /** Debug information on method maping */
  private static SimpleLog debug_map = new SimpleLog("method_mapping.txt", MapCallsAgent.debug);

  /** The map of method replacements */
  private final Map<MethodDef, MethodDef> replacementMap;

  /** The map of class name prefix replacements */
  private final Map<String, String> prefixReplacementMap;

  /** The list of packages to exclude from transformation */
  private final Set<String> excludedPackages;

  /**
   * Create a {@link CallReplacementTransformer} that transforms method calls in classes other than
   * those named in the given exclusion set. (Replacements are added using {@link
   * #readMapFile(Reader)}.)
   *
   * @param excludedPackages the packages from which classes should not be transformed
   */
  CallReplacementTransformer(Set<String> excludedPackages) {
    this.excludedPackages = excludedPackages;
    this.replacementMap = new HashMap<>();
    this.prefixReplacementMap = new HashMap<>();
  }

  /**
   * {@inheritDoc}
   *
   * <p>Transforms the given class class by replacing calls to methods with corresponding calls as
   * determined by {@link #replacementMap} (and {@link #prefixReplacementMap}).
   *
   * <p>Excludes bootloaded classes that are not AWT/Swing classes. Other exclusions are determined
   * by the set of {@link #excludedPackages}.
   *
   * @see MapCallsAgent
   */
  @Override
  public byte[] transform(
      ClassLoader loader,
      String className,
      Class<?> classBeingRedefined,
      ProtectionDomain protectionDomain,
      byte[] classfileBuffer)
      throws IllegalClassFormatException {

    debug_transform.log("In transform: class = %s%n", className);
    String fullClassName = className.replace("/", ".");
    if (isBootClass(loader, fullClassName)) {
      if (!isAWTSwingClass(fullClassName)) {
        debug_transform.log("ignoring non-AWT/Swing class %s, null class loader", fullClassName);
        return null;
      }
    }
    if (isExcludedClass(fullClassName)) {
      debug_transform.log("ignoring excluded class %s%n", fullClassName);
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
        if (MapCallsAgent.debug) {
          JavaClass njc = cg.getJavaClass();
          njc.dump("/tmp/ret/" + njc.getClassName() + ".class");
        }
        return (cg.getJavaClass().getBytes());
      } else {
        debug_transform.log("class %s not transformed", className);
        return null;
      }
    } catch (Throwable e) {
      System.out.format("Unexpected error %s in transform", e);
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Indicates whether the named class is in either the AWT ({@code java.awt}) or Swing ({@code
   * javax.swing}) packages.
   *
   * @param classname the fully qualified class name, must be non-null
   * @return true if the method is in either the AWT or Swing package, false otherwise
   */
  private boolean isAWTSwingClass(String classname) {
    return classname.startsWith("java.awt") || classname.startsWith("javax.swing");
  }

  /**
   * Indicate whether the class is boot loaded. Checks if the loader or the parent of the parent of
   * the loader is {@code null}.
   *
   * @param loader the class loader for the method
   * @param fullClassName the fully qualified class name of the method
   * @return true if the class is boot loaded, false, otherwise
   */
  private boolean isBootClass(ClassLoader loader, String fullClassName) {
    if (loader == null) {
      return true;
    } else if (loader.getParent() == null) {
      return true;
    }
    return false;
  }

  /**
   * Indicates whether the named class occurs in a package that is excluded. Tests whether one of
   * the excluded package names is a prefix of the fully qualified class name.
   *
   * @param fullClassName the fully qualified class name, must be non-null
   * @return true if any excluded package is a prefix of the class name, false otherwise
   */
  private boolean isExcludedClass(String fullClassName) {
    for (String excludedPackage : excludedPackages) {
      if (fullClassName.startsWith(excludedPackage)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Processes each method in the given class replacing any specified calls with static user calls.
   *
   * @param cg the BCEL class representation
   * @return true if the class has been transformed, false otherwise
   */
  private boolean mapCalls(ClassGen cg) {
    boolean transformed = false;
    pgen = cg.getConstantPool();

    // Loop through each method in the class
    for (Method method : cg.getMethods()) {
      MethodGen mg = new MethodGen(method, cg.getClassName(), pgen);

      // Get the instruction list and skip methods with no instructions
      InstructionList instructionList = mg.getInstructionList();
      if (instructionList == null) {
        continue;
      }

      debug_transform.log("Original code: %s%n", mg.getMethod().getCode());

      instrumentMethod(mg, new InstructionFactory(cg));

      // Remove the Local variable type table attribute (if any).
      // Evidently, some changes we make require this to be updated, but
      // without BCEL support, that would be hard to do. Just delete it
      // for now (since it is optional, and we are unlikely to be used by
      // a debugger)
      // Also remove the StackMap table because we are breaking it.
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
      debug_transform.log("Modified code: %s%n", mg.getMethod().getCode());

      transformed = true;
    }

    cg.update();

    return transformed;
  }

  /**
   * Transforms the specified method to replace mapped calls.
   *
   * @param mg the method generator
   * @param ifact the instrument factory for the enclosing class of this method
   */
  private void instrumentMethod(MethodGen mg, InstructionFactory ifact) {

    // Loop through each instruction, making substitutions
    InstructionList instructionList = mg.getInstructionList();
    for (InstructionHandle instructionHandle = instructionList.getStart();
        instructionHandle != null;
        ) {
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
   * Transforms invoke instructions that match a replacement so that it calls the replacement method
   * instead of the original method.
   *
   * @param mg the BCEL representation of the method being transformed
   * @param inst the instruction to transform
   * @param ifact the instruction factory for the enclosing class
   * @return the transformed instruction list, or null if the instruction is not trasformed
   */
  private InstructionList transformInstruction(
      MethodGen mg, Instruction inst, InstructionFactory ifact) {

    if (!(inst instanceof InvokeInstruction)) {
      return null;
    }

    InvokeInstruction invocation = (InvokeInstruction) inst;
    MethodDef orig;
    try {
      orig = MethodDef.of(invocation);
    } catch (Throwable e) {
      return null;
    }
    MethodDef call = getReplacement(orig);
    if (call == null) {
      debug_transform.log(
          "%s.%s: No replacement for %s%n", mg.getClassName(), mg.getName(), orig.toString());
      return null;
    }

    debug_transform.log(
        "%s.%s: Replacing method %s with %s%n",
        mg.getClassName(), mg.getName(), orig.toString(), call.getQualifiedName());

    InstructionList instructionList = new InstructionList();

    switch (inst.getOpcode()) {
      case Const.INVOKEDYNAMIC:
      case Const.INVOKEINTERFACE:
      case Const.INVOKESPECIAL:
      case Const.INVOKEVIRTUAL:
        /*
         * These are invocations where an implicit argument occurs in the call.
         */
        Type instanceType = invocation.getReferenceType(pgen);
        @UnknownInterned
        @UnknownKeyFor
        @NonNull
        @Initialized
        Type[] arguments = BCELUtil.insert_type(instanceType, invocation.getArgumentTypes(pgen));
        instructionList.append(
            ifact.createInvoke(
                call.getClassname(),
                call.getName(),
                invocation.getReturnType(pgen),
                arguments,
                Const.INVOKESTATIC));
        break;

      case Const.INVOKESTATIC:
        instructionList.append(
            ifact.createInvoke(
                call.getClassname(),
                call.getName(),
                invocation.getReturnType(pgen),
                invocation.getArgumentTypes(pgen),
                Const.INVOKESTATIC));
        break;

      default:
        return null;
    }
    return instructionList;
  }

  /**
   * Returns the replacement method for the given method if one is determined by a method, class or
   * package replacement.
   *
   * <p>Class or package replacements are represented as strings in the {@link
   * #prefixReplacementMap}. When the argument method belongs to one of these classes/packages, a
   * new {@link MethodDef} replacement is constructed for the method and is added to the {@link
   * #replacementMap}.
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
    // if prefixReplacement not null, the class name has a replacement
    // otherwise, strip off classname and search for package
    if (prefixReplacement == null) {
      int dotPos = prefix.lastIndexOf('.');
      if (dotPos > 0) {
        prefix = prefix.substring(0, dotPos);
        prefixReplacement = prefixReplacementMap.get(prefix);
      }
    }
    // if prefixReplacement is not null, then use it to replace prefix
    // in class name to create replacement method name
    if (prefixReplacement != null) {
      String replacementName = prefixReplacement + orig.getClassname().substring(prefix.length());
      replacement = new MethodDef(replacementName, orig.getName(), orig.getArgTypes());
      replacementMap.put(orig, replacement);
    }

    return replacement;
  }

  /**
   * Replace the instruction in the original instruction list with the instructions in the new
   * instruction list.
   *
   * @param instructionList the original instruction list
   * @param instruction the instruction to replace
   * @param newInstructionList the new instructions to substitute for the instruction, must be
   *     non-null and non-empty
   */
  private static void replaceInstructions(
      InstructionList instructionList,
      InstructionHandle instruction,
      InstructionList newInstructionList) {

    if ((newInstructionList == null) || newInstructionList.isEmpty()) {
      return;
    }

    // If there is only one new instruction, just replace it in the handle
    if (newInstructionList.getLength() == 1) {
      instruction.setInstruction(newInstructionList.getEnd().getInstruction());
      return;
    }

    // Get the start and end instruction of the new instructions
    InstructionHandle newEnd = newInstructionList.getEnd();
    InstructionHandle newBegin = instructionList.insert(instruction, newInstructionList);

    // Move all of the branches from the old instruction to the new start
    instructionList.redirectBranches(instruction, newBegin);

    // Move other targets to the new instuctions.
    if (instruction.hasTargeters()) {
      for (InstructionTargeter it : instruction.getTargeters()) {
        if (it instanceof LineNumberGen) {
          it.updateTarget(instruction, newBegin);
        } else if (it instanceof LocalVariableGen) {
          it.updateTarget(instruction, newEnd);
        } else if (it instanceof CodeExceptionGen) {
          CodeExceptionGen exc = (CodeExceptionGen) it;
          if (exc.getStartPC() == instruction) exc.updateTarget(instruction, newBegin);
          else if (exc.getEndPC() == instruction) exc.updateTarget(instruction, newEnd);
          else if (exc.getHandlerPC() == instruction) exc.setHandlerPC(newBegin);
          else System.out.printf("Malformed CodeException: %s%n", exc);
        } else {
          System.out.printf("unexpected target %s%n", it);
        }
      }
    }

    // Remove the old handle. There should be no targeters left to it.
    try {
      instructionList.delete(instruction);
    } catch (Exception e) {
      throw new Error("Can't delete instruction", e);
    }
  }

  /**
   * Indicates whether the method attribute is the local variable type table for the method.
   *
   * @param methodAttribute the method attribute
   * @return true if the attribute is a local variable type table, false otherwise
   */
  private boolean isLocalVariableTypeTable(Attribute methodAttribute) {
    int con_index = methodAttribute.getNameIndex();
    Constant c = pgen.getConstant(con_index);
    return (((ConstantUtf8) c).getBytes().equals("LocalVariableTypeTable"));
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
   * @throws IOException if there is an error reading the file
   */
  void readMapFile(File map_file) throws IOException {
    readMapFile(new FileReader(map_file));
  }

  /** Regex string for java identifiers */
  private static final String ID_STRING =
      "\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*";

  /**
   * Pattern to recognize a prefix of a fully qualified method name: either package or class. Does
   * not match the trailing "." that separates a package from a class.
   */
  private static final Pattern PREFIX_PATTERN =
      Pattern.compile(ID_STRING + "(\\." + ID_STRING + ")*");

  /**
   * Pattern for checking replacement file line for method. Has two groups, first matching
   * everything up to first left parenthesis, and second matching everything within the parentheses.
   */
  private static final Pattern SIGNATURE_PATTERN = Pattern.compile("([^(]+)\\(([^)]*)\\)");

  /**
   * Reads the replacement file specifying method calls that should be replaced by other method
   * calls. The file may have lines of the form:
   *
   * <p>[original-name] [replacement-name]
   *
   * <p>where both names are either method names, class names or package names.
   *
   * <p>Blank lines and // comments are ignored. A call to a method in a class of the
   * orig-package-name is replaced by a call to the same method within the new-package-name. All
   * method names and argument types should be fully-qualified.
   *
   * @param in the {@code Reader} for the replacement file
   * @throws IOException if there is an error reading from the file
   */
  void readMapFile(Reader in) throws IOException {
    LineNumberReader lr = new LineNumberReader(in);
    for (String line = lr.readLine(); line != null; line = lr.readLine()) {
      line = line.replaceFirst("//.*$", "").trim();
      if (line.length() == 0) {
        continue;
      }
      Matcher sigMatcher = SIGNATURE_PATTERN.matcher(line);
      if (sigMatcher.find() && sigMatcher.groupCount() == 2) {
        String[] arguments = new String[0];
        String argString = sigMatcher.group(2);
        if (!argString.isEmpty()) {
          arguments = argString.split(",");
        }
        MethodDef orig = getMethod(sigMatcher.group(1).trim(), arguments);
        if (sigMatcher.find() && sigMatcher.groupCount() == 2) {
          arguments = sigMatcher.group(2).split(",");
          MethodDef replacement = getMethod(sigMatcher.group(1).trim(), arguments);
          replacementMap.put(orig, replacement);
        }
      } else {
        Matcher prefixMatcher = PREFIX_PATTERN.matcher(line);
        if (prefixMatcher.find()) {
          String orig = prefixMatcher.group(0).trim();
          if (prefixMatcher.find()) {
            prefixReplacementMap.put(orig, prefixMatcher.group(0).trim());
          }
        }
      }
    }

    dumpMapCallsFile();
  }

  private MethodDef getMethod(String fullMethodName, String[] args) {
    String methodName = fullMethodName;
    String classname = "";
    int dotPos = fullMethodName.lastIndexOf('.');
    if (dotPos > 0) {
      methodName = fullMethodName.substring(dotPos + 1);
      classname = fullMethodName.substring(0, dotPos);
    }
    Type[] argTypes = new Type[args.length];
    for (int i = 0; i < args.length; i++) {
      argTypes[i] = BCELUtil.classname_to_type(args[i].trim());
    }
    return new MethodDef(classname, methodName, argTypes);
  }

  /** Dumps out the map list to the debug_map logger */
  private void dumpMapCallsFile() {
    if (debug_map.enabled()) {
      if (replacementMap.isEmpty()) {
        debug_map.log("no method replacements");
      } else {
        for (Map.Entry<MethodDef, MethodDef> entry : replacementMap.entrySet()) {
          debug_map.log("Method: %s (%d): %s", entry.getKey(), entry.hashCode(), entry.getValue());
        }
      }
      if (prefixReplacementMap.isEmpty()) {
        debug_map.log("no prefix replacements");
      } else {
        for (Map.Entry<String, String> entry : prefixReplacementMap.entrySet()) {
          debug_map.log("Prefix: %s : %s", entry.getKey(), entry.getValue());
        }
      }
    }
  }

  /** Adds a shutdown hook that prints out the results of the method maps */
  void addMapFileShutdownHook() {
    // Add a shutdown hook to printout some debug information
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread() {
              @Override
              public void run() {
                dumpMapCallsFile();
              }
            });
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
      if (!this.classname.equals(md.classname)) {
        return false;
      }
      if (!this.name.equals(md.name)) {
        return false;
      }
      if (this.argTypes.length != md.argTypes.length) {
        return false;
      }
      for (int i = 0; i < md.argTypes.length; i++) {
        if (!this.argTypes[i].equals(md.argTypes[i])) {
          return false;
        }
      }
      return true;
    }

    @Override
    public int hashCode() {
      if (argTypes.length > 0) {
        return Objects.hash(classname, name, Arrays.hashCode(argTypes));
      } else {
        return Objects.hash(classname, name);
      }
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
}
