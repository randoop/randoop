package randoop.types;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Represents generic types that can occur as class declarations, formal
 * parameters or return types.
 * <p>
 * In Randoop, meant to be instantiated to a {@code ConcreteType} rather than
 * be used directly in tests.
 *
 * @see randoop.types.GenericClassType
 * @see randoop.types.GenericArrayType
 * @see randoop.types.GenericSimpleType
 */
public abstract class GenericType extends GeneralType {

  /**
   * {@inheritDoc}
   * @return true since this object represents a generic type
   */
  @Override
  public boolean isGeneric() {
    return true;
  }

  /**
   * Returns a concrete type for this generic type created by instantiating
   * the type parameters with a list of concrete type arguments.
   *
   * @param typeArguments  the type arguments
   * @return the concrete type
   */
  public ConcreteType instantiate(ConcreteType... typeArguments) {
    return null;
  }

  /**
   * Returns a concrete type for this generic type created by instantiating
   * the type parameters with a list of concrete type arguments.
   *
   * @param substitution  the type substitution
   * @return a {@code ConcreteType} constructed by substituting for type
   * parameters in this generic type
   */
  @Override
  public ConcreteType apply(Substitution substitution) {
    return null;
  }

  /**
   * Builds a generic type from the object reference by the given {@code Type}
   * reference.
   * The type must either represent a generic array, or a generic class.
   * @see randoop.types.GeneralType#forType(Type)
   *
   * @param type  the object from which the generic type is to be built
   * @return a {@code GenericType} object constructed from the given type
   * @throws IllegalArgumentException if the type is neither a generic array or
   * class
   */
  public static GenericType forType(Type type) {
    GeneralType t = GeneralType.forType(type);
    if (t.isGeneric()) {
      return (GenericType) t;
    }

    String msg = "unable to create generic type from type " + t.toString();
    throw new IllegalArgumentException(msg);
  }

  /**
   * Builds a generic type from a {@code Class} object.
   * (In Randoop, only expect to see {@code Class} objects for class under test,
   * and so this method does not support constructing arrays from them.)
   *
   * @param c  the class
   * @return a generic type object representing the class
   * @throws IllegalArgumentException if the class is not a generic type, or
   * {@code Class} object represents an array
   */
  public static GenericType forClass(Class<?> c) {
    if (c.getTypeParameters().length == 0) {
      throw new IllegalArgumentException("class must be a generic type");
    }

    if (c.isArray()) {
      throw new IllegalArgumentException("not supporting array construction from Class object");
    }

    return new GenericClassType(c);
  }

  /**
   * Returns the list of type parameter bounds for this type.
   *
   * @return the list of type parameter bounds for this type
   */
  public List<TypeBound> getBounds() {
    return null;
  }
}
