package randoop.types;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import org.plumelib.util.UtilPlume;

/**
 * Represents a parameterized type. A <i>parameterized type</i> is a type {@code C<T1,...,Tk>} where
 * {@code C<F1,...,Fk>} is a generic class instantiated by a substitution {@code [Fi:=Ti]}, and
 * {@code Ti} is a subtype of the upper bound {@code Bi} of the type parameter {@code Fi}.
 *
 * @see GenericClassType
 * @see InstantiatedType
 */
public abstract class ParameterizedType extends ClassOrInterfaceType {

  /**
   * Creates a {@link GenericClassType} for the given reflective {@link Class} object.
   *
   * @param typeClass the class type
   * @return a generic class type for the given type
   */
  public static GenericClassType forClass(Class<?> typeClass) {
    if (typeClass.getTypeParameters().length == 0) {
      throw new IllegalArgumentException(
          "class must be a generic type, have " + typeClass.getName());
    }
    return new GenericClassType(typeClass);
  }

  /**
   * Performs the conversion of {@code java.lang.reflect.ParameterizedType} to a {@code
   * ParameterizedType} .
   *
   * @param type the reflective type object
   * @return an object of type {@code ParameterizedType}
   */
  public static ParameterizedType forType(java.lang.reflect.Type type) {
    if (!(type instanceof java.lang.reflect.ParameterizedType)) {
      throw new IllegalArgumentException("type must be java.lang.reflect.ParameterizedType");
    }

    java.lang.reflect.ParameterizedType t = (java.lang.reflect.ParameterizedType) type;
    Type rawType = t.getRawType();
    assert (rawType instanceof Class<?>) : "rawtype not an instance of Class<?> type ";

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

  @Override
  public String toString() {
    return this.getName();
  }

  @Override
  public abstract ParameterizedType substitute(Substitution substitution);

  /**
   * Returns the {@link GenericClassType} for this parameterized type.
   *
   * @return the generic class type for this type
   */
  public abstract GenericClassType getGenericClassType();

  /**
   * {@inheritDoc}
   *
   * <p>Returns the fully-qualified name of this type with fully-qualified type arguments. E.g.,
   * {@code java.lang.List<java.lang.String>}
   */
  @Override
  public String getName() {
    return super.getName() + "<" + UtilPlume.join(this.getTypeArguments(), ",") + ">";
  }

  @Override
  public String getUnqualifiedName() {
    return this.getSimpleName() + "<" + UtilPlume.join(this.getTypeArguments(), ",") + ">";
  }
}
