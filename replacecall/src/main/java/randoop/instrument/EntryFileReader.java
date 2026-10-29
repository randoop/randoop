package randoop.instrument;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.CharacterCodingException;
import org.checkerframework.checker.mustcall.qual.Owning;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.plumelib.util.EntryReader;
import org.plumelib.util.EntryReader.CommentFormat;
import org.plumelib.util.EntryReader.EntryFormat;

/** Reads the entries of a file, such as a replacement file or a package exclusion file. */
final class EntryFileReader {

  /** Do not instantiate. */
  private EntryFileReader() {
    throw new UnsupportedOperationException("Do not instantiate");
  }

  /**
   * An action to perform on each entry of a file.
   *
   * @param <X> the type of exception that the action may throw
   */
  @FunctionalInterface
  interface EntryAction<X extends Exception> {
    /**
     * Performs this action on the given entry.
     *
     * @param entry an entry of the file
     * @param lineNumber the line number of the entry
     * @throws X if the action fails
     */
    void accept(String entry, int lineNumber) throws X;
  }

  /**
   * Calls the given action on each entry of a file. An entry is a line, with leading and trailing
   * whitespace removed. Blank lines and C-style ({@code //} and {@code /* ... *}{@code /}) comments
   * are ignored.
   *
   * @param <X> the type of exception that the action may throw
   * @param fileReader the reader for the file. Is closed by this method.
   * @param filename the name of the file read by the reader
   * @param action called on each entry and its line number
   * @throws IOException if there is an error reading the file; its message names the file
   * @throws X if the action throws it
   */
  static <X extends Exception> void forEachEntry(
      @Owning Reader fileReader, String filename, EntryAction<X> action) throws IOException, X {
    // EntryReader.close() does not close the reader passed to its constructor.
    try (Reader ownedReader = fileReader;
        EntryReader reader =
            new EntryReader(ownedReader, filename, EntryFormat.DEFAULT, CommentFormat.C, null)) {
      for (String line = readLine(reader, filename);
          line != null;
          line = readLine(reader, filename)) {
        String entry = line.trim();
        if (!entry.isEmpty()) {
          action.accept(entry, reader.getLineNumber());
        }
      }
    }
  }

  /**
   * Reads the next line from the given reader, with comments removed.
   *
   * @param reader the reader to read from
   * @param filename the name of the file read by the reader
   * @return the next line, or null at end of file
   * @throws IOException if there is an error reading the file; its message names the file
   */
  private static @Nullable String readLine(EntryReader reader, String filename) throws IOException {
    try {
      return reader.readLine();
    } catch (CharacterCodingException e) {
      // No line number:  the decoder reads ahead, so the error may lie beyond the current line.
      throw new IOException(filename + ": invalid character encoding: " + e, e);
    } catch (IOException e) {
      throw new IOException(filename + ": " + e.getMessage(), e);
    }
  }
}
