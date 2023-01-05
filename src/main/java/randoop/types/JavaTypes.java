package randoop.types;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Constants for non-JDK Java types. Include primitive types, {@code Class<>}, {@code Object},
 * {@code Cloneable}, {@code Comparable}, {@code Serializable}, {@code String}, the null type, and
 * {@code void}.
 */
public class JavaTypes {
  /** The {@code boolean} type. */
  public static final PrimitiveType BOOLEAN_TYPE = PrimitiveType.forClass(boolean.class);

  /** The {@code byte} type. */
  public static final PrimitiveType BYTE_TYPE = PrimitiveType.forClass(byte.class);

  /** The {@code char} type. */
  public static final PrimitiveType CHAR_TYPE = PrimitiveType.forClass(char.class);

  /** The {@code double} type. */
  public static final PrimitiveType DOUBLE_TYPE = PrimitiveType.forClass(double.class);

  /** The {@code float} type. */
  public static final PrimitiveType FLOAT_TYPE = PrimitiveType.forClass(float.class);

  /** The {@code int} type. */
  public static final PrimitiveType INT_TYPE = PrimitiveType.forClass(int.class);

  /** The {@code long} type. */
  public static final PrimitiveType LONG_TYPE = PrimitiveType.forClass(long.class);

  /** The {@code short} type. */
  public static final PrimitiveType SHORT_TYPE = PrimitiveType.forClass(short.class);

  /** The {@code java.lang.Object} type. */
  public static final ClassOrInterfaceType OBJECT_TYPE =
      NonParameterizedType.forClass(Object.class);

  // This is used in CLASS_TYPE, so put it before CLASS_TYPE.
  // To ensure order, could put all initialization in a static initializer block.
  /**
   * The Null type is the lower bound of reference types and is only used in {@link
   * randoop.types.CaptureTypeVariable}
   */
  public static final ReferenceType NULL_TYPE = NullReferenceType.getNullType();

  /** The {@code java.lang.Class<?>} type. */
  public static final GenericClassType CLASS_TYPE = new GenericClassType(Class.class);

  /** The {@code java.lang.Comparable} type. */
  public static final GenericClassType COMPARABLE_TYPE = new GenericClassType(Comparable.class);

  /** The {@code java.lang.String} type. */
  public static final ClassOrInterfaceType STRING_TYPE =
      NonParameterizedType.forClass(String.class);

  /** The {@code java.util.Collection} type. */
  public static final ClassOrInterfaceType COLLECTION_TYPE =
      NonParameterizedType.forClass(Collection.class);

  /** The {@code java.lang.Cloneable} type. */
  public static final ReferenceType CLONEABLE_TYPE = NonParameterizedType.forClass(Cloneable.class);

  /** The {@code java.lang Serializable} type. */
  public static final ReferenceType SERIALIZABLE_TYPE =
      NonParameterizedType.forClass(Serializable.class);

  /** The void {@link VoidType} */
  public static final VoidType VOID_TYPE = VoidType.getVoidType();

  /** The list of primitive types. */
  private static final List<PrimitiveType> PRIMITIVE_TYPES;

  static {
    ArrayList<PrimitiveType> types = new ArrayList<>();
    types.add(BOOLEAN_TYPE);
    types.add(BYTE_TYPE);
    types.add(CHAR_TYPE);
    types.add(DOUBLE_TYPE);
    types.add(FLOAT_TYPE);
    types.add(INT_TYPE);
    types.add(LONG_TYPE);
    types.add(SHORT_TYPE);
    PRIMITIVE_TYPES = Collections.unmodifiableList(types);
  }

  /**
   * Returns the list of (non-void) primitive types.
   *
   * @return the list of (non-void) primitive types
   */
  public static List<PrimitiveType> getPrimitiveTypes() {
    return PRIMITIVE_TYPES;
  }
}
