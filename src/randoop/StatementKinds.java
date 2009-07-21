package randoop;

public class StatementKinds {

  /**
   * Parses a string representing a StatementKind. The string is expected to be
   * of the form:
   * 
   * ID : DESCRIPTION
   * 
   * Where ID is a string identifying the type of StatementKind, and DESCRIPTION
   * represents more specifics of the StatementKind. For example, the following
   * String represents the constructor for HashMap:
   * 
   * cons : java.util.HashMap.<init>()
   * 
   * A class implementing StatementKind should define a static field named ID
   * that corresponds to the ID string used when parsing. The way this parse
   * method works is by using the ID string to determine the specific
   * StatementKind class C, and the calling C.parse(String) on the DESCRIPTION
   * String.
   * 
   * For more details on the exact form of DESCRIPTION, see the different
   * classes implementing StatmentKind.
   */
  public static StatementKind parse(String str) {
    if (str == null || str.length() == 0)
      throw new IllegalArgumentException("invalid string: " + str);


    // <id> : <description>
    int colonIdx = str.indexOf(':');
    String id = str.substring(0, colonIdx).trim();
    String descr = str.substring(colonIdx + 1).trim();

    // Call appropriate parsing method.
    if (id.equals(PrimitiveOrStringOrNullDecl.ID)) {
      return PrimitiveOrStringOrNullDecl.parse(descr);
    } else if (id.equals(RMethod.ID)) {
      return RMethod.parse(descr);
    } else if (id.equals(RConstructor.ID)) {
      return RConstructor.parse(descr);
    } else if (id.equals(ArrayDeclaration.ID)) {
      return ArrayDeclaration.parse(descr);
    } else if (id.equals(DummyStatement.ID)) {
      return DummyStatement.parse(descr);
    } else {
      throw new Error();
    }
  }

  public static String getId(StatementKind st) {
    if (st == null) throw new IllegalArgumentException("st cannot be null.");
    if (st instanceof PrimitiveOrStringOrNullDecl)
      return PrimitiveOrStringOrNullDecl.ID;
    if (st instanceof RMethod)
      return RMethod.ID;
    if (st instanceof RConstructor)
      return RConstructor.ID;
    if (st instanceof ArrayDeclaration)
      return ArrayDeclaration.ID;
    if (st instanceof DummyStatement)
      return DummyStatement.ID;
    throw new Error();
  }

}
