package randoop;

import java.util.List;

import randoop.util.MultiMap;
import randoop.util.RecordListReader;
import randoop.util.RecordProcessor;


/**
 * Implements functionality to read a file specifying literal values
 * to use during generation. Method parse(String) takes as input the name
 * of a text file. The text file should contain one or more records of the form:
 * 
 * START CLASSLITERALS
 * CLASSNAME
 * <classname>
 * LITERALS
 * <type>:<value>
 * ...
 * <type>:<value>
 * END CLASSLITERALS
 *
 * Where:
 *
 * <classname> is the fully-qualified name of a valid class.
 * More specifically, Class.forName(classname) must return a valid Class object.
 *
 * Blank lines and comment lines (lines starting with "#") between records, as
 * well as inside records, are ignored.
 * 
 * Each <type>:<value> pair describes the type and value of a literal (for
 * example, "int:3").
 * 
 * LIMITATIONS:
 * 
 * Error messages do not include line numbers pointing to location of the error.
 * 
 * There is no way to specify literals that are not related to any class in particular,
 * or literals that are related to only specific methods within a class.
 */
public class LiteralFileReader {

  private static final String CLASSLITERALS = "CLASSLITERALS";
  private static final String CLASSNAME = "CLASSNAME";
  private static final String LITERALS = "LITERALS";

  public static MultiMap<Class<?>, PrimitiveOrStringOrNullDecl> parse(String inFile) {
    
    final MultiMap<Class<?>, PrimitiveOrStringOrNullDecl> map =
      new MultiMap<Class<?>, PrimitiveOrStringOrNullDecl>();

    RecordProcessor processor = new RecordProcessor() {
      public void processRecord(List<String> lines) {
        
        if (!(lines.size() >= 1 && lines.get(0).trim().toUpperCase().equals(CLASSNAME))) {
          throwInvalidRecordError("record does not begin with \"" + CLASSNAME + "\"", lines, 0);
        }

        if (!(lines.size() >= 2)) {
          throwInvalidRecordError("class name missing", lines, 1);
        }

        Class<?> cls = null;
        try {
          cls = Class.forName(lines.get(1));
        } catch (ClassNotFoundException e) {
          throwInvalidRecordError(e, lines, 1);
        }
        assert cls != null;

        if (!(lines.size() >= 3 && lines.get(2).trim().toUpperCase().equals(LITERALS))) {
          throwInvalidRecordError("Missing field \"" + LITERALS + "\"", lines, 2);
        }
        
        for (int i = 3 ; i < lines.size() ; i++) {
          try {
            PrimitiveOrStringOrNullDecl p = PrimitiveOrStringOrNullDecl.parse(lines.get(i));
            map.add(cls, p);
          } catch (StatementKindParseException e) {
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
    b.append("RECORD PROCESSING ERROR: " + string + "\n");
    appendRecord(b, lines, i);
    throw new Error(b.toString());
  }

  private static void appendRecord(StringBuilder b, List<String> lines, int i) {
    // This printout is less than ideal (it does not include the START/END
    // delimiters) and has no line number data, a limitation inherited from
    // RecordProcessor/RecordListReader.
    b.append("INVALID RECORD (error is at index " + i + "):\n");
    b.append("------------------------------\n");
    for (String l : lines) {
      b.append("   " + l + "\n");
    }
    b.append("------------------------------\n");
  }
  
  
}
