package randoop.main;

/**
 * WarnOnBadClassName is used to handle a bad class name error by printing a
 * warning to standard out, and is used to suppress an exception.
 */
public class WarnOnBadClassName implements ClassNameErrorHandler {

  @Override
  public void handle(String className) {
    System.out.format("Warning: no class found for type name \"%s\"%n", className);
  }

}
