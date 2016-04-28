package randoop.types;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;

import plume.UtilMDE;

/**
 * Represents a parameterized type as a generic class instantiated with
 * concrete type arguments.
 * <p>
 * Note that {@link java.lang.reflect.ParameterizedType} is an interface that
 * can represent either a parameterized type in the sense meant here, or a
 * generic class.
 * Conversion to this type from this and other {@link java.lang.reflect.Type}
 * interfaces is handled by
 * {@link randoop.types.GeneralType#forType(java.lang.reflect.Type)}.
 */
public abstract class ParameterizedType extends ClassOrInterfaceType {

  /**
   * {@inheritDoc}
   * Tests for assignability to a parameterized type: can the given concrete
   * type be assigned to this parameterized type.
   * <p>
   * A {@code ConcreteType} is assignable to a parameterized type via
   * identity, widening reference conversion, or unchecked conversion from raw
   * type.
   * The method {@link Class#isAssignableFrom(Class)} checks widening reference
   * conversion, but with parameterized types it is also necessary to check type
   * arguments.  Since a widening reference conversion (JLS, section 5.1.5)
   * exists from type S to type T if S is a subtype of T, this method calls
   * {@link ParameterizedType#isSubtypeOf(GeneralType)} to test the subtype
   * relation.
   */
  @Override
  public boolean isAssignableFrom(GeneralType sourceType) {
    if (sourceType == null) {
      throw new IllegalArgumentException("source type must be non-null");
    }

    // do a couple of quick checks for things that don't work
    // first, can't assign an array to a parameterized type
    if (sourceType.isArray()) {
      return false;
    }

    // second, if underlying Class objects not assignable then there is
    // definitely no widening conversion
    if (!this.getRuntimeClass().isAssignableFrom(sourceType.getRuntimeClass())) {
      return false;
    }

    // Now, check subtype relation
    try {
      if (sourceType.isSubtypeOf(this)) {
        return true;
      }
    } catch (RandoopTypeException e) {
      // this is a bit dangerous, but I'm doing it anyway
      return false;
    }

    // otherwise, test unchecked
    return sourceType.isRawtype() && sourceType.hasRuntimeClass(this.getRuntimeClass());

  }

  /**
   * {@inheritDoc}
   * @return true, since this is a parameterized type
   */
  @Override
  public boolean isParameterized() {
    return true;
  }

  /**
   * {@inheritDoc}
   * @return the name of this type
   */
  @Override
  public String toString() {
    return this.getName();
  }

  /**
   * {@inheritDoc}
   * @return the fully qualified name of this type with fully qualified type
   * arguments
   */
  @Override
  public String getName() {
    return getRuntimeClass().getCanonicalName()
        + "<"
        + UtilMDE.join(this.getTypeArguments(), ",")
        + ">";
  }

  @Override
  public abstract ParameterizedType apply(Substitution substitution);

  public abstract ParameterizedType instantiate(ReferenceType... typeArguments);

  /**
   * Returns the type arguments for this type.
   *
   * @return the list of type arguments
   */
  public abstract List<TypeArgument> getTypeArguments();

  /**
   * Checks whether this parameterized type is an instantiation of the given
   * generic class type.
   *
   * @param genericClassType  the generic class type
   * @return true if this type is an instantiation of the generic class, false otherwise
   */
  public abstract boolean isInstantiationOf(GenericClassType genericClassType);

  public static GenericClassType forClass(Class<?> typeClass) {
    if (typeClass.getTypeParameters().length == 0) {
      throw new IllegalArgumentException("class must be a generic type");
    }

    List<TypeVariable> argumentList = new ArrayList<>();
    for (java.lang.reflect.TypeVariable<?> v : typeClass.getTypeParameters()) {
      argumentList.add(TypeVariable.forType(v));
    }
    return new GenericClassType(typeClass, argumentList);
  }

  /**
   * Performs the conversion of {@code java.lang.reflect.ParameterizedType} to
   * either {@code GenericClassType} or {@code ParameterizedType} depending on
   * type arguments of the referenced object.
   *
   * @param type  the reflective type object
   * @return an object of type {@code GenericClassType} if arguments are type
   *         variables, or of type {@code ParameterizedType} if the arguments
   *         are concrete
   */
  public static ParameterizedType forType(Type type) {
    if (! (type instanceof java.lang.reflect.ParameterizedType)) {
      throw new IllegalArgumentException("type must be java.lang.reflect.ParameterizedType");
    }

    java.lang.reflect.ParameterizedType t = (java.lang.reflect.ParameterizedType)type;

    Type rawType = t.getRawType();
    assert (rawType instanceof Class<?>) : "rawtype not an instance of Class<?> type " ;

    List<TypeArgument> typeArguments = new ArrayList<>();
    List<TypeVariable> typeParameters = new ArrayList<>(); //see below

    for (Type argType : t.getActualTypeArguments()) {
      if (argType instanceof java.lang.reflect.TypeVariable) {
        java.lang.reflect.TypeVariable<?> v = (java.lang.reflect.TypeVariable<?>) argType;
        typeParameters.add(TypeVariable.forType(v));
      } else {
        typeArguments.add(TypeArgument.forType(argType));
      }
    }

    // Now decide whether object is generic or parameterized type
    if (typeParameters.size() == t.getActualTypeArguments().length) { // is generic
      // When building generic class type, need to use the TypeVariables
      // obtained through the java.lang.reflect.ParameterizedType as above.
      // Otherwise, the variables mapped by the substitutions used in checking
      // subtyping will not be the correct objects, and the subtype test will
      // fail.
      return new GenericClassType((Class<?>) rawType, typeParameters);
    } else if (typeParameters.isEmpty()) { // is parameterized type
      // When building parameterized type, first create generic class from the
      // rawtype, and then instantiate with the arguments collected from the
      // java.lang.reflect.ParameterizedType interface.
      GenericClassType genericClass = ParameterizedType.forClass((Class<?>) rawType);
      Substitution substitution = Substitution.forArgs(genericClass.getTypeArguments(), typeArguments);
      return new InstantiatedType(genericClass, substitution, typeArguments);
    } else {
      String msg = "Expecting either all concrete types or all type variables";
      throw new IllegalArgumentException(msg);
    }
  }


}
