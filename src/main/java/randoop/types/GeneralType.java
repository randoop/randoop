package randoop.types;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;

import randoop.reflection.ReflectionPredicate;

/**
 * Common superclass representing types in Randoop.
 * Intended as glue for dealing with generating objects from
 * {@code java.lang.reflect.Type} objects, especially instances of
 * {@code java.lang.reflect.ParameterizedType} that can either represent
 * generic classes or a parameterized type.
 * <p>
 * This class is package private because it should not be necessary to use the
 * class directly within Randoop.
 * The intent is that Randoop will only use the subclasses {@link ConcreteType}
 * and {@link GenericType}.
 * Instances of {@code GenericType} are only be used during harvesting of
 * classes under test using reflection, then these are instantiated to instances
 * of {@link ConcreteType} that are then used to generate the tests.
 *
 * @see randoop.types.ConcreteType
 * @see randoop.types.GenericType
 */
public abstract class GeneralType {

  /** The predicate to decide whether to include members in a type */
  protected static ReflectionPredicate predicate;

  /**
   * Returns the runtime {@code Class} object for this type.
   * For use when reflection is needed.
   *
   * @return the {@link Class} that is the runtime representation of the type
   */
  public Class<?> getRuntimeClass() {
    return null;
  }

  /**
   * Indicates whether the given {@code Class} object corresponds to the runtime
   * class of this type.
   *
   * @param c  the class to check
   * @return true if {@code c} is the raw type of this type, false otherwise
   */
  public boolean hasRuntimeClass(Class<?> c) {
    return this.getRuntimeClass().equals(c);
  }

  /**
   * Returns the fully-qualified name of the type, including type arguments if
   * this is a parameterized type.
   *
   * @return the fully-qualified type name for this type
   */
  public String getName() {
    return null;
  }

  /**
   * Indicates whether this object represents an array type.
   *
   * @return true if this object represents an array type, false otherwise
   */
  public boolean isArray() {
    return false;
  }

  /**
   * Indicate whether this type is generic.
   * If not, then type is concrete.
   *
   * @return true if this type is generic, false otherwise
   */
  public boolean isGeneric() {
    return false;
  }

  /**
   * Indicate whether this is the {@code Object} type.
   *
   * @return true if this is the {@code Object} type, false otherwise
   */
  public boolean isObject() {
    return this.getRuntimeClass().equals(Object.class);
  }

  /**
   * Test whether this type is a subtype of the given type according to
   * transitive closure of definition of the <i>direct supertype</i> relation in
   * <a href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-4.html#jls-4.10.2">
   * section 4.10.2 of JLS for JavaSE 8</a>.
   * <i>Only</i> checks reference types.
   * @see ConcreteType#isAssignableFrom(ConcreteType)
   * @see ParameterizedType#isSubtypeOf(ConcreteType)
   *
   * @param type  the possible supertype
   * @return true if this type is a subtype of the given type, false otherwise
   */
  public boolean isSubtypeOf(ConcreteType type) {
    if (type == null) {
      throw new IllegalArgumentException("type may not be null");
    }

    // Object is *the* supertype
    if (type.isObject()) {
      return true;
    }

    // minimally, underlying Class should be assignable
    Class<?> otherRuntimeType = type.getRuntimeClass();
    Class<?> thisRuntimeType = this.getRuntimeClass();
    if (!otherRuntimeType.isAssignableFrom(thisRuntimeType)) {
      return false;
    }

    // if other type is an interface, check interfaces first
    if (otherRuntimeType.isInterface()) {
      Type[] interfaces = thisRuntimeType.getGenericInterfaces();
      for (Type t : interfaces) {
        if (type.equals(GeneralType.forType(t))) {
          return true; // found the type
        }
      }
    }

    // otherwise, get superclass
    Type superclass = thisRuntimeType.getGenericSuperclass();
    if (superclass != null) {
      GeneralType superType = GeneralType.forType(superclass);
      if (type.equals(superType)) { // found the type
        return true;
      }

      // no match yet, so check for transitive chain
      return superType.isSubtypeOf(type);
    }

    return false;
  }

  /**
   * Returns a type constructed from an object hiding behind a
   * {@code java.lang.reflect.Type} reference.
   * If the object is a {@code Class} instance then returns the
   * corresponding {@code ConcreteType}. If the type is actually a
   * {@code java.lang.reflect.ParameterizedType}, then the type arguments are
   * inspected to decide whether to return a {@code ParameterizedType} or a
   * {@code GenericClassType}.
   * If the type is a {@code java.lang.reflect.GenericArrayType}, then the
   * corresponding {@code GenericArrayType} is returned.
   * <p>
   * Note that when the type corresponds to a generic class type, this method
   * returns the type variables from the
   * {@link java.lang.reflect.ParameterizedType#getActualTypeArguments() getActualTypeArguments()}
   * method to maintain the guarantees needed for
   * {@link ParameterizedType#isSubtypeOf(ConcreteType)}.
   *
   * @param type  the type to interpret
   * @return a {@code randoop.types.Type} object corresponding to the given type
   * @throws IllegalArgumentException if the rawtype is not a Class instance
   */
  public static GeneralType forType(Type type) {

    if (type instanceof java.lang.reflect.GenericArrayType) {
      return forGenericArrayType((java.lang.reflect.GenericArrayType) type);
    }

    if (type instanceof java.lang.reflect.ParameterizedType) {
      java.lang.reflect.ParameterizedType t = (java.lang.reflect.ParameterizedType) type;
      return forParameterizedType(t);
    }

    if (type instanceof TypeVariable) {
      return new GenericSimpleType((TypeVariable<?>) type);
    }

    if (type instanceof Class<?>) {
      return ConcreteType.forClass((Class<?>) type);
    }

    String msg =
        "Unrecognized type "
            + type.toString()
            + ". Must be parameterized type, generic type, or non-generic";
    throw new IllegalArgumentException(msg);
  }

