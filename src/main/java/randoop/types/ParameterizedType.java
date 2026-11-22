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

  /** A cache of all ParameterizedTypes that have been created. */
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
  public static synchronized GenericClassType forClass(Class<?> typeClass) {
    // This method is synchronized because the cache can contain a GenericClassType whose type
    // parameters have not yet been computed.  The lock is reentrant, so the initializing thread can
    // look up the class during initialization, but another thread waits until it is complete.
    if (typeClass.getTypeParameters().length == 0) {
      throw new IllegalArgumentException(
          "class must be a generic type, have " + typeClass.getName());
    }
    // This cannot be
    //   return cache.computeIfAbsent(typeClass, GenericClassType::new);
    // because of a recursive call that might side-effect `cache`.

    GenericClassType cached = cache.get(typeClass);
    if (cached == null) {
      // Cache the generic class type before computing its type parameters.  A bound of a type
      // parameter may refer to the class itself, as in `C<T extends List<? extends C<?>[]>>`.
      // Converting such a bound calls this method again for the class, which must not recurse.
      cached = GenericClassType.withoutParameters(typeClass);
      cache.put(typeClass, cached);
      try {
        cached.initializeParameters();
      } catch (Throwable e) {
        cache.remove(typeClass);
        throw e;
      }
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
    return new InstantiatedType(genericClass, typeArguments);
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
