package randoop.reflection.supertypetest;

/**
 * Supertype class for an enum with overloaded methods with different argument types to mess with
 * reflection.
 */
public interface TheSupertype {

  String alpha(Integer i);

  int alpha(String s);
}
