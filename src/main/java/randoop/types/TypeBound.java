package randoop.types;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a type bound on a type variable occurring as a type parameter in
 * a class, interface, method or constructor. (See JLS section 4.4)
 * In Java, a type bound is either a type variable, a class type, an interface
 * type, or an intersection type of class and interface bounds.
 * This class represents a bound as concretely as possible based on the values
 * returned by {@link java.lang.reflect.TypeVariable#getBounds()}.
 * @see ConcreteTypeBound
 * @see GenericTypeBound
 * @see IntersectionTypeBound
 */
public abstract class TypeBound {

  /**
   * Determines if this is an upper bound for the concrete argument type.
   *
   * @param argType  the concrete argument type
   * @return true if this bound is satisfied by the concrete type when the
   *         substitution is used on the bound, false otherwise
   */
  public boolean isSatisfiedBy(ConcreteType argType, Substitution subst) {
    return false;
  }

  /**
   * Creates a {@code TypeBound} object from the given array of bounds.
   * If there is more than one type, the returned bound is an intersection type.
   *
   * @param bounds  the types representing a type parameter bound
   * @return the type bound constructed from the given {@code Type} objects
   */
  public static TypeBound fromTypes(Type... bounds) {
    if (bounds == null) {
      throw new IllegalArgumentException("bounds must be non null");
    }

    if (bounds.length == 1) {
      return TypeBound.fromType(bounds[0]);
    } else {
      List<TypeBound> boundList = new ArrayList<TypeBound>();
      for (Type t : bounds) {
        boundList.add(TypeBound.fromType(t));
      }
      return new IntersectionTypeBound(boundList);
    }
  }

  /**
   * Creates a {@code TypeBound} object from a single
   * {@code java.lang.reflect.Type}.
   * Tests for types that are represented by {@code Class} objects, or
   * {@code java.lang.reflect.ParameterizedType} objects.
   *
   * @param type  the type for type bound
   * @return a type bound that ensures the given type is satisfied as an upper
   *         bound
   * @throws IllegalArgumentException if a parameterized type is given but the
   *         rawtype is not a Class object
   */
  private static TypeBound fromType(Type type) {

    if (type instanceof Class<?>) {
      Class<?> c = (Class<?>) type;
      return new ConcreteTypeBound(ConcreteType.forClass(c, new ConcreteType[0]));
    }

    if (type instanceof java.lang.reflect.ParameterizedType) {

      java.lang.reflect.ParameterizedType pt = (java.lang.reflect.ParameterizedType) type;
      Type rawType = pt.getRawType();
      if (!(rawType instanceof Class<?>)) {
        throw new IllegalArgumentException("Rawtype expected to be a Class");
      }

      Class<?> runtimeType = (Class<?>) rawType;

      // Can't tell whether type is a generic or parameterized type
      // so have to inspect the arguments
      Type[] arguments = pt.getActualTypeArguments();
      // array for concrete arguments
      ConcreteType[] conTypes = new ConcreteType[arguments.length];

      for (int i = 0; i < arguments.length; i++) {
        if (arguments[i] instanceof Class<?>) { // concrete
          conTypes[i] = ConcreteType.forClass((Class<?>) arguments[i], new ConcreteType[0]);
        } else { // generic -- just bail to generic bound constructor
          return new GenericTypeBound(runtimeType, arguments);
        }
      }
      return new ConcreteTypeBound(ConcreteType.forClass(runtimeType, conTypes));
    }

    throw new IllegalArgumentException("unsupported type bound " + type.toString());
  }

  /**
   * Returns the runtime class for this type bound.
   * Depending on implementing class, this may be {@code Object} or something
   * closer to the bound.
   * The returned value should not be used to test satisfiability of the bound.
   *
   * @return the runtime class for this type bound.
   */
  public Class<?> getRuntimeClass() {
    return null;
  }
}
