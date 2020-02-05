package randoop.instrument;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import randoop.main.RandoopBug;

/**
 * A {@code java.lang.instrument.ClassTransformer} that instruments loaded classes to determine if
 * covered. Does the following instrumentation of each class:
 *
 * <ol>
 *   <li>Adds a static boolean flag to the class. Initially set to false.
 *   <li>Adds a statement at the beginning of each method and constructor that sets the flag.
 *   <li>Adds a static method that polls and resets the value of the flag.
 * </ol>
 *
 * Avoids instrumenting JDK and JUnit classes and skips interfaces. Otherwise, all other classes are
 * instrumented.
 *
 * @see CoveredClassAgent
 * @see #modifyClass(CtClass)
 */
public class CoveredClassTransformer implements ClassFileTransformer {

  /** the class pool used to load class files */
  private ClassPool pool;

  /** Create {@code CoveredClassTransformer}. */
  CoveredClassTransformer() {
    super();
    pool = ClassPool.getDefault();
  }

  /**
   * {@inheritDoc}
   *
   * <p>Transforms bytecode for a class by adding "covered" instrumentation. Avoids JDK and JUnit
   * classes, interfaces and any "frozen" classes that have already been loaded.
   */
  @Override
  public byte[] transform(
      ClassLoader loader,
      String className,
      Class<?> classBeingRedefined,
      ProtectionDomain protectionDomain,
      byte[] classfileBuffer)
      throws IllegalClassFormatException {

    byte[] bytecode;

    String qualifiedName = className.replace('/', '.');

    // For performance reasons, don't transform rt.jar classes except
    // java.awt and javax.swing.  (List derived from jdk1.8.0_71.)
    if (qualifiedName.startsWith("java.") // start of rt.jar name prefixes
        || qualifiedName.startsWith("javax.")
        || qualifiedName.startsWith("jdk.")
        || qualifiedName.startsWith("apple.")
        || qualifiedName.startsWith("com.apple.")
        || qualifiedName.startsWith("com.oracle.")
        || qualifiedName.startsWith("com.sun.")
        || qualifiedName.startsWith("org.ietf.")
        || qualifiedName.startsWith("org.jcp.")
        || qualifiedName.startsWith("org.omg.")
        || qualifiedName.startsWith("org.w3c")
        || qualifiedName.startsWith("org.xml.")
        || qualifiedName.startsWith("sun.") // end of rt.jar name prefixes
    ) {
      return null;
    }

    // run environment classes
    if (qualifiedName.startsWith("org.junit.")
        || qualifiedName.startsWith("org.hamcrest.")
        || qualifiedName.startsWith("org.gradle.")
        || qualifiedName.startsWith("worker.org.gradle.")) {
      return null;
    }

    // randoop classes
    if (qualifiedName.startsWith("randoop.")
        || qualifiedName.startsWith("replacecall.")
        || qualifiedName.startsWith("org.plumelib.")) {
      return null;
    }

    // agent dependency classes -- see build script for package relocation details
    if (qualifiedName.startsWith("coveredclass.")) {
      return null;
    }

    CtClass cc;
    try {
      cc = pool.makeClassIfNew(new ByteArrayInputStream(classfileBuffer));
    } catch (Exception e) {
      throw new RandoopBug("Unable to instrument file: " + e);
    }

    if (cc.isFrozen() || cc.isInterface()) {
      return null;
    }

    // OK to transform bytecode
    modifyClass(cc);
    try {
      bytecode = cc.toBytecode();
    } catch (IOException e) {
      throw new RandoopBug("Unable to convert instrumentation to bytecode: " + e);
    } catch (CannotCompileException e) {
      throw new RandoopBug("Error in instrumentation code: " + e);
    }
    // For debugging to write out the modified classes.
    // cc.debugDump = "./dump";
    cc.detach(); // done with class, remove from ClassPool

    return bytecode;
  }

  /**
   * Instruments the bytecode of the given class object to track constructor and method calls for
   * the class. Modifies each method and constructor to set an inserted private field that keeps
   * track. Adds a public method {@code boolean randoop_checkAndReset()}
   *
   * @param cc the {@code javassist.CtClass} object
   * @see #transform(ClassLoader, String, Class, ProtectionDomain, byte[])
   */
  private void modifyClass(CtClass cc) {
    // add static field
    String flagFieldName = "randoop_classUsedFlag";
    try {
      CtField flagField = new CtField(CtClass.booleanType, flagFieldName, cc);
      flagField.setModifiers(Modifier.STATIC);
      cc.addField(flagField, "false");
    } catch (CannotCompileException e) {
      throw new Error("error adding instrumentation field: " + e);
    }
    String flagFieldAccess = cc.getName() + "#" + flagFieldName;

    // add code to entry of each method to indicate that called
    String statementToSetFlag = flagFieldAccess + " = true" + ";";

    // instrument methods *before* adding polling method
    try {
      for (CtMethod m : cc.getDeclaredMethods()) {
        int mods = m.getModifiers();
        if (!Modifier.isNative(mods) && !Modifier.isAbstract(mods)) {
          m.insertBefore(statementToSetFlag);
        }
      }
    } catch (CannotCompileException e) {
      throw new Error("error instrumenting method: " + e);
    }

    // instrument constructors
    try {
      for (CtConstructor c : cc.getConstructors()) {
        c.insertBefore(statementToSetFlag);
      }
    } catch (CannotCompileException e) {
      throw new Error("error instrumenting constructor: " + e);
    }

    // add static method that polls and resets the covered flag
    try {
      String methodName = "randoop_checkAndReset";
      CtMethod pollMethod = new CtMethod(CtClass.booleanType, methodName, new CtClass[0], cc);
      pollMethod.setBody(
          "{"
              + "boolean state = "
              + flagFieldAccess
              + "; "
              + flagFieldAccess
              + " = false"
              + ";"
              + "return state"
              + ";"
              + "}");
      pollMethod.setModifiers(Modifier.STATIC | Modifier.PUBLIC);
      cc.addMethod(pollMethod);
    } catch (CannotCompileException e) {
      throw new Error("error adding instrumentation method: " + e);
    }
  }
}
