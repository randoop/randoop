package randoop.experiments;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.Map;

public class JCrasherResults {

  // maps thigs like "java.lang.NullPointException" --> 2
  private final Map<String,Integer> numExceptionsByName = new LinkedHashMap<String,Integer>();
  private String name;

  public JCrasherResults(String name) {
    this.name = name;
  }


  public void processJcrasherOutput(String s) throws IOException {
    BufferedReader reader = new BufferedReader(new StringReader(s));
    String line = reader.readLine();
    int errorsAfterFiltering = -1;
    int errorLinesFound = 0;

    while (line != null) {
      // ) test4(jcrash.java.util.HashSetTest1)java.lang.IllegalArgumentException: Illegal load factor: -100.12346
      if (line.matches("^[\\d]+\\) testclasses[\\d]+.*")) {
        errorLinesFound++;
        int firstParen = line.indexOf(')');
        int secondParen = line.indexOf(')', firstParen + 1);
        String exceptionClassAndMessage = line.substring(secondParen + 1);
        int exceptionIndex = exceptionClassAndMessage.indexOf(':');
        String exceptionType = null;
        if (exceptionIndex > 0) {
          exceptionType = exceptionClassAndMessage.substring(0, exceptionIndex);
        } else {
          exceptionType = exceptionClassAndMessage;
        }
        Integer exceptionCount = numExceptionsByName.get(exceptionType);
        int count = (exceptionCount == null ? 0 : exceptionCount.intValue());
        numExceptionsByName.put(exceptionType, count + 1);

      } else if (line.startsWith("Exceptions and Errors after filtering")) {
        String errorsAfterFilteringString =
          line.substring("Exceptions and Errors after filtering (E): ".length());
        errorsAfterFiltering = Integer.parseInt(errorsAfterFilteringString);
      }
      line = reader.readLine();
    }
    if (errorsAfterFiltering != errorLinesFound) {
      throw new RuntimeException("things didn't match up. errorsAfterFiltering="
          + errorsAfterFiltering + ", errorLinesFound=" + errorLinesFound);
    }
  }

  @Override
  public String toString() {
    return name + ":" + numExceptionsByName.toString();
  }

}
