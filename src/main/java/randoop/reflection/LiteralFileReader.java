package randoop.reflection;

import java.util.ArrayList;
import java.util.List;

import randoop.operation.ConcreteOperation;
import randoop.operation.NonreceiverTerm;
import randoop.operation.OperationParseException;
import randoop.sequence.Sequence;
import randoop.sequence.Variable;
import randoop.types.ConcreteType;
import randoop.types.TypeNames;
import randoop.util.MultiMap;
import randoop.util.RecordListReader;
import randoop.util.RecordProcessor;

/**
 * Reads a file specifying literal values to use during generation. Method
 * parse(String) takes as input the name of a text file. The text file should
 * contain one or more records of the form:
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
 * <ul>
 * <li>classname is the fully-qualified name of a valid class. More
 * specifically, TypeNames.getTypeForName(classname) must return a valid Class
 * object.
 * <li>Each type:value pair describes the type and value of a literal (for
 * example, <tt>int:3</tt>).
 * </ul>
 * Blank lines and comment lines (lines starting with "#") are ignored, both
 * between records and inside records.
 * <p>
 *
 * An example literals file appears in file
 * randoop/systemtests/resources/literalsfile.txt.
 * <p>
 *
 * LIMITATIONS:
 * <p>
 *
 * Error messages do not include line numbers pointing to location of the error.
 * </p>
 *
 * There is no way to specify literals that are not related to any class in
 * particular, or literals that are related to only specific methods within a
 * class.
 */
public class LiteralFileReader {

  private static final String CLASSLITERALS = "CLASSLITERALS";
  private static final String CLASSNAME = "CLASSNAME";
  private static final String LITERALS = "LITERALS";

  /**
   * Returns a map from class to list of constants.
   *
   * @param inFile  the input file
   * @return the map from types to literal values
   */
  public static MultiMap<ConcreteType, Sequence> parse(String inFile) {

    final MultiMap<ConcreteType, Sequence> map = new MultiMap<>();

    final ModelCollections collections = new ConcreteSequenceCollections(map);
    final TypedOperationManager manager = new TypedOperationManager(collections);

    RecordProcessor processor =
        new RecordProcessor() {
          @Override
          public void processRecord(List<String> lines) {

            if (!(lines.size() >= 1 && lines.get(0).trim().toUpperCase().equals(CLASSNAME))) {
              throwInvalidRecordError("record does not begin with \"" + CLASSNAME + "\"", lines, 0);
            }

            if (!(lines.size() >= 2)) {
              throwInvalidRecordError("class name missing", lines, 1);
            }

            Class<?> cls = null;
            try {
              cls = TypeNames.getTypeForName(lines.get(1));
            } catch (ClassNotFoundException e) {
              throwInvalidRecordError(e, lines, 1);
            }
            assert cls != null;

            if (!(lines.size() >= 3 && lines.get(2).trim().toUpperCase().equals(LITERALS))) {
              throwInvalidRecordError("Missing field \"" + LITERALS + "\"", lines, 2);
            }

            for (int i = 3; i < lines.size(); i++) {
              try {
                NonreceiverTerm.parse(lines.get(i), manager);
              } catch (OperationParseException e) {
                throwInvalidRecordError(e, lines, i);
              }
            }
          }
        };

    RecordListReader reader = new RecordListReader(CLASSLITERALS, processor);
    reader.parse(inFile);

    return map;
  }

  private static void throwInvalidRecordError(Exception e, List<String> lines, int i) {
    throw new Error(e);
  }

  private static void throwInvalidRecordError(String string, List<String> lines, int i) {
    StringBuilder b = new StringBuilder();
    b.append("RECORD PROCESSING ERROR: ").append(string).append("\n");
    appendRecord(b, lines, i);
    throw new Error(b.toString());
  }

  private static void appendRecord(StringBuilder b, List<String> lines, int i) {
    // This printout is less than ideal (it does not include the START/END
    // delimiters) and has no line number data, a limitation inherited from
    // RecordProcessor/RecordListReader.
    b.append("INVALID RECORD (error is at index ").append(i).append("):\n");
    b.append("------------------------------\n");
    for (String l : lines) {
      b.append("   ").append(l).append("\n");
    }
    b.append("------------------------------\n");
  }

  private static class ConcreteSequenceCollections extends ModelCollections {

    private final MultiMap<ConcreteType, Sequence> map;

    ConcreteSequenceCollections(MultiMap<ConcreteType, Sequence> map) {
      this.map = map;
    }

    @Override
    public void addConcreteOperation(ConcreteType declaringType, ConcreteOperation operation) {
      map.add(declaringType, new Sequence().extend(operation, new ArrayList<Variable>()));
    }
  }
}
