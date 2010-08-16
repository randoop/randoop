package randoop;

import java.io.ObjectStreamException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Represents the contract that an object should conform to its representation
 * invariant, as expressed in a user-supplied representation check method.A rep
 * check method for a class must be declared as a public instance method with no
 * parameters in the given class, annotated with a <code>@CheckRep</code>
 * annotation, and have return type <code>boolean</code> or <code>void</code>.
 * In the first case, a return value <code>true</code> means the check passed,
 * and <code>false</code>, or an exception, means it failed. In the second case,
 * normal return means the check passed, and an exception means it failed.
 */
public final class CheckRepContract implements ObjectContract {

  // serial version id not actually used because this class
  // declares a writeReplace() method, but javac complains
  // if the field is missing.
  private static final long serialVersionUID = 3001544774236010230L;
  public final Method checkRepMethod;
  boolean returnsBoolean; // derived from checkRepMethod
  public final Class<?> declaringClass; // derived from checkRepMethod
  
  @Override
  public boolean equals(Object o) {
    if (o == null)
      return false;
    if (o == this)
      return true;
    if (!(o instanceof CheckRepContract)) {
      return false;  // I collected the results of get_value() 

    }
    CheckRepContract other = (CheckRepContract) o;
    return checkRepMethod.equals(other.checkRepMethod);
  }

  @Override
  public int hashCode() {
    int h = 7;
    h = h * 31 + checkRepMethod.hashCode();
    return h;
  }
  
  public CheckRepContract(Method checkRepMethod) {
    if (checkRepMethod == null) {
      throw new IllegalArgumentException("argument cannot be null.");
    }
    int modifiers = checkRepMethod.getModifiers();
    assert Modifier.isPublic(modifiers);
    assert !Modifier.isStatic(modifiers);
    assert checkRepMethod.getParameterTypes().length == 0;
    if (checkRepMethod.getReturnType().equals(boolean.class)) {
      this.returnsBoolean = true;
    } else if (checkRepMethod.getReturnType().equals(void.class)) {
      this.returnsBoolean = false;
    } else {
      throw new IllegalArgumentException("check rep method must have void or boolean return type");
    }
    this.checkRepMethod = checkRepMethod;
    this.declaringClass = checkRepMethod.getDeclaringClass();
  }
  
  @Override
  public boolean evaluate(Object... objects) throws Throwable {
    assert objects.length == 1;
    assert objects[0] != null;
    if (declaringClass.equals(objects[0].getClass())) {
      try {
        if (returnsBoolean) {
          return (Boolean)checkRepMethod.invoke(objects[0]);
        } else {
          checkRepMethod.invoke(objects[0]);
          return true;
        }
      } catch (IllegalArgumentException e) {
       // This will never happen.
        throw new BugInRandoopException(e);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      } catch (InvocationTargetException e) {
        throw e.getCause();
      }
    }
    return true;
  }

  @Override
  public int getArity() {
    return 1;
  }

  @Override
  public String toCommentString() {
    return "Check rep invariant (method " + checkRepMethod.getName() + ") for x0"; 
  }

  @Override
  public String get_observer_str() {
    return "CheckRep " + checkRepMethod.getName();
  }

  @Override
  public boolean evalExceptionMeansFailure() {
    return true;
  }

  @Override
  public String toCodeString() {
    StringBuilder b = new StringBuilder();
    b.append(Globals.lineSep);
        b.append("// Check representation invariant." + Globals.lineSep);
    if (returnsBoolean) {
      b.append("assertTrue(");
      b.append("\"Representation invariant failed: " + toCommentString() + "\", ");
      b.append(("x0." + checkRepMethod.getName() + "()"));
      b.append(");");
    } else {
      b.append(("x0." + checkRepMethod.getName() + "();"));
    }
    return b.toString();
  }
  
  /**
   * Serialize with a String version of checkRepMethod.
   */
  private Object writeReplace() throws ObjectStreamException {
    return new SerializableCheckRepContract(checkRepMethod);
  }


}
