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
 * ClassTypeLoader provides methods that allow a list of class names to be loaded
 * as {@link Class<?>} objects.
 * 
 * (Extracted from class previously known as randoop.util.Reflection.)
 * 
 * @author bjkeller
 *
 */
public class ClassTypeLoader {

  /**
   * loadClassesFromList returns a list of {@link Class} objects given a list of class names.
   * In the case that the typename recognition throws an exception, an error is only thrown if
   * <code>noerr</code> is false.
   * 
   * @param classNames list of strings for classnames.
   * @param noerr a boolean flag to indicate whether to report classes not found.
   * @return list of {@link Class} objects corresponding to elements of list.
   */
  public static List<Class<?>> loadClassesFromList(List<String> classNames, boolean noerr) {
    List<Class<?>> result = new ArrayList<Class<?>>(classNames.size());
    for (String className : classNames) {
      try {
        Class<?> c = TypeNames.recognizeType(className);
        result.add(c);
      } catch (ClassNotFoundException e) {
        if (!noerr) {
          throw new Error("No class found for type name \"" + className + "\"");
        }
      }
    }
    return result;
  }

  /**
   * loadClassesFromStream returns a list of {@link Class} objects given a stream
   * containing class names. Blank lines and lines starting with "#" are ignored.
   * 
   * @see loadClasses
   * 
   * @param in an {@link InputStream} from which class names are read.
   * @param filename the name of the file from which class names are read.
   * @return list of {@link Class} objects corresponding to class names from stream.
   */
  public static List<Class<?>> loadClassesFromStream(InputStream in, String filename) {
    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
    return loadClasses(reader, filename, false);
  }

  /**
   * loadClassesFromReader returns a list of {@link Class} objects given a {@link BufferedReader} object
   * from which to read class names. Blank lines and lines starting wtih "#" are ignored.
   * 
   * @param reader a {@link BufferedReader} from which to read class names.
   * @param filename the name of the input file.
   * @return a list of {@link Class} objects corresponding to class names.
   */
  public static List<Class<?>> loadClassesFromReader(BufferedReader reader, String filename) {
    return loadClasses(reader, filename, false);
  }

  /**
   * loadClasses returns a list of {@link Class} objects given a {@link BufferedReader} object
   * from which to read class names. Blank lines and lines starting with "#" are ignored.
   * 
   * @param reader a {@link BufferedReader} from which to read class names.
   * @param filename the name of the input file.
   * @param noerr a boolean indicating whether to report errors for bad class names.
   * @return a list of {@link Class} objects corresponding to class names.
   */
  private static List<Class<?>> loadClasses(BufferedReader reader, String filename, boolean noerr) {
    List<Class<?>> result = new ArrayList<Class<?>>();
    EntryReader er = new EntryReader(reader, filename, "^#.*", null);
    for (String line : er) {
      String trimmed = line.trim();
      try {
        Class<?> c = TypeNames.recognizeType(trimmed);
        result.add(c);
      } catch (ClassNotFoundException e) {
        if (!noerr) {
          throw new Error("No class found for type name \"" + trimmed + "\"");
        }
      }
    }
    return result;
  }

  /** Blank lines and lines starting with "#" are ignored.
   *  Other lines must contain string such that Class.forName(s) returns a class.
   */
  /**
   * loadClassesFromFile returns a list of {@link Class} objects given a {@link File} object.
   * Blank lines and lines starting with "#" are ignored.
   * 
   * @param classListingFile
   * @return list of {@link Class} objects corresponding to class names.
   * @throws IOException
   */
  public static List<Class<?>> loadClassesFromFile(File classListingFile) throws IOException {
    return loadClassesFromFile(classListingFile, false);
  }

  /**
   * loadClassesFromFile returns a list of {@link Class} objects given a file containing 
   * class names. Blank lines and lines starting with "#" are ignored.
   * 
   * @param classListingFile a {@link File} object from which to read the class names.
   * @param noerr a boolean indicating whether to report bad class names as an error.
   * @return list of {@link Class} objects corresponding to class names.
   * @throws IOException
   */
  public static List<Class<?>> loadClassesFromFile(File classListingFile, boolean noerr) throws IOException {
    try (BufferedReader reader = Files.getFileReader(classListingFile)) {
      return loadClasses(reader, classListingFile.getPath(), noerr);
    } 
  }

}
