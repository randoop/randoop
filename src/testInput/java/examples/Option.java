package examples;

/**
 * Input class for generation of assertions on enum types.
 * Related to issue #87.
 */
public class Option {

  public enum OptionType {
    SOME,
    NONE;
  }

  public static OptionType createOption(Object obj) {
    if (obj == null) {
      return OptionType.NONE;
    } else {
      return OptionType.SOME;
    }
  }
}
