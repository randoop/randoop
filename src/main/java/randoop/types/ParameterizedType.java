package randoop.types;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import plume.UtilMDE;

/**
 * Represents a parameterized type.
 * A <i>parameterized type</i> is a type <code>C&lt;T<sub>1</sub>,&hellip;,T<sub>k</sub>&gt;</code>
 * where <code>C&lt;F<sub>1</sub>,&hellip;,F<sub>k</sub>&gt;</code> is a generic class
 * instantiated by a substitution <code>[F<sub>i</sub>:=T<sub>i</sub>]</code>, and
 * <code>T<sub>i</sub></code> is a subtype of the upper bound <code>B<sub>i</sub></code> of
 * the type parameter <code>F<sub>i</sub></code>.
 *
 * @see GenericClassType
 * @see InstantiatedType
 */
public abstract class ParameterizedType extends ClassOrInterfaceType {

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
  public abstract ParameterizedType apply(Substitution<ReferenceType> substitution);

  /**
   * Returns the type arguments for this type.
   *
   * @return the list of type arguments
   */
  public abstract List<TypeArgument> getTypeArguments();

  /**
   * Creates a {@link GenericClassType} for the given reflective {@link Class} object.
   *
   * @param typeClass  the class type
   * @return  a generic class type for the given type
   */
  public static GenericClassType forClass(Class<?> typeClass) {
    if (typeClass.getTypeParameters().length == 0) {
      throw new IllegalArgumentException("class must be a generic type");
    }
    return new GenericClassType(typeClass);
  }

  /**
   * Performs the conversion of {@code java.lang.reflect.ParameterizedType} to
   * a {@code ParameterizedType} .
   *
   * @param type  the reflective type object
   * @return an object of type {@code ParameterizedType}
   */
  public static ParameterizedType forType(Type type) {
    if (! (type instanceof java.lang.reflect.ParameterizedType)) {
      throw new IllegalArgumentException("type must be java.lang.reflect.ParameterizedType");
    }

    java.lang.reflect.ParameterizedType t = (java.lang.reflect.ParameterizedType)type;

    Type rawType = t.getRawType();
    assert (rawType instanceof Class<?>) : "rawtype not an instance of Class<?> type " ;

    // Categorize the type arguments as either a type variable or other kind of argument
    List<TypeArgument> typeArguments = new ArrayList<>();
    for (Type argType : t.getActualTypeArguments()) {
      TypeArgument argument = TypeArgument.forType(argType);
      typeArguments.add(argument);
    }

    // When building parameterized type, first create generic class from the
    // rawtype, and then instantiate with the arguments collected from the
    // java.lang.reflect.ParameterizedType interface.
    GenericClassType genericClass = ParameterizedType.forClass((Class<?>) rawType);
    return new InstantiatedType(genericClass, typeArguments);
  }

  /**
   * Returns the {@link GenericClassType} for this parameterized type.
   *
   * @return the generic class type for this type
   */
  public abstract GenericClassType getGenericClassType();
}
