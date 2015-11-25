package randoop.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import randoop.types.TypeNames;
import randoop.util.Files;

import plume.EntryReader;

/**
 * ClassReader is a library of static methods that converts a list of
 * class names into a list of {@link Class} objects. 
 * The list of names may be read from a file, an input stream (or reader), and the
 * resulting list is constructed using reflection.
 * <p>
 * (Extracted from class previously known as randoop.util.Reflection.)
 */
public class TypeReader {

  /**
   * Returns a list of {@link Class} objects given a list of class names.
   * In the case that the type name recognition throws an exception, an error is only thrown if
   * <code>ignoreBadNames</code> is false.
   * 
   * @param classNames  the list of fully qualified class names.
   * @param ignoreBadNames  a flag to indicate that {@code ClassNotFoundExceptions} should be ignored. 
   * @return list of {@link Class} objects corresponding to elements of list.
   */
  public static List<Class<?>> getTypesForNames(List<String> classNames, boolean ignoreBadNames) {
    List<Class<?>> result = new ArrayList<Class<?>>(classNames.size());
    for (String className : classNames) {
      try {
        Class<?> c = TypeNames.getTypeForName(className);
        result.add(c);
      } catch (ClassNotFoundException e) {
        if (ignoreBadNames) {
          throw new Error("No class found for type name \"" + className + "\"");
        }
      }
    }
    return result;
  }

  /**
   * Returns a list of {@link Class} objects given a stream
   * containing class names. Blank lines and lines starting with "#" are ignored.
   * 
   * @see #getTypesForReader(BufferedReader, String)
   * 
   * @param in an {@link InputStream} from which class names are read.
   * @param filename the name of the file from which class names are read.
   * @return list of {@link Class} objects corresponding to class names from stream.
   */
  public static List<Class<?>> getTypesForStream(InputStream in, String filename) {
    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
    return getTypesForReader(reader, filename);
  }

  /**
   * Returns a list of {@link Class} objects given a {@link BufferedReader} object
   * from which to read class names. Blank lines and lines starting with "#" are ignored.
   * 
   * @param reader a {@link BufferedReader} from which to read class names.
   * @param filename the name of the input file.
   * @return a list of {@link Class} objects corresponding to class names.
   */
  public static List<Class<?>> getTypesForReader(BufferedReader reader, String filename) {
    List<Class<?>> result = new ArrayList<Class<?>>();
    EntryReader er = new EntryReader(reader, filename, "^#.*", null);
    for (String line : er) {
      String trimmed = line.trim();
      try {
        Class<?> c = TypeNames.getTypeForName(trimmed);
        result.add(c);
      } catch (ClassNotFoundException e) {
        throw new Error("No class found for type name \"" + trimmed + "\"");
      }
    }
    return result;
  }

  /**
   * Returns a list of {@link Class} objects given a file containing 
   * class names. Blank lines and lines starting with "#" are ignored.
   * 
   * @param classListingFile a {@link File} object from which to read the class names.
   * @return list of {@link Class} objects corresponding to class names.
   * @throws IOException
   */
  public static List<Class<?>> getTypesForFile(File classListingFile) throws IOException {
    try (BufferedReader reader = Files.getFileReader(classListingFile)) {
      return getTypesForReader(reader, classListingFile.getPath());
    } 
  }

}
