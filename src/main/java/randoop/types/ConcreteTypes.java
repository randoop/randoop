package randoop.types;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Constants for concrete Java types.
 */
public class ConcreteTypes {
  /** The boolean {@link PrimitiveType} */
  public static final PrimitiveType BOOLEAN_TYPE = new PrimitiveType(boolean.class);

  /** The byte {@link PrimitiveType} */
  public static final PrimitiveType BYTE_TYPE = new PrimitiveType(byte.class);

  /** The char {@link PrimitiveType} */
  public static final PrimitiveType CHAR_TYPE = new PrimitiveType(char.class);

  /** The {@code Class<?>} {@link ClassOrInterfaceType} */
  public static final ClassOrInterfaceType CLASS_TYPE = new SimpleClassOrInterfaceType(Class.class);

  /** The double {@link PrimitiveType} */
  public static final PrimitiveType DOUBLE_TYPE = new PrimitiveType(double.class);

  /** The float {@link PrimitiveType} */
  public static final PrimitiveType FLOAT_TYPE = new PrimitiveType(float.class);

  /** The int {@link PrimitiveType} */
  public static final PrimitiveType INT_TYPE = new PrimitiveType(int.class);

  /** The long {@link PrimitiveType} */
  public static final PrimitiveType LONG_TYPE = new PrimitiveType(long.class);

  /** The {@code Object} {@link ClassOrInterfaceType} */
  public static final ClassOrInterfaceType OBJECT_TYPE =
      new SimpleClassOrInterfaceType(Object.class);

  /** The short {@link PrimitiveType} */
  public static final PrimitiveType SHORT_TYPE = new PrimitiveType(short.class);

  /** The {@code String} {@link ClassOrInterfaceType} */
  public static final ClassOrInterfaceType STRING_TYPE =
      new SimpleClassOrInterfaceType(String.class);

  /** The void {@link PrimitiveType} */
  public static final PrimitiveType VOID_TYPE = new PrimitiveType(void.class);

  /** The Cloneable {@link ClassOrInterfaceType} */
  public static final Type CLONEABLE_TYPE = new SimpleClassOrInterfaceType(Cloneable.class);

  /** The Serializable {@link ClassOrInterfaceType} */
  public static final Type SERIALIZABLE_TYPE = new SimpleClassOrInterfaceType(Serializable.class);

  /** The Null type is the lower bound of reference types and is only used in {@link randoop.types.CaptureTypeVariable} */
  public static final ReferenceType NULL_TYPE = NullReferenceType.getNullType();

  /** The {@code Comparable} {@link ClassOrInterfaceType} */
  public static final GenericClassType COMPARABLE_TYPE = new GenericClassType(Comparable.class);

  /** The list of primitive types */
  private static final List<PrimitiveType> PRIMITIVE_TYPES = new ArrayList<>();

  static {
    PRIMITIVE_TYPES.add(BOOLEAN_TYPE);
    PRIMITIVE_TYPES.add(BYTE_TYPE);
    PRIMITIVE_TYPES.add(CHAR_TYPE);
    PRIMITIVE_TYPES.add(DOUBLE_TYPE);
    PRIMITIVE_TYPES.add(FLOAT_TYPE);
    PRIMITIVE_TYPES.add(INT_TYPE);
    PRIMITIVE_TYPES.add(LONG_TYPE);
    PRIMITIVE_TYPES.add(SHORT_TYPE);
  }

  /**
   * Returns the list of (non-void) primitive types.
   *
   * @return  the list of (non-void) primitive types
   */
  public static List<PrimitiveType> getPrimitiveTypes() {
    return PRIMITIVE_TYPES;
  }
}
