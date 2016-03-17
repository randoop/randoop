package randoop.operation;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class OperationParser {

  /**
   * Parses a string representing a StatementKind. The string is expected to be
   * of the form:
   *
   * <pre>
   *   ID : DESCRIPTION
   * </pre>
   *
   * Where ID is a string identifying the type of StatementKind, and DESCRIPTION
   * represents more specifics of the StatementKind. For example, the following
   * String represents the constructor for HashMap:
   *
   * <pre>
   *   cons : java.util.HashMap.&lt;init&gt;()
   * </pre>
   *
   * A class implementing StatementKind should define a static field named ID
   * that corresponds to the ID string used when parsing. The way this parse
   * method works is by using the ID string to determine the specific
   * StatementKind class C, and the calling C.parse(String) on the DESCRIPTION
   * String.
   * <p>
   * For more details on the exact form of DESCRIPTION, see the different
   * classes implementing StatementKind.
   *
   * @param str
   *          the string to be parsed.
   * @return the operation corresponding to the string.
   * @throws OperationParseException
   *           if the string does not have expected format.
   */
  public static ConcreteOperation parse(String str) throws OperationParseException {
    if (str == null || str.length() == 0)
      throw new IllegalArgumentException("invalid string: " + str);

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

    Set<String> validIds = new LinkedHashSet<String>();

    // If you add a statement kind, add its ID to this set.
    validIds.addAll(
        Arrays.asList(
            NonreceiverTerm.ID,
            MethodCall.ID,
            ConstructorCall.ID,
            ArrayCreation.ID,
            EnumConstant.ID,
            FieldGet.ID,
            FieldSet.ID));

    // Call appropriate parsing method.
    if (id.equals(NonreceiverTerm.ID)) {
      return NonreceiverTerm.parse(descr);
    } else if (id.equals(MethodCall.ID)) {
      return MethodCall.parse(descr);
    } else if (id.equals(ConstructorCall.ID)) {
      return ConstructorCall.parse(descr);
    } else if (id.equals(ArrayCreation.ID)) {
      return ArrayCreation.parse(descr);
    } else if (id.equals(EnumConstant.ID)) {
      return EnumConstant.parse(descr);
    } else if (id.equals(FieldGet.ID)) {
      return FieldGet.parse(descr);
    } else if (id.equals(FieldSet.ID)) {
      return FieldSet.parse(descr);
    } else {
      String msg =
          "A statement description must be of the form "
              + "<id> <description>"
              + " with <id> in "
              + validIds.toString()
              + " but the statement \""
              + str
              + "\" does not have a valid <id>.";
      throw new OperationParseException(msg);
    }
  }

  /**
   * Returns the "id" for the Operation. The ID is really the kind or a tag,
   * such as "prim". It is not a unique identifier for individual Operations.
   *
   * @param op
   *          the operation.
   * @return the ID string for the operation.
   */
  public static String getId(Operation op) {
    if (op == null) throw new IllegalArgumentException("st cannot be null.");
    if (op instanceof NonreceiverTerm) return NonreceiverTerm.ID;
    if (op instanceof MethodCall) return MethodCall.ID;
    if (op instanceof ConstructorCall) return ConstructorCall.ID;
    if (op instanceof ArrayCreation) return ArrayCreation.ID;
    if (op instanceof EnumConstant) return EnumConstant.ID;
    if (op instanceof FieldGet) return FieldGet.ID;
    if (op instanceof FieldSet) return FieldSet.ID;
    throw new Error();
  }
}
