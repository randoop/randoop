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
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.bcel.Const;
import org.apache.bcel.classfile.ClassParser;
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
import plume.BCELUtil;
import plume.SimpleLog;
import plume.UtilMDE;

/**
 * The {@code CallReplacementTransformer} replaces each call to method m1 by a call to method m2, as
 * specified by files loaded with {@link #readMapFile(File)} or {@link #readMapFile(Reader)}. It is
 * used by the MapCallsAgent.
 *
 * <p>Replacements may be given as original-replacement pairs of methods, classes, or packages. For
 * a replacement pair consisting of methods, the original method is replaced by the replacement
 * method. Replacement methods must be static. If the pair consists of classes, the methods of the
 * original class will be replaced by identically named static methods of a class with the
 * replacement name. (If the original method is non-static, the class
 *
 * @see MapCallsAgent
 */
public class CallReplacementTransformer implements ClassFileTransformer {

  //XXX fix path construction for windows
  /** Debug information about which classes are transformed and why */
  private static SimpleLog debug_transform =
      new SimpleLog(MapCallsAgent.debugPath + "/transform-log.txt", MapCallsAgent.debug);

  private static SimpleLog debug_instrument_inst =
      new SimpleLog(MapCallsAgent.debugPath + "/instrument-log.txt", MapCallsAgent.debug);

  /** Debug information on method maping */
  private static SimpleLog debug_map =
      new SimpleLog(MapCallsAgent.debugPath + "/method_mapping.txt", MapCallsAgent.debug);

  /** Map from a method to its replacement. See {@link #getReplacementMethod(MethodDef)}. */
  private final Map<MethodDef, MethodDef> replacementMap;

  /**
   * Map from a class or package name to its replacement. See {@link
   * #getReplacementMethod(MethodDef)}.
   */
  private final Map<String, String> classOrPackageReplacementsMap;

  /** The list of packages to exclude from transformation */
  private final Set<String> excludedPackages;

  /**
   * Create a {@link CallReplacementTransformer} that transforms method calls in classes other than
   * those named in the given exclusion set. (Replacements are added using {@link
   * #readMapFile(Reader)}.)
   *
   * <p>The transformer can be run by multiple threads, so the replacement maps use concurrent
   * implementations.
   *
   * @param excludedPackages the packages from which classes should not be transformed
   */
  CallReplacementTransformer(Set<String> excludedPackages) {
    this.excludedPackages = excludedPackages;
    this.replacementMap = new ConcurrentHashMap<>();
    this.classOrPackageReplacementsMap = new ConcurrentHashMap<>();
  }

  /**
   * {@inheritDoc}
   *
   * <p>Transforms the given class class by replacing calls to methods with corresponding calls as
   * determined by {@link #replacementMap} (and {@link #classOrPackageReplacementsMap}).
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

    if (className == null) {
      return null;
    }

    String fullClassName = className.replace("/", ".");

    if (isNonGUIBootClass(loader, fullClassName)) {
      return null;
    }

    debug_transform.log("%ntransform: ENTER %s%n", className);

    if (isExcludedClass(fullClassName)) {
      debug_transform.log("transform: EXIT ignoring excluded class %s%n", className);
      return null;
    }

    if (loader != null) {
      debug_transform.log(
          "transforming class %s, loader %s - %s%n", className, loader, loader.getParent());
    } else {
      debug_transform.log("transforming class %s, null - null%n", className);
    }

    // Parse the bytes of the classfile
    JavaClass c;
    try {
      ClassParser parser = new ClassParser(new ByteArrayInputStream(classfileBuffer), className);
      c = parser.parse();
    } catch (Exception e) {
      debug_transform.log("transform: EXIT parse of %s resulted in error %s%n", className, e);
      throw new RuntimeException("Unexpected parse exception: ", e);
    }

    try {
      // Get the class information
      ClassGen cg = new ClassGen(c);
      if (transformClass(cg)) {
        if (MapCallsAgent.debug) {
          JavaClass njc = cg.getJavaClass();
          njc.dump("/tmp/ret/" + njc.getClassName() + ".class");
        }
        debug_transform.log("transform: EXIT class %s transformed", className);
        return cg.getJavaClass().getBytes();
      } else {
        debug_transform.log("transform: EXIT class %s not transformed", className);
        return null;
      }
    } catch (Throwable e) {
      debug_transform.log(
          "transform: EXIT transform of %s resulted in exception %s%n", className, e);
      System.out.format(
          "Unexpected exception %s (%s) in class transform of %s%n", e, e.getCause(), className);
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Indicates whether the class is a non-AWT/Swing boot loaded class.
   *
   * @param loader the class loader for the class
   * @param fullClassName the fully-qualified name of the class
   * @return true if the named class is boot loaded and not in {@code java.awt.} or {@code
   *     javax.swing.}, false otherwise
   */
  private boolean isNonGUIBootClass(ClassLoader loader, String fullClassName) {
    return (loader == null || loader.getParent() == null)
        && !(fullClassName.startsWith("java.awt.") || fullClassName.startsWith("javax.swing."));
  }