  /**
   * Performs the conversion of {@code java.lang.reflectGenericArrayType} to
   * a {@code GenericArrayType} or a {@code ConcreteArrayType} depending on
   * whether the component type is generic or concrete.
   *
   * @param type  the {@link java.lang.reflect.GenericArrayType} reference
   * @return the {@code GeneralType} for the array type
   */
  private static GeneralType forGenericArrayType(java.lang.reflect.GenericArrayType type) {
    GeneralType elementType = GeneralType.forType(type.getGenericComponentType());
    if (elementType.isGeneric()) {
      return new GenericArrayType((GenericType) elementType);
    } else {
      return new ConcreteArrayType((ConcreteType) elementType);
    }
  }

  /**
   * Performs the conversion of {@code java.lang.reflect.ParameterizedType} to
   * either {@code GenericClassType} or {@code ParameterizedType} depending on
   * type arguments of the referenced object.
   *
   * @param t  the reflective type object
   * @return an object of type {@code GenericClassType} if arguments are type
   *         variables, or of type {@code ParameterizedType} if the arguments
   *         are concrete
   */
  private static GeneralType forParameterizedType(java.lang.reflect.ParameterizedType t) {
    Type rawType = t.getRawType();
    assert !(rawType instanceof Class<?>);

    // Collect whatever is lurking in the "actual type arguments"
    // Could be *actual* "actual type arguments", or type variables
    ConcreteType[] typeArguments = new ConcreteType[t.getActualTypeArguments().length];
    List<TypeVariable<?>> typeParameters = new ArrayList<>(); //see below
    List<TypeBound> typeBounds = new ArrayList<>();
    Type[] actualArguments = t.getActualTypeArguments();
    for (int i = 0; i < actualArguments.length; i++) {
      if (actualArguments[i] instanceof TypeVariable) {
        TypeVariable<?> v = (TypeVariable<?>) actualArguments[i];
        typeParameters.add(v);
        typeBounds.add(TypeBound.fromTypes(v.getBounds()));
      } else if (actualArguments[i] instanceof Class) {
        typeArguments[i] =
            ConcreteType.forClass((Class<?>) actualArguments[i], new ConcreteType[0]);
      } else {
        String msg = "Expecting either type or type variable, got " + actualArguments[i].toString();
        throw new IllegalArgumentException(msg);
      }
    }

    // Now decide whether object is generic or parameterized type
    if (typeParameters.size() == actualArguments.length) { // is generic
      // When building generic class type, need to use the TypeVariables
      // obtained through the java.lang.reflect.ParameterizedType as above.
      // Otherwise, the variables mapped by the substitutions used in checking
      // subtyping will not be the correct objects, and the subtype test will
      // fail.
      return new GenericClassType((Class<?>) rawType, typeParameters, typeBounds);
    } else if (typeParameters.isEmpty()) { // is parameterized type
      // When building parameterized type, first create generic class from the
      // rawtype, and then instantiate with the arguments collected from the
      // java.lang.reflect.ParameterizedType interface.
      GenericClassType genericClass = (GenericClassType) GenericType.forClass((Class<?>) rawType);
      return genericClass.instantiate(typeArguments);
    } else {
      String msg = "Expecting either all concrete types or all type variables";
      throw new IllegalArgumentException(msg);
    }
  }

  /**
   * Returns a {@code GeneralType} object for the given type name.
   * Uses reflection to find the correspond type.
   *
   * @param typeName  the name of a type
   * @return the type object for the type with the name, null if none is found
   * @throws ClassNotFoundException if name is not a recognized type
   */
  public static GeneralType forName(String typeName) throws ClassNotFoundException {
    Class<?> c = PrimitiveTypes.getClassForName(typeName);
    if (c == null) {
      c = Class.forName(typeName);
    }
    if (c.getTypeParameters().length > 0) {
      return GenericType.forClass(c);
    }
    return ConcreteType.forClass(c);
  }

  /**
   * Returns a concrete type for this general type created by instantiating
   * the type parameters with a list of concrete type arguments.
   *
   * @param substitution  the type substitution
   * @return a {@code ConcreteType} constructed by substituting for type
   * parameters in this generic type
   */
  public abstract GeneralType apply(Substitution substitution);

  /**
   * Returns the package of this types runtime class.
   *
   * @return the package of the runtime class of this type
   */
  public Package getPackage() {
    Class<?> c = getRuntimeClass();
    if (c != null) {
      return c.getPackage();
    }
    return null;
  }
}
