package randoop.util;

import static org.apache.commons.codec.CharEncoding.UTF_8;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/** Utilities for working with streams. */
public class StreamUtils {

  /**
   * Captures lines from the stream as a {@code List<String>}.
   *
   * @param stream the stream to read from
   * @return the list of lines read from the stream
   * @throws IOException if there is an error reading from the stream
   */
  public static List<String> captureLinesFromStream(InputStream stream) throws IOException {
    List<String> outputLines = new ArrayList<>();
    try (BufferedReader rdr = new BufferedReader(new InputStreamReader(stream, UTF_8))) {
      String line;
      while ((line = rdr.readLine()) != null) {
        outputLines.add(line);
      }
    }
    return outputLines;
  }
}
