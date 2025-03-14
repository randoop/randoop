package instrument.testcase;

public class ImplementorOfTarget implements InterfaceTarget {

  @Override
  public String glue(String s, String t) {
    return s + t;
  }
}
