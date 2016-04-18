package randoop.types;

/**
 * Represents a type bound on a type variable occurring as a type parameter in
 * a class, interface, method or constructor. (See JLS section 8.1.2)
 * In Java, a type bound is either a type variable, a class type, an interface
 * type, or an intersection type of class and interface bounds.
 * This class represents a bound as concretely as possible based on the values
 * returned by {@link java.lang.reflect.TypeVariable#getBounds()}.
 * @see ClassOrInterfaceBound
 * @see TypeVariableBound
 * @see GenericTypeBound
 * @see IntersectionTypeBound
 */
public class ParameterBound {
}
