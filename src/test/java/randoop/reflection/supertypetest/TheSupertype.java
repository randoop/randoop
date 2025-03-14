package randoop.reflection.supertypetest;

/**
 * Supertype class for an enum with overloaded method with different argument type to mess with
 * reflection.
 */
public interface TheSupertype {

  String alpha(Integer i);

  int alpha(String s);
}