  /**
   * Indicates whether the named class occurs in a package that is excluded. Tests whether one of
   * the excluded package names is a prefix of the fully-qualified class name.
   *
   * @param fullClassName the fully-qualified class name, must be non-null
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
  private boolean transformClass(ClassGen cg) {
    boolean transformed = false;
    ConstantPoolGen pgen = cg.getConstantPool();
    debug_transform.log("transformClass: ENTER %s", cg.getClassName());
    // Loop through each method in the class
    for (Method method : cg.getMethods()) {
      MethodGen mg = new MethodGen(method, cg.getClassName(), pgen);

      // Get the instruction list and skip methods with no instructions
      InstructionList instructionList = mg.getInstructionList();
      if (instructionList == null) {
        continue;
      }

      debug_instrument_inst.log(
          "%n%s.%s original code: %s%n", mg.getClassName(), mg.getName(), mg.getMethod().getCode());

      if (transformMethod(mg, new InstructionFactory(cg), pgen)) {
        transformed = true;
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

      debug_instrument_inst.log(
          "%s.%s modified code: %s%n%n", mg.getClassName(), mg.getName(), mg.getMethod().getCode());
    }
    cg.update();

    debug_transform.log("transformClass: EXIT %s", cg.getClassName());
    return transformed;
  }

  /**
   * Transforms the specified method to replace mapped calls.
   *
   * @param mg the method generator
   * @param ifact the instrument factory for the enclosing class of this method
   * @param pgen the constant pool for this class
   */
  private boolean transformMethod(MethodGen mg, InstructionFactory ifact, ConstantPoolGen pgen) {
    boolean transformed = false;
    // Loop through each instruction, making substitutions
    debug_transform.log("transformMethod: ENTER %s.%s%n", mg.getClassName(), mg.getName());
    InstructionList instructionList = mg.getInstructionList();
    for (InstructionHandle instructionHandle = instructionList.getStart();
        instructionHandle != null;
        ) {

      debug_instrument_inst.log(
          "%s.%s: instrumenting instruction %s%n",
          mg.getClassName(), mg.getName(), instructionHandle);

      // The next instruction for next iteration
      InstructionHandle nextHandle = instructionHandle.getNext();

      InvokeInstruction instruction =
          getReplacementInstruction(mg, instructionHandle.getInstruction(), ifact, pgen);
      debug_instrument_inst.log(
          " %s.%s new inst: %s%n", mg.getClassName(), mg.getName(), instruction);

      if (instruction != null) {
        transformed = true;
        replaceInstructions(instructionList, instructionHandle, instruction);
      }

      instructionHandle = nextHandle;
    }
    debug_transform.log("transformMethod: EXIT %s.%s%n", mg.getClassName(), mg.getName());
    return transformed;
  }

