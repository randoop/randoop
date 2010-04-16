package cov;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Various utility methods that cov uses.
 */
public class FilesUtil {

  static List<String> getJavaFileNames(List<String> paths) {
    List<File> filesAbsolute = new ArrayList<File>();
    for (String path : paths) {
      filesAbsolute.add(new File(path).getAbsoluteFile());
    }
    List<File> javaFiles = FilesUtil.getJavaFiles(filesAbsolute);
    List<String> retval = new ArrayList<String>();
    for (File javaFile : javaFiles) {
      retval.add(javaFile.getAbsolutePath());
    }
    return retval;
  }

  static List<File> getJavaFiles(List<File> args) {
    FilesUtil.checkAbsolute(args);
    List<File> retval = new ArrayList<File>();
    for (File oneArg : args) {
      retval.addAll(FilesUtil.getJavaFiles(oneArg));
    }
    return retval;
  }

  static List<File> getJavaFiles(File f) {
    FilesUtil.checkAbsolute(f);
    if (f.isDirectory()) {
      return getJavaFiles(FilesUtil.listFilesAbsolutePath(f));
    }
    if (f.getName().endsWith(".java")) {
      return Collections.singletonList(f);
    }
    return Collections.emptyList();
  }

  static List<File> listFilesAbsolutePath(File dir) {
    assert dir.isDirectory();
    FilesUtil.checkAbsolute(dir);
    List<File> retval = new ArrayList<File>();
    for (String f : dir.list()) {
      retval.add(new File(dir, f));
    }
    return retval;
  }

  static void checkAbsolute(File f) {
    if (!f.isAbsolute()) throw new IllegalStateException();
  }

  static void checkAbsolute(List<File> fl) {
    for (File f : fl) checkAbsolute(f);
  }

}
