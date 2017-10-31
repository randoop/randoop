package randoop.util;

import java.io.File;

public class FileUtilities {

  /**
   * Deletes a directory by recursively deleting all its sub-directories and files.
   *
   * @param dir the directory to be deleted
   * @return true successfully deleted, false otherwise
   */
  public static boolean deleteDirectory(File dir) {
    if (dir.isDirectory()) {
      File[] children = dir.listFiles();
      for (int i = 0; i < children.length; i++) {
        boolean success = deleteDirectory(children[i]);
        if (!success) {
          return false;
        }
      }
    }
    // either a file or an empty directory
    return dir.delete();
  }
}
