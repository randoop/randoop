package randoop.reflection;

import java.util.ArrayList;
import java.util.List;
import randoop.Globals;
import randoop.operation.NonreceiverTerm;
import randoop.operation.OperationParseException;
import randoop.operation.TypedOperation;
import randoop.sequence.Sequence;
import randoop.sequence.Variable;
import randoop.types.ClassOrInterfaceType;
import randoop.util.MultiMap;
import randoop.util.RecordListReader;
import randoop.util.RecordProcessor;

/**
 * Method {@link #parse} reads a file specifying literal values to use during generation. The text
 * file should contain one or more records of the form:
 *
 * <pre>
 * START CLASSLITERALS
 * CLASSNAME
 * classname
 * LITERALS
 * type:value
 * ...
 * type:value
 * END CLASSLITERALS
 * </pre>
 *
 * Capitalized text must appear literally. Lowercase text is as follows:
 *
 * <ul>
 *   <li>classname is the name of a class in Class.getName format. More specifically,
 *       TypeNames.getTypeForName(classname) must return a valid Class object.
 *   <li>Each type:value pair describes the type and value of a literal (for example, {@code
 *       int:3}).
 * </ul>
 *
 * Blank lines and comment lines (lines starting with "#") are ignored, both between records and
 * inside records.
 *
 * <p>An example literals file appears in file randoop/systemtests/resources/literalsfile.txt.
 *
 * <p>LIMITATIONS:
 *
 * <p>Error messages do not include line numbers pointing to location of the error. There is no way
 * to specify literals that are not related to any class in particular, or literals that are related
 * to only specific methods within a class.
 */
public class LiteralFileReader {

  private LiteralFileReader() {
    throw new Error("Do not instantiate");
  }

  /**
   * Returns a map from class to list of constants.
   *
   * @param inFile the input file
   * @return the map from types to literal values
   */
  @SuppressWarnings("signature") // parsing
  public static MultiMap<ClassOrInterfaceType, Sequence> parse(String inFile) {

    final MultiMap<ClassOrInterfaceType, Sequence> map = new MultiMap<>();

    RecordProcessor processor =
        new RecordProcessor() {
          @Override
          public void processRecord(List<String> lines) {

            if (!(lines.size() >= 1 && lines.get(0).trim().toUpperCase().equals("CLASSNAME"))) {
              throwRecordSyntaxError("record does not begin with \"CLASSNAME\"", lines, 0);
            }

            if (!(lines.size() >= 2)) {
              throwRecordSyntaxError("class name missing", lines, 1);
            }

            Class<?> cls = null;
            try {
              cls = TypeNames.getTypeForName(lines.get(1));
            } catch (ClassNotFoundException e) {
              throwRecordSyntaxError(e);
            }
            assert cls != null;
            ClassOrInterfaceType classType = ClassOrInterfaceType.forClass(cls);

            if (!(lines.size() >= 3 && lines.get(2).trim().toUpperCase().equals("LITERALS"))) {
              throwRecordSyntaxError("Missing field \"LITERALS\"", lines, 2);
            }

            for (int i = 3; i < lines.size(); i++) {
              try {
                TypedOperation operation = NonreceiverTerm.parse(lines.get(i));
                map.add(classType, new Sequence().extend(operation, new ArrayList<Variable>()));
              } catch (OperationParseException e) {
                throwRecordSyntaxError(e);
              }
            }
          }
        };

    RecordListReader reader = new RecordListReader("CLASSLITERALS", processor);
    reader.parse(inFile);

    return map;
  }

  /** Throw an error with the given exception as its cause. */
  private static void throwRecordSyntaxError(Exception e) {
    throw new Error(e);
  }

  private static void throwRecordSyntaxError(String string, List<String> lines, int i) {
    StringBuilder b = new StringBuilder();
    b.append("RECORD PROCESSING ERROR: ").append(string).append(Globals.lineSep);
    appendRecord(b, lines, i);
    throw new Error(b.toString());
  }

  private static void appendRecord(StringBuilder b, List<String> lines, int i) {
    // This printout is less than ideal (it does not include the START/END
    // delimiters) and has no line number data, a limitation inherited from
    // RecordProcessor/RecordListReader.
    b.append("INVALID RECORD (error is at index ").append(i).append("):").append(Globals.lineSep);
    b.append("------------------------------").append(Globals.lineSep);
    for (String l : lines) {
      b.append("   ").append(l).append(Globals.lineSep);
    }
    b.append("------------------------------").append(Globals.lineSep);
  }
}
