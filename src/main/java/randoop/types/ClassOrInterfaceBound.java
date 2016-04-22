package randoop.types;

import java.lang.reflect.*;
import java.lang.reflect.TypeVariable;
import java.util.Set;

/**
 * Represents a parameter bound which is a class or interface type.
 *
 * @see ClassOrInterfaceTypeBound
 * @see GenericTypeBound
 */
public abstract class ClassOrInterfaceBound extends ParameterBound {

  /**
   * Create a {@link ParameterBound} for the given {@code java.lang.reflect.Type} that is defined
   * as a class or interface.
   * If the type is a {@code java.lang.reflect.ParameterizedType} with a type variable, then bound
   * may be recursive, and so returns a {@link GenericTypeBound}.
   * Otherwise, returns a {@link ClassOrInterfaceTypeBound}.
   *
   * @param type  the {@code Type} bound
   * @return a {@code ClassOrInterfaceBound} for the given type
   */
  public static ClassOrInterfaceBound forType(Type type) {

    if (type instanceof java.lang.reflect.ParameterizedType) {
      java.lang.reflect.ParameterizedType pt = (java.lang.reflect.ParameterizedType) type;

      for (Type argType : pt.getActualTypeArguments()) {
        if (argType instanceof java.lang.reflect.TypeVariable<?>) {
          return GenericTypeBound.fromType(type);
        }
      }

      return new ClassOrInterfaceTypeBound(ParameterizedType.forType(type));
    }

    if (type instanceof Class<?>) {
      return ClassOrInterfaceTypeBound.forType(type);
    }

    throw new IllegalArgumentException("Bounds may only be class or interface types");
  }
}
