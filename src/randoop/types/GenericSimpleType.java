package randoop.types;

import java.lang.reflect.TypeVariable;

/** 
 * Represents a type variable used by itself as a type.
 * Could occur as a return type, a method/constructor parameter type, a field
 * type, or the type of an array.
 */
public class GenericSimpleType extends GenericType {

  /** the type parameter of the simple type */
  private TypeVariable<?> parameter;
  
  /** the (upper) bound on the type parameter */
  private TypeBound bound;

  /**
   * Create a {@code GenericSimpleType} for the given type parameter.
   * 
   * @param parameter  the type parameter
   */
  public GenericSimpleType(TypeVariable<?> parameter) {
    this.parameter = parameter;
    this.bound = TypeBound.fromTypes(parameter.getBounds());
  }

  @Override
  public Class<?> getRuntimeClass() {
    return bound.getRuntimeClass();
  }
  
  
  @Override
  public String getName() {
    return parameter.getName();
  }
  
  
}
