package randoop.instrument;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import org.checkerframework.checker.mustcall.qual.Owning;
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
   * @throws IOException if there is an error reading the file
   * @throws X if the action throws it
   */
  static <X extends Exception> void forEachEntry(
      @Owning Reader fileReader, String filename, EntryAction<X> action) throws IOException, X {
    try (EntryReader reader =
        new EntryReader(fileReader, filename, EntryFormat.DEFAULT, CommentFormat.C, null)) {
      for (String line : reader) {
        String entry = line.trim();
        if (!entry.isEmpty()) {
          action.accept(entry, reader.getLineNumber());
        }
      }
    } catch (UncheckedIOException e) {
      // EntryReader's iterator wraps an IOException in an UncheckedIOException.
      throw e.getCause();
    }
  }
}
