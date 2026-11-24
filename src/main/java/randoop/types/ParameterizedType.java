package randoop.types;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.plumelib.util.CollectionsPlume;

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
   * A cache of all ParameterizedTypes that have been created. Accessed only while holding the lock
   * on {@code ParameterizedType.class}; see {@link #forClass}.
   */
  private static final Map<Class<?>, GenericClassType> cache = new HashMap<>();

  /** Creates a ParameterizedType. */
  public ParameterizedType() {
    super();
  }

  /**
   * Creates a {@link GenericClassType} for the given reflective {@link Class} object.
   *
   * @param typeClass the class type
   * @return a generic class type for the given type
   */
  // This method is synchronized so that concurrent calls do not corrupt the cache, and so that an
  // uninitialized generic class type in the cache is visible only to the thread that is
  // initializing it.  The lock is reentrant, so recursive calls from that thread find it.
  public static synchronized GenericClassType forClass(Class<?> typeClass) {
    if (typeClass.getTypeParameters().length == 0) {
      throw new IllegalArgumentException(
          "class must be a generic type, have " + typeClass.getName());
    }
    // This cannot be
    //   return cache.computeIfAbsent(typeClass, GenericClassType::new);
    // because of a recursive call that might side-effect `cache`.

    GenericClassType cached = cache.get(typeClass);
    if (cached == null) {
      cached = new GenericClassType(typeClass);
      cache.put(typeClass, cached);
    }
    return cached;
  }

  /**
   * Performs the conversion of {@code java.lang.reflect.ParameterizedType} to a {@code
   * ParameterizedType}.
   *
   * @param type the reflective type object
   * @return an object of type {@code ParameterizedType}
   */
  public static ParameterizedType forType(Type type) {
    if (!(type instanceof java.lang.reflect.ParameterizedType)) {
      throw new IllegalArgumentException("type must be java.lang.reflect.ParameterizedType");
    }

    java.lang.reflect.ParameterizedType t = (java.lang.reflect.ParameterizedType) type;
    Type rawType = t.getRawType();
    assert (rawType instanceof Class<?>) : "rawtype not an instance of Class<?> type ";

    // Categorize the type arguments as either a type variable or other kind of argument
    List<TypeArgument> typeArguments =
        CollectionsPlume.mapList(TypeArgument::forType, t.getActualTypeArguments());

    // When building parameterized type, first create generic class from the
    // rawtype, and then instantiate with the arguments collected from the
    // java.lang.reflect.ParameterizedType interface.
    GenericClassType genericClass = forClass((Class<?>) rawType);
    InstantiatedType result = new InstantiatedType(genericClass, typeArguments);
    // For a member class, record the enclosing type, as GenericClassType.substitute does.
    // Otherwise, a reflected member type would not equal the same type constructed by
    // substitution.  For a member class such as Outer<String>.Middle<Integer>, keep the owner type
    // as written.
    java.lang.reflect.ParameterizedType ownerType = ClassOrInterfaceType.getPreservedOwnerType(t);
    Type owner = t.getOwnerType();
    if (ownerType != null) {
      result.enclosingType = ClassOrInterfaceType.forType(ownerType);
    } else if (owner instanceof java.lang.reflect.ParameterizedType
        && !java.lang.reflect.Modifier.isStatic(((Class<?>) rawType).getModifiers())) {
      // For a member class whose owner type is the type that the declaration of the enclosing
      // class denotes, such as Outer<E>.Inner<Integer> within Outer, use that declaration, as
      // ClassOrInterfaceType.forType does.  Its type parameters, such as E, are those of the type.
      Class<?> enclosingClass =
          (Class<?>) ((java.lang.reflect.ParameterizedType) owner).getRawType();
      result.enclosingType = ClassOrInterfaceType.forClass(enclosingClass);
    } else if (owner instanceof Class<?>
        && !java.lang.reflect.Modifier.isStatic(((Class<?>) rawType).getModifiers())) {
      // The owner type supplies no type arguments, as for NonGenericOuter.Base<String> or
      // Plain.Inner<String>.  Set the enclosing type as GenericClassType#substitute does for a
      // member class, so that the two representations of the type are equal.
      result.enclosingType = ClassOrInterfaceType.forClass((Class<?>) owner);
    }
    return result;
  }

  @Override
  public abstract ParameterizedType substitute(Substitution substitution);

  /**
   * Returns the {@link GenericClassType} for this parameterized type.
   *
   * @return the generic class type for this type
   */
  public abstract GenericClassType getGenericClassType();

  @Override
  public String getFqName() {
    return super.getFqName()
        + "<"
        + getTypeArguments().stream().map(TypeArgument::getFqName).collect(Collectors.joining(","))
        + ">";
  }

  @Override
  public String getBinaryName() {
    return super.getBinaryName()
        + "<"
        + getTypeArguments().stream()
            .map(TypeArgument::getBinaryName)
            .collect(Collectors.joining(","))
        + ">";
  }

  @Override
  public String getUnqualifiedBinaryName() {
    return super.getUnqualifiedBinaryName()
        + "<"
        + getTypeArguments().stream()
            .map(TypeArgument::getBinaryName)
            .collect(Collectors.joining(","))
        + ">";
  }
}
