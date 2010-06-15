package randoop;

import java.util.List;

import randoop.util.MultiMap;
import randoop.util.RecordListReader;
import randoop.util.RecordProcessor;

public class LiteralFileReader {

  private static final String CLASSLITERALS = "CLASSLITERALS";
  private static final String CLASSNAME = "CLASSNAME";
  private static final String LITERALS = "LITERALS";

  /**
   * START CLASSLITERALS
   * CLASSNAME
   * <classname>
   * LITERALS
   * <type>:<value>
   * ...
   * <type>:<value>
   * END CLASSLITERALS
   *
   * <classname> is the fully-qualified name of a valid class. More specifically, Class.forName(classname)
   * must return a valid Class object.
   *
   * Blank lines and comment lines (lines starting with "#") between records, as
   * well as inside records, are ignored.
   * 
   * Literals field can be empty (i.e. no <type>:<value> pair). 
   * 
   */
  public static MultiMap<Class<?>, PrimitiveOrStringOrNullDecl> parse(String inFile) {
    
    final MultiMap<Class<?>, PrimitiveOrStringOrNullDecl> map =
      new MultiMap<Class<?>, PrimitiveOrStringOrNullDecl>();

    RecordProcessor processor = new RecordProcessor() {
      public void processRecord(List<String> lines) {
        
        if (!(lines.size() >= 1 && lines.get(0).trim().toUpperCase().equals(CLASSNAME))) {
          throwInvalidRecordError("record does not begin with \"" + CLASSNAME + "\" field name", lines, 0);
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
          throwInvalidRecordError("Missing field \"" + LITERALS + "\" field name", lines, 2);
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

      private void throwInvalidRecordError(Exception e, List<String> lines, int i) {
        throw new Error(e);        
        
      }
      private void throwInvalidRecordError(String string, List<String> lines, int i) {
        throw new Error(string);
      }
    };
    
    RecordListReader reader = new RecordListReader(CLASSLITERALS, processor);
    reader.parse(inFile);
    
    return map;
  }
    
  
  
}
