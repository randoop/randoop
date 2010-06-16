package randoop.plugin.tests.resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import junit.framework.TestCase;

public class FileResources extends TestCase {
  public static void copy(File source, File destination) throws IOException {
    assertTrue(source.exists());
    assertFalse(destination.exists());

    if (source.isFile()) {
      copyFile(source, destination);
    } else {
      copyDirectory(source, destination);
    }
  }

  private static void copyDirectory(File source, File destination) throws IOException {
    assertTrue(source.isDirectory());
    if (destination.exists()) {
      destination.mkdirs();
    }
    assertTrue(destination.isDirectory());

    for (File f : source.listFiles()) {
      copy(f, new File(destination, f.getName()));
    }
  }

  /**
   * Copies a file from a source to the supplied destination. The destination
   * file and its parent directories will be created if they do not exist.
   * 
   * @param source
   *          file to be copied
   * @param destination
   *          location of new file
   * @param monitor
   * @throws IOException
   */
  private static void copyFile(File source, File destination) throws IOException {
    // Create the directories and file for destination
    destination.getParentFile().mkdirs();
    destination.createNewFile();

    InputStream in = new FileInputStream(source);
    OutputStream out = new FileOutputStream(destination);

    byte[] buffer = new byte[1024];
    int length;

    while ((length = in.read(buffer)) > 0) {
      out.write(buffer, 0, length);
    }

    in.close();
    out.close();
  }
}
