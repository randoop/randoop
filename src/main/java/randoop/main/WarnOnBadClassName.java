package randoop.main;

/**
 * WarnOnBadClassName is used to handle a bad class name error by printing a warning to standard
 * out, and is used to suppress an exception.
 */
public class WarnOnBadClassName implements ClassNameErrorHandler {

  @Override
  public void handle(String className) {
    System.out.format("Warning: no class \"%s\" found on the classpath%n", className);
  }

  @Override
  public void handle(String classname, Throwable e) {
    System.out.format("Warning: loading class \"%s\" resulted in exception: %s%n", classname, e);
  }
}
