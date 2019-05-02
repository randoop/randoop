package randoop.contract;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;
import randoop.Globals;
import randoop.main.RandoopBug;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.types.JavaTypes;
import randoop.types.TypeTuple;

/**
 * Represents the contract that an object must conform to its representation invariant, as expressed
 * in a user-supplied representation check method. A rep check method for a class must be declared
 * as a public instance method with no parameters in the given class, annotated with a
 * {@code @CheckRep} annotation, and have return type {@code boolean} or {@code void}. In the first
 * case, a return value {@code true} means the check passed, and {@code false}, or an exception,
 * means it failed. In the second case, normal return means the check passed, and an exception means
 * it failed.
 */
public final class CheckRepContract extends ObjectContract {

  public final Method checkRepMethod;
  private final TypedClassOperation operation;
  boolean returnsBoolean; // derived from checkRepMethod
  public final Class<?> declaringClass; // derived from checkRepMethod

  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (o == this) {
      return true;
    }
    if (!(o instanceof CheckRepContract)) {
      return false; // I collected the results of get_value()
    }
    CheckRepContract other = (CheckRepContract) o;
    return checkRepMethod.equals(other.checkRepMethod);
  }

  @Override
  public int hashCode() {
    return Objects.hash(checkRepMethod);
  }

  public CheckRepContract(Method checkRepMethod) {
    if (checkRepMethod == null) {
      throw new IllegalArgumentException("check-rep method cannot be null.");
    }
    int modifiers = checkRepMethod.getModifiers();
    assert Modifier.isPublic(modifiers);
    assert !Modifier.isStatic(modifiers);
    assert checkRepMethod.getParameterTypes().length == 0;
    this.operation = TypedOperation.forMethod(checkRepMethod);
    if (operation.getOutputType().equals(JavaTypes.BOOLEAN_TYPE)) {
      this.returnsBoolean = true;
    } else if (operation.getOutputType().equals(JavaTypes.VOID_TYPE)) {
      this.returnsBoolean = false;
    } else {
      throw new IllegalArgumentException("check-rep method must have void or boolean return type");
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
          return (Boolean) checkRepMethod.invoke(objects[0]);
        } else {
          checkRepMethod.invoke(objects[0]);
          return true;
        }
      } catch (IllegalArgumentException e) {
        // This will never happen.
        throw new RandoopBug(e);
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
  public TypeTuple getInputTypes() {
    return operation.getInputTypes();
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
  public String toCodeString() {
    StringBuilder b = new StringBuilder();
    b.append(Globals.lineSep);
    b.append("// Check representation invariant.").append(Globals.lineSep);
    if (returnsBoolean) {
      b.append("org.junit.Assert.assertTrue(");
      b.append("\"Representation invariant failed: ").append(toCommentString()).append("\", ");
      b.append("x0.").append(checkRepMethod.getName()).append("()");
      b.append(");");
    } else {
      b.append("x0.").append(checkRepMethod.getName()).append("();");
    }
    return b.toString();
  }
}
