package randoop.instrument;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.nio.file.Path;
import java.security.ProtectionDomain;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.bcel.Const;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;
import plume.BCELUtil;
import plume.SimpleLog;

/**
 * The {@code CallReplacementTransformer} replaces each call to method m1 by a call to method m2. It
 * is used by the MapCallsAgent.
 *
 * @see MapCallsAgent
 */
public class CallReplacementTransformer implements ClassFileTransformer {

  /** Debug information about which classes are transformed and why */
  private static SimpleLog debug_transform =
      new SimpleLog(
          MapCallsAgent.debugPath + File.separator + "transform-log.txt", MapCallsAgent.debug);

  private static SimpleLog debug_instrument_inst =
      new SimpleLog(
          MapCallsAgent.debugPath + File.separator + "instrument-log.txt", MapCallsAgent.debug);

  /** Debug information on method maping */
  private static SimpleLog debug_map =
      new SimpleLog(
          MapCallsAgent.debugPath + File.separator + "method_mapping.txt", MapCallsAgent.debug);

  /** Map from a method to its replacement. */
  private final ConcurrentHashMap<MethodSignature, MethodSignature> replacementMap;

  /** The list of package prefixes (package name + ".") to exclude from transformation. */
  private final Set<String> excludedPackagePrefixes;

