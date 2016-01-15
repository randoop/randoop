package randoop.types;

import java.lang.reflect.Array;

public class GenericArrayType extends GenericType {

  /** The (generic) element type of this array type */
  private GenericType elementType;
  
  /** The runtime type of this array type */
  private Class<?> runtimeType;

  /**
   * 
   * @param t
   */
  public GenericArrayType(java.lang.reflect.GenericArrayType t) {
    this(convertType(t));  
  }
  
  /**
   * Converts the element type of the given 
   * {@code java.lang.reflect.GenericArrayType}
   * to a {@code GenericType} object.
   *  
   * @param t  the array type to convert
   * @return the converted element type of generic array type 
   */
  private static GenericType convertType(java.lang.reflect.GenericArrayType t) {
    if (t == null) {
      throw new IllegalArgumentException("type must be non-null");
    }
    GeneralType elementType = GeneralType.forType(t.getGenericComponentType());
    if (! elementType.isGeneric()) {
      String msg = "expecting generic element type (found: " 
                 + elementType.getName() + ")";
      throw new IllegalArgumentException(msg);
    }
    return (GenericType)elementType;
  }

  /**
   * Create a generic array type for the given element type.
   * 
   * @param elementType  the element type for the array
   */
  public GenericArrayType(GenericType elementType) {
    if (elementType == null) {
      throw new IllegalArgumentException("element type must be non-null");
    }
    
    this.elementType = elementType;
    this.runtimeType = Array.newInstance(elementType.getRuntimeClass(), 0).getClass();
  }
  
  @Override
  public Class<?> getRuntimeClass() {
    return runtimeType;
  }
}
