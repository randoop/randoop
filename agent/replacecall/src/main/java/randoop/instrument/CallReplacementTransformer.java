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
 * is used by the ReplaceCallAgent.
 *
 * @see ReplaceCallAgent
 */
public class CallReplacementTransformer implements ClassFileTransformer {

  /** Debug information about which classes are transformed and why */
  private static SimpleLog debug_transform =
      new SimpleLog(
          ReplaceCallAgent.debugPath + File.separator + "transform-log.txt",
          ReplaceCallAgent.debug);

  /** Debug information for instrumentation. */
  private static SimpleLog debug_instrument_inst =
      new SimpleLog(
          ReplaceCallAgent.debugPath + File.separator + "instrument-log.txt",
          ReplaceCallAgent.debug);

  /** Debug information on method mapping. */
  private static SimpleLog debug_map =
      new SimpleLog(
          ReplaceCallAgent.debugPath + File.separator + "method_mapping.txt",
          ReplaceCallAgent.debug);

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
   * @param replacementMap the concurrent hash map with method replacements
   * @param excludedPackagePrefixes the period-terminated prefixes for packages from which classes
   *     should not be transformed
   */
  CallReplacementTransformer(
      ConcurrentHashMap<MethodSignature, MethodSignature> replacementMap,
      Set<String> excludedPackagePrefixes) {
    this.replacementMap = replacementMap;
    this.excludedPackagePrefixes = excludedPackagePrefixes;
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
   * @see ReplaceCallAgent
   */
  @Override
  public byte[] transform(
      ClassLoader loader,
      String className,
      Class<?> classBeingRedefined,
      ProtectionDomain protectionDomain,
      byte[] classfileBuffer)
      throws IllegalClassFormatException {

    // Note: An uncaught exception within a transform method is equivalent to null being returned.
    // This method will only throw an IllegalClassFormatException, which is a ClassFileTransformer
    // convention.

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
      return null;
    }

    try {
      ClassGen cg = new ClassGen(c);
      if (transformClass(cg)) {
        // If transformClass returns true, it has also transformed the class.
        JavaClass javaClass = cg.getJavaClass();
        if (ReplaceCallAgent.debug) {
          Path filepath = ReplaceCallAgent.debugPath.resolve(className + ".class");
          javaClass.dump(filepath.toFile());
        }
        debug_transform.log("transform: EXIT class %s transformed", className);
        return javaClass.getBytes();
      } else {
        debug_transform.log(
            "transform: EXIT class %s not transformed (nothing to replace)", className);
        return null;
      }
    } catch (IllegalClassFormatException e) {
      debug_transform.log(
          "transform: EXIT transform of %s resulted in exception %s%n", className, e);
      System.out.format(
          "Unexpected exception %s (%s) in class transform of %s%n", e, e.getCause(), className);
      throw e;
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
   * Indicates whether the class loader is the bootstrap loader or by the first classloader. The
   * first classloader will either be a user-provided boot loader, or the extension class loader.
   * Since the predicate is for performance, we don't make the extra check to determine if the user
   * has given a boot loader.
   *
   * @param loader the classloader for the class
   * @return true if the named class is boot-loaded, false otherwise
   */
  private boolean isBootloadedClass(ClassLoader loader) {
    return loader == null || loader.getParent() == null;
  }

  /**
   * Indicates whether the class name begins with {@code java.awt.} or {@code javax.swing.} package
   * prefixes.
   *
   * @param fullClassName the fully-qualified class name
   * @return true if the class is in {@code java.awt.} or {@code javax.swing.}, false otherwise
   */
  private boolean isGUIClass(String fullClassName) {
    return fullClassName.startsWith("java.awt.") || fullClassName.startsWith("javax.swing.");
  }

  /**
   * Indicates whether the named class is defined in a package that is excluded.
   *
   * @param fullClassName the fully-qualified class name, must be non-null
   * @return true if any excluded package is a prefix of the class name, false otherwise
   */
  private boolean isExcludedClass(ClassLoader loader, String fullClassName) {
    // For performance, skip boot loaded classes, but include GUI classes
    if (isBootloadedClass(loader) && !isGUIClass(fullClassName)) {
      return true;
    }

    for (String prefix : excludedPackagePrefixes) {
      if (fullClassName.startsWith(prefix)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Processes each method in the given class replacing any specified calls. The replacements are
   * static methods.
   *
   * @param cg the BCEL class representation
   * @return true if the class has been transformed, false otherwise
   * @throws IllegalClassFormatException if an unexpected instruction is found where an invoke is
   *     expected
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

        // Update the max stack and Max Locals.
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
   * @throws IllegalClassFormatException if an unexpected instruction is found where an invoke is
   *     expected
   */
  private boolean transformMethod(MethodGen mg, InstructionFactory ifact, ConstantPoolGen pgen)
      throws IllegalClassFormatException {
    debug_transform.log("transformMethod: ENTER %s.%s%n", mg.getClassName(), mg.getName());
    InstructionList instructionList = mg.getInstructionList();
    InstructionHandle instructionHandle = instructionList.getStart();

    // Loop through each instruction, making substitutions
    boolean transformed = false;
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
   * @throws IllegalClassFormatException if an unexpected instruction is found where an invoke is
   *     expected
   */
  private InvokeInstruction getReplacementInstruction(
      MethodGen mg, Instruction inst, InstructionFactory ifact, ConstantPoolGen pgen)
      throws IllegalClassFormatException {
    debug_transform.log("getReplacementInstruction: ENTER %s%n", inst);
    if (!(inst instanceof InvokeInstruction)) {
      debug_transform.log("getReplacementInstruction: EXIT %s (not an InvokeInstruction)%n", inst);
      return null;
    }

    InvokeInstruction origInvocation = (InvokeInstruction) inst;
    MethodSignature origSig = MethodSignature.of(origInvocation, pgen);

    MethodSignature newSig = replacementMap.get(origSig);
    if (newSig == null) {
      debug_transform.log(
          "%s.%s: No replacement for %s%n", mg.getClassName(), mg.getName(), origSig.toString());
      debug_transform.log("getReplacementInstruction: EXIT %s%n", inst);
      return null;
    }

    debug_transform.log(
        "%s.%s: Replacing method %s with %s%n",
        mg.getClassName(), mg.getName(), origSig.toString(), newSig);

    InvokeInstruction newInvocation;

    switch (origInvocation.getOpcode()) {
      case Const.INVOKEINTERFACE:
      case Const.INVOKESPECIAL:
      case Const.INVOKEVIRTUAL:
        // TODO: I am not confident that these always have an implicit receiver.
        // Constructors (invokespecial) usually don't have one.
        // Maybe interface methods can now be static too?
        // It seems that a more correct implementation would be to check whether there is
        // a receiver, and if so to change the signature as below.
        /*
         * These calls have an implicit receiver ({@code this}) argument. Since the conversion is
         * to a static call, the receiver type is inserted at the beginning of the argument type
         * array. This argument has already been explicitly pushed onto the stack, so modifying the
         * call signature is enough.
         */
        Type instanceType = origInvocation.getReferenceType(pgen);
        Type[] arguments =
            BCELUtil.prependToArray(instanceType, origInvocation.getArgumentTypes(pgen));
        newInvocation =
            ifact.createInvoke(
                newSig.getClassname(),
                newSig.getName(),
                origInvocation.getReturnType(pgen),
                arguments,
                Const.INVOKESTATIC);
        break;
      case Const.INVOKESTATIC:
        newInvocation =
            ifact.createInvoke(
                newSig.getClassname(),
                newSig.getName(),
                origInvocation.getReturnType(pgen),
                origInvocation.getArgumentTypes(pgen),
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
                "Unexpected invoke instruction %s in %s.%s",
                origInvocation, mg.getClassName(), mg.getName());
        throw new IllegalClassFormatException(msg);
    }
    debug_transform.log(
        "getReplacementInstruction: EXIT %s => %s%n", origInvocation, newInvocation);
    return newInvocation;
  }

  /** Dumps out {@link #replacementMap} to the debug_map logger, if that logger is enabled. */
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

  /** Adds a shutdown hook that prints out {@link #replacementMap}, if debug logging is enabled. */
  void addMapFileShutdownHook() {
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
