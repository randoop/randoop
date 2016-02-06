package randoop;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Set;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;

/**
 * Special class loader that instruments bytecode of certain loaded classes to
 * allow tracking of whether any method or constructor of instrumented classes
 * has been called.
 * Loaded classes have a public static method {@code randoop_checkAndReset()}
 * that returns true if some method or constructor of the class has been called.
 */
public class RandoopClassLoader extends ClassLoader {

  /** The class pool to use for accessing classes */
  private ClassPool pool;

  /** The set of names for classes to be instrumented */
  private Set<String> transformClassnames;

  /**
   * Creates a class loader from a {@code javassist.ClassPool}.
   *
   * @param pool  the class pool object
   * @param transformClassnames  the names of classes to be instrumented
   */
  public RandoopClassLoader(ClassLoader parent, ClassPool pool, Set<String> transformClassnames) {
    super(parent);
    this.pool = pool;
    this.transformClassnames = transformClassnames;
  }

  /**
   * {@inheritDoc}
   * Before returning class, first instruments a class whose names are in the
   * "transform" set held by this loader, otherwise returns the class untouched.
   * Instrumentation records whether a constructor or method of the class has
   * been called: adding a method {@code boolean randoop_checkAndReset()} that
   * returns whether the class has been used since loaded or last call, and then
   * resets the usage flag for the class to false.
   *
   * @return the instrumented class object for the given class name
   */
  @Override
  protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    Class<?> c = findLoadedClass(name);
    if (c != null) {
      return c;
    }

    if (! transformClassnames.contains(name)) {
      return super.loadClass(name, resolve);
    }

    CtClass cc = null;
    byte[] b = null;
    try {
      cc = pool.get(name);
      modifyBytecode(cc);
      b = cc.toBytecode();
    } catch (IOException e) {
      throw new ClassNotFoundException(name);
    } catch (CannotCompileException e) {
      throw new ClassNotFoundException(name);
    } catch (NotFoundException e) {
      throw new ClassNotFoundException(name);
    }

    c = defineClass(name, b, 0, b.length);
    if (resolve) {
      resolveClass(c);
    }
    return c;
  }

  /**
   * Instruments the bytecode of the given class object to track constructor and
   * method calls for the class.
   * Modifies each method and constructor to set an inserted private field that
   * keeps track
   * Adds a public method {@code boolean randoop_checkAndReset()}
   *
   * @param cc  the {@code javassist.CtClass} object
   * @throws CannotCompileException if inserted code doesn't compile
   */
  private void modifyBytecode(CtClass cc) {
    // add static field
    String flagFieldName = "randoop_classUsedFlag";
    try {
      CtField flagField = new CtField(CtClass.booleanType, flagFieldName, cc);
      flagField.setModifiers(Modifier.STATIC);
      cc.addField(flagField, "false");
    } catch (CannotCompileException e) {
      throw new Error("error adding instrumentation field: " + e);
    }
    try {
      // add static method to poll and reset the field
      String methodName = "randoop_checkAndReset";
      CtMethod pollMethod = new CtMethod(CtClass.booleanType, methodName, new CtClass[0], cc);
      pollMethod.setBody("{"
          + "boolean state = " + flagFieldName + ";"
          + flagFieldName + " = false" + ";"
          + "return state" + ";"
          + "}");
      pollMethod.setModifiers(Modifier.STATIC | Modifier.PUBLIC);
      cc.addMethod(pollMethod);
    } catch (CannotCompileException e) {
      throw new Error("error adding instrumentation method: " + e);
    }

    // add code to entry of each method to indicate that called
    String statementToSetFlag = cc.getName() + "#" + flagFieldName + " = true" + ";";

    try {
      for (CtMethod m : cc.getMethods()) {
        if (! Modifier.isNative(m.getModifiers())) {
          m.insertBefore(statementToSetFlag);
        }
      }
    } catch (CannotCompileException e) {
      throw new Error("error instrumenting method: " + e);
    }
    try {
      for (CtConstructor c : cc.getConstructors()) {
        c.insertBefore(statementToSetFlag);
      }

    } catch (CannotCompileException e) {
      throw new Error("error instrumenting constructor: " + e);
    }
  }
}