  /**
   * Transforms invoke instructions that match a replacement so that it calls the replacement method
   * instead of the original method.
   *
   * @param mg the BCEL representation of the method being transformed
   * @param inst the instruction to transform
   * @param ifact the instruction factory for the enclosing class
   * @return the transformed instruction list, or null if the instruction is not transformed
   */
  private InvokeInstruction getReplacementInstruction(
      MethodGen mg, Instruction inst, InstructionFactory ifact, ConstantPoolGen pgen) {
    debug_transform.log(
        "getReplacementInstruction: ENTER %s.%s%n", mg.getClassName(), mg.getName());
    if (!(inst instanceof InvokeInstruction)) {
      debug_transform.log(
          "getReplacementInstruction: EXIT %s.%s%n", mg.getClassName(), mg.getName());
      return null;
    }

    InvokeInstruction invocation = (InvokeInstruction) inst;
    MethodDef orig;
    try {
      orig = MethodDef.of(invocation, pgen);
    } catch (Throwable e) {

      debug_transform.log(
          "getReplacementInstruction: EXIT exception getting method for instruction %s of %s.%s: %s%n",
          invocation.toString(), mg.getClassName(), mg.getName(), e);
      return null;
    }
    MethodDef call = getReplacementMethod(orig);
    if (call == null) {
      debug_transform.log(
          "%s.%s: No replacement for %s%n", mg.getClassName(), mg.getName(), orig.toString());
      debug_transform.log(
          "getReplacementInstruction: EXIT %s.%s%n", mg.getClassName(), mg.getName());
      return null;
    }

    debug_transform.log(
        "%s.%s: Replacing method %s with %s%n",
        mg.getClassName(), mg.getName(), orig.toString(), call);

    InvokeInstruction instruction;

    switch (inst.getOpcode()) {
      case Const.INVOKEDYNAMIC:
      case Const.INVOKEINTERFACE:
      case Const.INVOKESPECIAL:
      case Const.INVOKEVIRTUAL:
        /*
         * These are invocations where an implicit argument occurs in the call.
         */
        Type instanceType = invocation.getReferenceType(pgen);
        Type[] arguments = BCELUtil.insert_type(instanceType, invocation.getArgumentTypes(pgen));
        instruction =
            ifact.createInvoke(
                call.getClassname(),
                call.getName(),
                invocation.getReturnType(pgen),
                arguments,
                Const.INVOKESTATIC);
        break;
      case Const.INVOKESTATIC:
        instruction =
            ifact.createInvoke(
                call.getClassname(),
                call.getName(),
                invocation.getReturnType(pgen),
                invocation.getArgumentTypes(pgen),
                Const.INVOKESTATIC);
        break;
      default:
        debug_transform.log(
            "getReplacementInstruction: EXIT wrong instruction type in %s.%s%n",
            mg.getClassName(), mg.getName());
        return null;
    }
    debug_transform.log("getReplacementInstruction: EXIT %s.%s%n", mg.getClassName(), mg.getName());
    return instruction;
  }

  /**
   * Returns the replacement method for the given method if one is determined by a method, class or
   * package replacement, or null otherwise.
   *
   * <p>Class or package replacements are represented as strings in the {@link
   * #classOrPackageReplacementsMap}. If the argument {@link MethodDef} does not already have a
   * replacement in {@link #replacementMap}, but method belongs to a class or package with a
   * replacement, a new {@link MethodDef} replacement is constructed for the method and is added to
   * the {@link #replacementMap}.
   *
   * @param methodDef the method to replace, must not be null
   * @return the replacement method, null if there is none
   */
  private MethodDef getReplacementMethod(MethodDef methodDef) {
    MethodDef replacement = replacementMap.get(methodDef);
    if (replacement != null) {
      return replacement;
    }

    // if no method replacement found, check for a class or package prefix and build replacement
    String classname = methodDef.getClassname();
    String classOrPackageReplacement = classOrPackageReplacementsMap.get(classname);
    if (classOrPackageReplacement != null) { // the class name has a replacement
      replacement = constructMethodReplacement(methodDef, classname, classOrPackageReplacement);
    } else { // otherwise, strip off classname and search for package
      int dotPos = classname.lastIndexOf('.');
      if (dotPos > 0) {
        String packageName = classname.substring(0, dotPos);
        classOrPackageReplacement = classOrPackageReplacementsMap.get(packageName);
        if (classOrPackageReplacement != null) {
          replacement =
              constructMethodReplacement(methodDef, packageName, classOrPackageReplacement);
        }
      }
    }

    return replacement;
  }

  /**
   * Constructs a {@link MethodDef} for a method by replacing the class (or package) name with the
   * given replacement, adds the replacement method to {@link #replacementMap}, and returns the new
   * {@link MethodDef}.
   *
   * @param methodDef the original {@link MethodDef}
   * @param classOrPackageName the class (or package) of {@code methodDef}
   * @param classOrPackageReplacement the replacement class (or package) name to construct the new
   *     {@link MethodDef}. Must be non-null.
   * @return the replacement {@link MethodDef} for {@code methodDef}
   */
  private MethodDef constructMethodReplacement(
      MethodDef methodDef, String classOrPackageName, String classOrPackageReplacement) {
    String replacementName =
        classOrPackageReplacement + methodDef.getClassname().substring(classOrPackageName.length());
    MethodDef replacement =
        new MethodDef(replacementName, methodDef.getName(), methodDef.getArgTypes());
    replacementMap.put(methodDef, replacement);
    return replacement;
  }

