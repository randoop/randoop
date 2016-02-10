package randoop.experiments;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.bcel6.classfile.ClassParser;
import org.apache.commons.bcel6.classfile.JavaClass;

import randoop.Globals;

/**
 * Finds all files ending in ".class" in the given package, located in directory
 * classDir. For each file, it strips the file extension, converts "/" to "."
 * and returns the result. For example, file "foo/bar/MyClass.class" would be
 * returned as "foo.bar.MyClass".
 *
 * Inner classes (those ending in $D, where D is a digit) are ignored.
 *
 * If the -omit:"REGEXP" option is given, then any class names that match REGEXP
 * will be omitted.
 *
 * Results are output to destinationFile.
 */
public class ClassListPrinter {

  /**
   * @param destinationFile
   *          the file where class names are written
   * @param classDir
   *          the directory for the package
   * @param packageName
   *          the name of the package (may be null)
   * @param filter
   *          the class filter
   * @throws IOException
   *           if there is an error writing the file
   * @throws IllegalArgumentException
   *           if filter is null, classDir is invalid, or packageName doesn't
   *           correspond to directory
   */
  public static void findPublicTopLevelClasses(String destinationFile, String classDir, String packageName, ClassFilter filter) throws IOException {

    // TODO check other args also not null.
    if (filter == null)
      throw new IllegalArgumentException("filter cannot be null.");

    File baseDir = new File(classDir);
    if (!baseDir.exists())
      throw new IllegalArgumentException("Path does not exist:" + baseDir);

    File dir = baseDir;
    if (packageName != null && packageName.length() > 0) {
      for (String s : packageName.split("\\.")) {
        dir = new File(dir, s);
        if (!dir.exists() || !dir.isDirectory())
          throw new IllegalArgumentException("Not a directory: " + dir.getAbsolutePath());
      }
    }

    FileWriter writer = new FileWriter(destinationFile);
    outputClassStrings(writer, baseDir, dir, filter);
    writer.close();
  }

  private static void outputClassStrings(FileWriter writer, File baseDir, File dir, ClassFilter filter) throws IOException {
    if (!dir.isDirectory())
      throw new IllegalArgumentException("Expected a directory path.");

    for (File file : dir.listFiles()) {
      String fileName = file.getName();
      if (fileName.endsWith(".class")) {

        JavaClass cls = new ClassParser(file.getAbsolutePath()).parse();

        if (!filter.include(cls))
          continue;

        // Modify and output
        // The call to replace is to get foo/bar/baz in Windows systems.
        String withoutExt = dir.getAbsolutePath().substring(baseDir.getAbsolutePath().length()).replace("\\", "/") + "/"
            + fileName.substring(0, fileName.length() - 6);
        String withDots = withoutExt.replace('/', '.');
        // Remove any heading dots
        while (withDots.startsWith("."))
          withDots = withDots.substring(1);
        writer.write(withDots + Globals.lineSep);
        writer.flush();
        continue;
      }
      File f = new File(dir, fileName);
      if (f.isDirectory()) {
        outputClassStrings(writer, baseDir, f, filter);
      }
    }
  }
}
