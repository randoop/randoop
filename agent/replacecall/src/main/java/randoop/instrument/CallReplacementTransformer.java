package randoop.instrument;

import java.io.ByteArrayInputStream;
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
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;
import org.plumelib.bcelutil.BcelUtil;
import org.plumelib.bcelutil.InstructionListUtils;
import org.plumelib.bcelutil.SimpleLog;

/**
 * The {@code CallReplacementTransformer} replaces each call to method m1 by a call to method m2. It
 * is used by the ReplaceCallAgent.
 *
 * @see ReplaceCallAgent
 */
public class CallReplacementTransformer extends InstructionListUtils
    implements ClassFileTransformer {

  /** Debug information about which classes are transformed and why */
  private static SimpleLog debug_transform =
      new SimpleLog(
          //ReplaceCallAgent.debugPath + File.separator + "replacecall-transform-log.txt", ReplaceCallAgent.debug);
          false);

  /** Debug information on method mapping. */
  private static SimpleLog debug_map =
      new SimpleLog(
          //ReplaceCallAgent.debugPath + File.separator + "replacecall-method_mapping.txt", ReplaceCallAgent.debug);
          false);

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
    //debug_instrument.enabled = ReplaceCallAgent.debug;
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
        debug_transform.log("transform: EXIT class %s transformed%n", className);
        return javaClass.getBytes();
      } else {
        debug_transform.log(
            "transform: EXIT class %s not transformed (nothing to replace)%n", className);
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
    // Have we modified this class?
    boolean transformed = false;
    InstructionFactory ifact = new InstructionFactory(cg);

    try {
      // Loop through each method in the class
      for (Method method : cg.getMethods()) {

        // The class data in StackMapUtils is not thread safe,
        // allow only one method at a time to be instrumented.
        synchronized (this) {
          pool = cg.getConstantPool();
          MethodGen mg = new MethodGen(method, cg.getClassName(), pool);

          // Get the instruction list and skip methods with no instructions
          InstructionList il = mg.getInstructionList();
          if (il == null) {
            continue;
          }

          boolean save_debug = debug_instrument.enabled;
          debug_instrument.enabled = false;

          // Prepare method for instrumentation.
          fetch_current_stack_map_table(mg, cg.getMajor());
          build_unitialized_NEW_map(il);
          fix_local_variable_table(mg);
          debug_instrument.enabled = save_debug;

          if (transformMethod(cg, mg, ifact)) {
            transformed = true;
          } else {
            continue;
          }

          // Clean up method after instrumentation.
          update_uninitialized_NEW_offsets(il);
          create_new_stack_map_attribute(mg);
          remove_local_variable_type_table(mg);

          // Update the instruction list
          mg.setInstructionList(il);
          mg.update();

          // Update the max stack and max locals.
          mg.setMaxLocals();
          mg.setMaxStack();
          mg.update();

          // Update the method in the class
          try {
            cg.replaceMethod(method, mg.getMethod());
          } catch (Exception e) {
            if ((e.getMessage()).startsWith("Branch target offset too large")) {
              System.out.printf(
                  "ReplaceCall warning: ClassFile: %s - method %s is too large to instrument and is being skipped.%n",
                  cg.getClassName(), mg.getName());
              continue;
            } else {
              throw e;
            }
          }

          debug_instrument.log(
              "%n%s.%s modified code: %s%n%n",
              mg.getClassName(), mg.getName(), mg.getMethod().getCode());
          cg.update();
        }
      }
    } catch (Exception e) {
      System.out.printf("Unexpected exception encountered: %s", e);
      e.printStackTrace();
    }

    return transformed;
  }

  /**
   * Transforms the specified method to replace mapped calls.
   *
   * @param cg the class generator
   * @param mg the method generator
   * @param ifact the instrument factory for the enclosing class of this method
   * @return true if the method was modified, false otherwise
   * @throws IllegalClassFormatException if an unexpected instruction is found where an invoke is
   *     expected
   */
  private boolean transformMethod(ClassGen cg, MethodGen mg, InstructionFactory ifact)
      throws IllegalClassFormatException {
    InstructionList il = mg.getInstructionList();
    InstructionHandle ih = il.getStart();

    // Have we modified this method?
    boolean transformed = false;

    // Loop through each instruction, making substitutions
    while (ih != null) {

      // The next instruction for next iteration
      InstructionHandle nextHandle = ih.getNext();

      InstructionList new_il = getReplacementInstruction(cg, mg, ifact, ih);

      if (new_il != null) {
        debug_instrument.log("%s.%s:%n", mg.getClassName(), mg.getName());
        transformed = true;
        replace_instructions(mg, il, ih, new_il);
      }

      ih = nextHandle;
    }
    return transformed;
  }

  /**
   * Returns the instruction to call a replacement method instead of the original method.
   *
   * @param cg the BCEL representation of the class being transformed
   * @param mg the BCEL representation of the method being transformed
   * @param ifact the instrument factory for the enclosing class of this instruction
   * @param ih the handle of the instruction to replace
   * @return replacement instruction list, or null if the instruction has no replacement
   * @throws IllegalClassFormatException if an unexpected instruction is found where an invoke is
   *     expected
   */
  private InstructionList getReplacementInstruction(
      ClassGen cg, MethodGen mg, InstructionFactory ifact, InstructionHandle ih)
      throws IllegalClassFormatException {

    Instruction inst = ih.getInstruction();
    if (!(inst instanceof InvokeInstruction)) {
      return null;
    }

    InvokeInstruction origInvocation = (InvokeInstruction) inst;
    MethodSignature origSig = MethodSignature.of(origInvocation, pool);
    MethodSignature newSig = replacementMap.get(origSig);

    String super_class_name = cg.getSuperclassName();
    String class_name = cg.getClassName();
    String method_name = mg.getName();
    String invoke_class = origSig.getClassname();
    String invoke_method = origSig.getName();

    //debug_transform.log("invoke_target: %s, invoke_class: %s, invoke_method: %s%n",
    //    invoke_target, invoke_class, invoke_method);
    //debug_transform.log("super: %s, host: %s.%s, target: %s%n", super_class_name, class_name, method_name, invoke_target);

    if (newSig == null) {
      debug_transform.log("%s.%s: No replacement for %s%n", class_name, method_name, origSig);
      return null;
    }

    boolean new_dup_removed = false;
    if (invoke_method.equals("<init>")) {
      if (method_name.equals("<init>")) {
        if (invoke_class.equals(class_name)) {
          debug_transform.log(
              "%s.%s: Do not modify call to this().<init>%n", class_name, method_name);
          return null;
        }
        if (invoke_class.equals(super_class_name)) {
          debug_transform.log(
              "%s.%s: Do not modify call to super().<init>%n", class_name, method_name);
          return null;
        }
      }
      // need to search backwards and delete "new", "dup" instruction pair
      InstructionHandle tih = ih.getPrev();
      while (tih != null) {
        if (tih.getInstruction().getOpcode() == Const.NEW) {
          if (tih.getNext().getInstruction().getOpcode() != Const.DUP) {
            // oops; we expect NEW to be immediatly followed by DUP
            break;
          }
          delete_instructions(mg, tih, tih.getNext());
          new_dup_removed = true;
          break;
        }
        tih = tih.getPrev();
      }
      if (!new_dup_removed) {
        throw new IllegalClassFormatException("Unable to find NEW DUP pair.");
      }
    }

    debug_transform.log(
        "%s.%s: Replacing method %s with %s%n", class_name, method_name, origSig, newSig);

    InvokeInstruction newInvocation;

    switch (origInvocation.getOpcode()) {
      case Const.INVOKEINTERFACE:
      case Const.INVOKESPECIAL:
      case Const.INVOKEVIRTUAL:
        Type instanceType = origInvocation.getReferenceType(pool);
        if (new_dup_removed) {
          // We are calling an <init> method replacement.  Use the same argument list as
          // the original call, but instead of a void return, the replacement will return
          // the object type. (As a replacement for the NEW we deleted earlier.)
          newInvocation =
              ifact.createInvoke(
                  newSig.getClassname(),
                  newSig.getName(),
                  instanceType,
                  origInvocation.getArgumentTypes(pool),
                  Const.INVOKESTATIC);
        } else {
          // These calls have an implicit receiver ({@code this}) argument. Since the conversion is
          // to a static call, the receiver type is inserted at the beginning of the argument type
          // array. This argument has already been explicitly pushed onto the stack, so modifying the
          // call signature is enough.
          Type[] arguments =
              BcelUtil.prependToArray(instanceType, origInvocation.getArgumentTypes(pool));
          newInvocation =
              ifact.createInvoke(
                  newSig.getClassname(),
                  newSig.getName(),
                  origInvocation.getReturnType(pool),
                  arguments,
                  Const.INVOKESTATIC);
        }
        break;

      case Const.INVOKESTATIC:
        newInvocation =
            ifact.createInvoke(
                newSig.getClassname(),
                newSig.getName(),
                origInvocation.getReturnType(pool),
                origInvocation.getArgumentTypes(pool),
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
    return build_il(newInvocation);
  }

  /** Dumps out {@link #replacementMap} to the debug_map logger, if that logger is enabled. */
  private void logReplacementMap() {
    if (debug_map.enabled()) {
      if (replacementMap.isEmpty()) {
        debug_map.log("No method replacements");
      } else {
        debug_map.log("%nMethod replacement list:%n");
        for (Map.Entry<MethodSignature, MethodSignature> entry : replacementMap.entrySet()) {
          debug_map.log("%s => %s%n", entry.getKey(), entry.getValue());
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
