package randoop.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** Deterministic versions of Class methods */
public class ClassUtil {

  private ClassUtil() {
    throw new Error("do not instantiate");
  }

  /**
   * Like {@link Class#getAnnotations()}, but returns the methods in deterministic order.
   *
   * @param c the Class whose annotations to return
   * @return the class's annotations
   */
  public static Annotation[] getAnnotations(Class<?> c) {
    Annotation[] result = c.getAnnotations();
    Arrays.sort(result, annotationComparator);
    return result;
  }
  /**
   * Like {@link Class#getDeclaredAnnotations()}, but returns the methods in deterministic order.
   *
   * @param c the Class whose declared annotations to return
   * @return the class's declared annotations
   */
  public static Annotation[] getDeclaredAnnotations(Class<?> c) {
    Annotation[] result = c.getDeclaredAnnotations();
    Arrays.sort(result, annotationComparator);
    return result;
  }
  /**
   * Like {@link Class#getClasses()}, but returns the classes in deterministic order.
   *
   * @param c the Class whose member classes to return
   * @return the class's member classes
   */
  public static Class<?>[] getClasses(Class<?> c) {
    Class<?>[] result = c.getClasses();
    Arrays.sort(result, classComparator);
    return result;
  }
  /**
   * Like {@link Class#getDeclaredClasses()}, but returns the classes in deterministic order.
   *
   * @param c the Class whose declared member classes to return
   * @return the class's declared member classes
   */
  public static Class<?>[] getDeclaredClasses(Class<?> c) {
    Class<?>[] result = c.getDeclaredClasses();
    Arrays.sort(result, classComparator);
    return result;
  }
  /**
   * Like {@link Class#getEnumConstants()}, but returns the methods in deterministic order.
   *
   * @param <T> the class's type parameter
   * @param c the Class whose enum constants to return
   * @return the class's enum constants
   */
  public static <T> T[] getEnumConstants(Class<T> c) {
    T[] result = c.getEnumConstants();
    Arrays.sort(result, toStringComparator);
    return result;
  }
  /**
   * Like {@link Class#getConstructors()}, but returns the methods in deterministic order.
   *
   * @param c the Class whose constructors to return
   * @return the class's constructors
   */
  public static Constructor<?>[] getConstructors(Class<?> c) {
    Constructor<?>[] result = c.getConstructors();
    Arrays.sort(result, constructorComparator);
    return result;
  }
  /**
   * Like {@link Class#getDeclaredConstructors()}, but returns the methods in deterministic order.
   *
   * @param c the Class whose declared constructors to return
   * @return the class's declared constructors
   */
  public static Constructor<?>[] getDeclaredConstructors(Class<?> c) {
    Constructor<?>[] result = c.getDeclaredConstructors();
    Arrays.sort(result, constructorComparator);
    return result;
  }
  /**
   * Like {@link Class#getFields()}, but returns the methods in deterministic order.
   *
   * @param c the Class whose fields to return
   * @return the class's fields
   */
  public static Field[] getFields(Class<?> c) {
    Field[] result = c.getFields();
    Arrays.sort(result, fieldComparator);
    return result;
  }
  /**
   * Like {@link Class#getDeclaredFields()}, but returns the methods in deterministic order.
   *
   * @param c the Class whose declared fields to return
   * @return the class's declared fields
   */
  public static Field[] getDeclaredFields(Class<?> c) {
    Field[] result = c.getDeclaredFields();
    Arrays.sort(result, fieldComparator);
    return result;
  }
  /**
   * Like {@link Class#getMethods()}, but returns the methods in deterministic order.
   *
   * @param c the Class whose methods to return
   * @return the class's methods
   */
  public static Method[] getMethods(Class<?> c) {
    Method[] result = c.getMethods();
    Arrays.sort(result, methodComparator);
    return result;
  }
  /**
   * Like {@link Class#getDeclaredMethods()}, but returns the methods in deterministic order.
   *
   * @param c the Class whose declared methods to return
   * @return the class's declared methods
   */
  public static Method[] getDeclaredMethods(Class<?> c) {
    Method[] result = c.getDeclaredMethods();
    Arrays.sort(result, methodComparator);
    return result;
  }