  /**
   * Replace the instruction in the original instruction list with the instructions in the new
   * instruction list. If this instruction was the target of any jumps or line numbers, replace them
   * with the first instruction in the new list.
   *
   * @param instructionList the original instruction list, must be non-null
   * @param instruction the instruction to replace
   * @param invokeInstruction the new instructions to substitute for the instruction, must be
   *     non-null and non-empty
   */
  private static void replaceInstructions(
      InstructionList instructionList,
      InstructionHandle instruction,
      InvokeInstruction invokeInstruction) {

    if ((invokeInstruction == null)) {
      throw new IllegalArgumentException("New instruction must be non-null");
    }

    // If there is only one new instruction, just replace it in the handle
    if (invokeInstruction.getLength() == 1) {
      instruction.setInstruction(invokeInstruction);
      return;
    }

    InstructionList newInstructionList = new InstructionList();
    newInstructionList.append(invokeInstruction);

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
   * Reads the replacement file specifying method calls that should be replaced by other method
   * calls. See the <a href="https://randoop.github.io/randoop/manual/index.html#map_calls">mapcall
   * user documentation</a> for details on the file format.
   *
   * @param map_file the file with map of method substitutions
   * @throws IOException if there is an error reading the file
   */
  void readMapFile(File map_file) throws IOException {
    readMapFile(new FileReader(map_file));
  }

  /** Regex string for Java identifiers */
  private static final String ID_STRING =
      "\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*";

  /**
   * Pattern to match a prefix of a fully qualified method name: either package or class. Does not
   * match the trailing "." that separates a package from a class.
   */
  private static final Pattern PREFIX_PATTERN =
      Pattern.compile(ID_STRING + "(\\." + ID_STRING + ")*");

  /**
   * Pattern for checking for a method signature on a replacement file line. (See the <a
   * href="https://randoop.github.io/randoop/manual/index.html#map_calls">mapcall user
   * documentation</a> for details on the file format.) Has two groups, first matching everything up
   * to first left parenthesis, and second matching everything within the parentheses.
   */
  private static final Pattern SIGNATURE_PATTERN = Pattern.compile("([^(]+)\\(([^)]*)\\)");

  /**
   * Reads the replacement file specifying method calls that should be replaced by other method
   * calls. See the <a href="https://randoop.github.io/randoop/manual/index.html#map_calls">mapcall
   * user documentation</a> for details on the file format.
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
      if (sigMatcher.find()) {
        String[] arguments = new String[0];
        String argString = sigMatcher.group(2);
        if (!argString.isEmpty()) {
          arguments = argString.split(",");
        }
        MethodDef orig = getMethod(sigMatcher.group(1).trim(), arguments);
        if (sigMatcher.find()) {
          arguments = new String[0];
          argString = sigMatcher.group(2);
          if (!argString.isEmpty()) {
            arguments = argString.split(",");
          }
          MethodDef replacement = getMethod(sigMatcher.group(1).trim(), arguments);
          replacementMap.put(orig, replacement);
        }
      } else {
        Matcher prefixMatcher = PREFIX_PATTERN.matcher(line);
        if (prefixMatcher.find()) {
          String orig = prefixMatcher.group(0).trim();
          if (prefixMatcher.find()) {
            classOrPackageReplacementsMap.put(orig, prefixMatcher.group(0).trim());
          }
        } else {
          System.err.format("Error in replacement file on line %n%s%n", line);
          System.exit(1);
        }
      }
    }

    logReplacementMap();
  }

  /**
   * @param fullMethodName fully-qualified name of method
   * @param args fully-qualified names of argument types
   */
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
  private void logReplacementMap() {
    if (debug_map.enabled()) {
      if (replacementMap.isEmpty()) {
        debug_map.log("no method replacements");
      } else {
        for (Map.Entry<MethodDef, MethodDef> entry : replacementMap.entrySet()) {
          debug_map.log("Method: %s (%d): %s", entry.getKey(), entry.hashCode(), entry.getValue());
        }
      }
      if (classOrPackageReplacementsMap.isEmpty()) {
        debug_map.log("no prefix replacements");
      } else {
        for (Map.Entry<String, String> entry : classOrPackageReplacementsMap.entrySet()) {
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
                logReplacementMap();
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

    static MethodDef of(InvokeInstruction invocation, ConstantPoolGen pgen) {
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
      return this.classname.equals(md.classname)
          && this.name.equals(md.name)
          && Arrays.equals(this.argTypes, md.argTypes);
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
      return String.format("%s.%s(%s)", classname, name, UtilMDE.join(argTypes, ", "));
    }

    String getClassname() {
      return classname;
    }

    String getName() {
      return name;
    }

    Type[] getArgTypes() {
      return argTypes;
    }
  }
}
