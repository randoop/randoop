package randoop.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


/**
 * A collection of static methods to serialize an object to a file and
 * visceversa.
 */
public final class SerializationHelper {

  private SerializationHelper() {
    throw new IllegalStateException("no instances");
  }

  public static void writeSerialized(String fileName, Object o) {
    writeSerialized(new File(fileName), o);
  }

  public static void writeSerialized(File outFile, Object o) {
    ObjectOutputStream out = null;
    try {
      out = new ObjectOutputStream(new FileOutputStream(outFile));

      out.writeObject(o);

    } catch (Exception e) {
      Log.out.println("When trying to create a PrintWriter for file "
          + outFile + ", exception thrown: " + e);
      e.printStackTrace();
      throw new Error(e);
    }

  }

  public static Object readSerialized(String fileName) {
    return readSerialized(new File(fileName));
  }

  public static Object readSerialized(File inFile) {
    Object ret = null;
    try {
      FileInputStream fs = new FileInputStream(inFile);
      ObjectInputStream in = null;
      in = new ObjectInputStream(fs);
      ret = in.readObject();
      in.close();
      fs.close();
      return ret;
    } catch (Exception e) {
      Log.out.println("When trying to read serialized file " + inFile
          + ", exception thrown: " + e);
      e.printStackTrace();
      throw new Error(e);
    }
  }
}