  /**
   * Create a {@link CallReplacementTransformer} that transforms method calls in classes other than
   * those named in the given exclusion set.
   *
   * <p>The transformer can be run by multiple threads, so the replacement maps use concurrent
   * implementations.
   *
   * @param excludedPackagePrefixes the period-terminated prefixes for packages from which classes
   *     should not be transformed
   * @param replacementMap the concurrent hash map with method replacements
   */
  CallReplacementTransformer(
      Set<String> excludedPackagePrefixes,
      ConcurrentHashMap<MethodSignature, MethodSignature> replacementMap) {
    this.excludedPackagePrefixes = excludedPackagePrefixes;
    this.replacementMap = replacementMap;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Transforms the given class class by replacing calls to methods with corresponding calls as
   * determined by {@link #replacementMap}.
   *
   * <p>Excludes bootloaded classes that are not AWT/Swing classes. Other exclusions are determined
   * by the set of {@link #excludedPackagePrefixes}.
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

    if (isExcludedClass(loader, fullClassName)) {
      debug_transform.log("transform: ignoring excluded class %s%n", className);
      return null;
    }

    debug_transform.log("%ntransform: ENTER %s%n", className);

    // Parse the bytes of the classfile
    JavaClass c;
    try {
      ClassParser parser = new ClassParser(new ByteArrayInputStream(classfileBuffer), className);
      c = parser.parse();
    } catch (Exception e) {
      debug_transform.log("transform: EXIT parse of %s resulted in error %s%n", className, e);
      throw new RuntimeException("Unexpected parse exception", e);
    }

    try {
      // Get the class information
      ClassGen cg = new ClassGen(c);
      if (transformClass(cg)) {
        JavaClass javaClass = cg.getJavaClass();
        if (MapCallsAgent.debug) {
          Path filepath = MapCallsAgent.debugPath.resolve(javaClass.getClassName() + ".class");
          javaClass.dump(filepath.toFile());
        }
        debug_transform.log("transform: EXIT class %s transformed", className);
        return javaClass.getBytes();
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
   * Indicates whether the class is a non-AWT/Swing boot-loaded class.
   *
   * <p>This check is for performance, since attempting to transform all of {@code rt.jar} is
   * unnecessary. However, we want to transform AWT/Swing classes if they are loaded.
   *
   * <p>Actually checks whether the class is loaded by the bootstrap loader or by the first
   * classloader. The first classloader will either be a user-provided boot loader, or the extension
   * class loader. Since the predicate is mostly for performance, we don't make the extra check to
   * determine if the user has given a boot loader.
   *
   * @param loader the class loader for the class
   * @param fullClassName the fully-qualified name of the class
   * @return true if the named class is boot-loaded and not in {@code java.awt.} or {@code
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
  private boolean isExcludedClass(ClassLoader loader, String fullClassName) {
    if (isNonGUIBootClass(loader, fullClassName)) {
      return false;
    }

    for (String prefix : excludedPackagePrefixes) {
      if (fullClassName.startsWith(prefix)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Processes each method in the given class replacing any specified calls with static user calls.
   *
   * @param cg the BCEL class representation
   * @return true if the class has been transformed, false if either no calls were replaced or an
   *     error occured
   */
  private boolean transformClass(ClassGen cg) throws IllegalClassFormatException {
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

      try { // sometimes get exceptions on mg.getMethod().getCode().toString()
        debug_instrument_inst.log(
            "%n%s.%s original code: %s%n",
            mg.getClassName(), mg.getName(), mg.getMethod().getCode());
      } catch (Throwable e) {
        debug_instrument_inst.log(
            "%nException %s logging original code for %s.%s%n",
            e.getMessage(), mg.getClassName(), mg.getName());
      }

      if (transformMethod(mg, new InstructionFactory(cg), pgen)) {
        transformed = true;

        // Update the instruction list
        mg.setInstructionList(instructionList);
        mg.update();

        // Update the max stack and Max Locals
        // Since the locals and stack are not modified these three lines should not be necessary,
        // and are here to be cautious. The performance hit is expected to be minimal.
        mg.setMaxLocals();
        mg.setMaxStack();
        mg.update();

        // Update the method in the class
        cg.replaceMethod(method, mg.getMethod());
      }

      try {
        debug_instrument_inst.log(
            "%s.%s modified code: %s%n%n",
            mg.getClassName(), mg.getName(), mg.getMethod().getCode());
      } catch (Throwable e) {
        debug_instrument_inst.log(
            "%nException %s logging modified code for %s.%s%n",
            e.getMessage(), mg.getClassName(), mg.getName());
      }
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
   * @return true if the method was modified, false otherwise
   */
  private boolean transformMethod(MethodGen mg, InstructionFactory ifact, ConstantPoolGen pgen)
      throws IllegalClassFormatException {
    boolean transformed = false;
    // Loop through each instruction, making substitutions
    debug_transform.log("transformMethod: ENTER %s.%s%n", mg.getClassName(), mg.getName());
    InstructionList instructionList = mg.getInstructionList();
    InstructionHandle instructionHandle = instructionList.getStart();
    while (instructionHandle != null) {

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
        instructionHandle.setInstruction(instruction);
      }

      instructionHandle = nextHandle;
    }
    debug_transform.log("transformMethod: EXIT %s.%s%n", mg.getClassName(), mg.getName());
    return transformed;
  }

  /**
   * Returns the instruction to call a replacement method instead of the original method.
   *
   * @param mg the BCEL representation of the method being transformed
   * @param inst the instruction to replace
   * @param ifact the instruction factory for the enclosing class
   * @return the new instruction, or null if the instruction has no replacement
   */
  private InvokeInstruction getReplacementInstruction(
      MethodGen mg, Instruction inst, InstructionFactory ifact, ConstantPoolGen pgen)
      throws IllegalClassFormatException {
    debug_transform.log(
        "getReplacementInstruction: ENTER %s.%s%n", mg.getClassName(), mg.getName());
    if (!(inst instanceof InvokeInstruction)) {
      debug_transform.log(
          "getReplacementInstruction: EXIT %s.%s%n", mg.getClassName(), mg.getName());
      return null;
    }

    InvokeInstruction invocation = (InvokeInstruction) inst;
    MethodSignature orig = MethodSignature.of(invocation, pgen);

    MethodSignature call = replacementMap.get(orig);
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
      case Const.INVOKEINTERFACE:
      case Const.INVOKESPECIAL:
      case Const.INVOKEVIRTUAL:
        /*
         * These calls have an implicit argument of the  {@code this} pointer. Since coversion is
         * to a static call, need to insert the receiver type at the beginning of the argument type
         * array. This argument has already been explicitly pushed onto the stack, so modifying the
         * call signature is enough.
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
        // This should be impossible.  The only unhandled instruction type is Const.INVOKEDYNAMIC
        // which is a nameless method (lambda) and would not have a replacement.
        debug_transform.log(
            "getReplacementInstruction: EXIT Exception thrown due to wrong instruction type in %s.%s%n",
            mg.getClassName(), mg.getName());
        String msg =
            String.format(
                "Unexpected invoke instruction in %s.%s", mg.getClassName(), mg.getName());
        throw new IllegalClassFormatException(msg);
    }
    debug_transform.log("getReplacementInstruction: EXIT %s.%s%n", mg.getClassName(), mg.getName());
    return instruction;
  }

  /** Dumps out the map list to the debug_map logger */
  private void logReplacementMap() {
    if (debug_map.enabled()) {
      if (replacementMap.isEmpty()) {
        debug_map.log("no method replacements");
      } else {
        for (Map.Entry<MethodSignature, MethodSignature> entry : replacementMap.entrySet()) {
          debug_map.log("Method: %s (%d): %s", entry.getKey(), entry.hashCode(), entry.getValue());
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
}
