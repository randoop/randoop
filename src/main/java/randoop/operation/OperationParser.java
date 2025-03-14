package randoop.operation;

import java.util.List;

/**
 * Parser for text serialization (string representation) of {@link Operation}s. See {@link #parse}
 * for format details.
 *
 * @see randoop.sequence.Sequence#parse(List)
 */
public class OperationParser {

  private OperationParser() {
    throw new Error("Do not instantiate");
  }

  /**
   * Parses a string representing an {@link Operation}. The string is expected to be of the form:
   *
   * <pre>
   *   ID : DESCRIPTION
   * </pre>
   *
   * Where ID is a string identifying the type of Operation, and DESCRIPTION represents more
   * specifics of the Operation. For example, the following String represents the constructor for
   * HashMap:
   *
   * <pre>{@code
   * cons : java.util.HashMap.<init>()
   * }</pre>
   *
   * A class implementing Operation should define a static field named ID that corresponds to the ID
   * string used when parsing. The way this parse method works is by using the ID string to
   * determine the specific Operation class C, and the calling C.parse(String) on the DESCRIPTION
   * String.
   *
   * <p>For more details on the exact form of DESCRIPTION, see the different classes implementing
   * Operation.
   *
   * @param str the string to be parsed
   * @return the operation for the given string descriptor
   * @throws OperationParseException if the string does not have expected format
   */
  public static TypedOperation parse(String str) throws OperationParseException {
    if (str == null || str.length() == 0) {
      throw new IllegalArgumentException("invalid string: " + str);
    }

    // <id> : <description>
    int colonIdx = str.indexOf(':');
    if (colonIdx == -1) {
      String msg =
          "A statement description must be of the form "
              + "<id> : <description> but the statement \""
              + str
              + "\" does not have a valid form (no colon).";
      throw new OperationParseException(msg);
    }

    String id = str.substring(0, colonIdx).trim();
    String descr = str.substring(colonIdx + 1).trim();

    // Call appropriate parsing method.
    if (id.equals("NonreceiverTerm")) {
      return NonreceiverTerm.parse(descr);
    } else if (id.equals("MethodCall")) {
      return MethodCall.parse(descr);
    } else if (id.equals("ConstructorCall")) {
      return ConstructorCall.parse(descr);
    } else if (id.equals("InitializedArrayCreation")) {
      return InitializedArrayCreation.parse(descr);
    } else if (id.equals("EnumConstant")) {
      return EnumConstant.parse(descr);
    } else if (id.equals("FieldGet")) {
      return FieldGet.parse(descr);
    } else if (id.equals("FieldSet")) {
      return FieldSet.parse(descr);
    } else {
      throw new OperationParseException("Invalid id \"" + id + "\" in statement: " + str);
    }
  }
}
