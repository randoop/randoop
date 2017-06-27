package randoop.instrument;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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

/**
 * The {@code CallReplacementTransformer} replaces each call to method m1 by a call to method m2. It
 * is used by the MapCallsAgent.
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

  /** Map from a method to its replacement. */
  private final ConcurrentHashMap<MethodDef, MethodDef> replacementMap;

  /** The list of packages to exclude from transformation */
  private final Set<String> excludedPackages;

  /**
   * Create a {@link CallReplacementTransformer} that transforms method calls in classes other than
   * those named in the given exclusion set.
   *
   * <p>The transformer can be run by multiple threads, so the replacement maps use concurrent
   * implementations.
   *
   * @param excludedPackages the packages from which classes should not be transformed
   * @param replacementMap the concurrent hash map with method replacements
   */
  CallReplacementTransformer(
      Set<String> excludedPackages, ConcurrentHashMap<MethodDef, MethodDef> replacementMap) {
    this.excludedPackages = excludedPackages;
    this.replacementMap = replacementMap;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Transforms the given class class by replacing calls to methods with corresponding calls as
   * determined by {@link #replacementMap}.
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

    debug_transform.log(
        "transforming class %s, loader %s - %s%n",
        className, loader, (loader == null ? null : loader.getParent()));

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
   * <p>This check is for performance, since attempting to transform all of {@code rt.jar} is
   * unnecessary. However, we want to transform AWT/Swing classes if they are loaded.
   *
   * <p>Actually checks whether the class is loaded by the bootstrap loader or by the first
   * classloader. The first classloader will either be a user provided boot loader, or the extension
   * class loader. Since predicate is mostly for performance, we don't make the extra check to
   * determine if the user has given a boot loader.
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
   * @param methodDef the method to replace, must not be null
   * @return the replacement method, null if there is none
   */
  private MethodDef getReplacementMethod(MethodDef methodDef) {
    return replacementMap.get(methodDef);
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