  /**
   * Creates a sorted list from an array of elements using the given classComparator.
   *
   * @param array the array of elements to be sorted
   * @param comparator the classComparator over the element type
   * @param <T> the element type
   * @return the sorted list of elements of the given array
   */
  private <T> List<T> toSortedList(T[] array, Comparator<T> comparator) {
    List<T> list = new ArrayList<>();
    Collections.addAll(list, array);
    Collections.sort(list, comparator);
    return list;
  }

  static AnnotationComparator annotationComparator = new AnnotationComparator();

  /** Compares Annotation objects by type name. */
  private static class AnnotationComparator implements Comparator<Annotation> {

    @Override
    public int compare(Annotation a1, Annotation a2) {
      return classComparator.compare(a1.annotationType(), a2.annotationType());
    }
  }

  static ClassComparator classComparator = new ClassComparator();

  /** Compares Class objects by name. */
  private static class ClassComparator implements Comparator<Class<?>> {

    @Override
    public int compare(Class<?> c1, Class<?> c2) {
      return c1.getName().compareTo(c2.getName());
    }
  }

  static MethodComparator methodComparator = new MethodComparator();

  /**
   * Compares Method objcets by signature: compares name, number of parameters, and parameter type
   * names.
   */
  private static class MethodComparator implements Comparator<Method> {

    @Override
    public int compare(Method m1, Method m2) {
      int result = classComparator.compare(m1.getDeclaringClass(), m2.getDeclaringClass());
      if (result != 0) {
        return result;
      }
      result = m1.getName().compareTo(m2.getName());
      if (result != 0) {
        return result;
      }
      result = m1.getParameterTypes().length - m2.getParameterTypes().length;
      if (result != 0) {
        return result;
      }
      for (int i = 0; i < m1.getParameterTypes().length; i++) {
        result = classComparator.compare(m1.getParameterTypes()[i], m2.getParameterTypes()[i]);
        if (result != 0) {
          return result;
        }
      }
      return result;
    }
  }

  static ConstructorComparator constructorComparator = new ConstructorComparator();

  /**
   * Compares Constructor objects by signature: compares name, number of parameters, and parameter
   * type names.
   */
  private static class ConstructorComparator implements Comparator<Constructor<?>> {

    @Override
    public int compare(Constructor<?> c1, Constructor<?> c2) {
      int result = classComparator.compare(c1.getDeclaringClass(), c2.getDeclaringClass());
      if (result != 0) {
        return result;
      }
      result = c1.getParameterTypes().length - c2.getParameterTypes().length;
      if (result != 0) {
        return result;
      }
      for (int i = 0; i < c1.getParameterTypes().length; i++) {
        result = classComparator.compare(c1.getParameterTypes()[i], c2.getParameterTypes()[i]);
        if (result != 0) {
          return result;
        }
      }
      return result;
    }
  }

  static FieldComparator fieldComparator = new FieldComparator();

  /** Compares Field objects by name. */
  private static class FieldComparator implements Comparator<Field> {

    @Override
    public int compare(Field f1, Field f2) {
      int result = classComparator.compare(f1.getDeclaringClass(), f2.getDeclaringClass());
      if (result != 0) {
        return result;
      }
      return f1.getName().compareTo(f2.getName());
    }
  }

  static ToStringComparator toStringComparator = new ToStringComparator();

  /** Compares objects by the result of toString(). */
  private static class ToStringComparator implements Comparator<Object> {

    @Override
    public int compare(Object o1, Object o2) {
      return o1.toString().compareTo(o2.toString());
    }
  }
}
