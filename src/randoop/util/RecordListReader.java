package randoop.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import plume.UtilMDE;

/**
 * Reads a list of records from a text file, where a record is
 * partially specified by the client of this class.
 * 
 * A record is a sequence of lines, beginning with the string
 * "START <recordtype>" and ending with the string "END <recordtype>"
 * where <recordtype> is specified by the client. For example:
 * 
 * START person
 * ...
 * ... arbitrary text
 * ...
 * END person
 * 
 * Any lines within and between records, that are only whitespace
 * or start with "#", are skipped.
 * 
 * This class includes the functionality to parse individual records
 * out of a file. How each record is processed is up to the client,
 * who provides a RecordProcessor.
 */
public class RecordListReader {
  
  // startMarker is "START <recordType>"
  // endMarker   is "END <recordType>"
  private final String startMarker, endMarker;

  // The object in charge of doing whatever is to be done with the record. 
  private final RecordProcessor processor;

  public RecordListReader(String recordType, RecordProcessor proc) {
    if (recordType == null || recordType.length() == 0)
      throw new IllegalArgumentException("Invalid record type:" + recordType);
    if (proc == null)
      throw new IllegalArgumentException("proc cannot be null.");
    this.processor = proc;
    this.startMarker = "START " + recordType;
    this.endMarker = "END " + recordType;
  }

  public void parse(String inFile) {
    if (inFile == null || inFile.length() == 0)
      throw new IllegalArgumentException("Illegal input file name: " + inFile);

    BufferedReader reader;
    try {
      reader = UtilMDE.bufferedFileReader(inFile);
    } catch (IOException e) {
      throw new Error(e);
    }
    
    parse(reader);
  }
  
  public void parse(BufferedReader reader) {

    String line;
    try {
      line = nextNWCLine(reader);
      while (line != null) {
        line = line.trim();
        if (line.startsWith(startMarker)) {
          List<String> oneRecord = readOneRecord(reader);
          processor.processRecord(oneRecord);
        } else {
          throw new IllegalArgumentException("Expected \"" + startMarker + "\" but got " + line);
        }
        line = nextNWCLine(reader);
      }
    } catch (IOException e) {
      throw new Error(e);
    }
  }
  
  private List<String> readOneRecord(BufferedReader reader) throws IOException {
    List<String> ret = new ArrayList<String>();
    String line = nextNWCLine(reader);
    while (line != null && !line.equals(endMarker)) {
      if (line.length() == 0 || line.charAt(0) == '#')
        continue;
      ret.add(line);
      line = nextNWCLine(reader);
    }
    return ret;
  }


  private static String nextNWCLine(BufferedReader reader) throws IOException {
    String line = reader.readLine();
    if (line != null)
      line = line.trim();
    while (line != null && (line.length() == 0 || line.indexOf('#') == 0)) {
      line = reader.readLine();
      if (line != null)
        line = line.trim();
    }
    return line;
  }

}
