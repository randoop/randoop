package randoop.instrument;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;

import com.beust.jcommander.internal.Console;
import com.beust.jcommander.internal.DefaultConsole;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import org.junit.Test;

/** Tests for {@link CallReplacementTransformer}. */
public class CallReplacementTransformerTest {

  /** Calls {@code readPassword} through the {@code Console} interface, as JCommander does. */
  public static class PasswordReader {
    /**
     * Reads a password from the given console.
     *
     * @param console the console
     * @return the password
     */
    public static char[] read(Console console) {
      return console.readPassword(false);
    }
  }

  /** A class loader that defines a class from given bytes. */
  private static class BytesClassLoader extends ClassLoader {
    /**
     * Creates a class loader.
     *
     * @param parent the parent class loader
     */
    BytesClassLoader(ClassLoader parent) {
      super(parent);
    }

    /**
     * Defines a class from the given bytes.
     *
     * @param name the binary name of the class
     * @param bytes the class file
     * @return the class
     */
    Class<?> define(String name, byte[] bytes) {
      return defineClass(name, bytes, 0, bytes.length);
    }
  }

  @Test
  public void jcommanderReadPasswordTest() throws Exception {
    Path file = Paths.get("build/resources/test/randoop/instrument/jcommanderreplacement.txt");
    Map<MethodSignature, MethodSignature> map = ReplacementFileReader.readReplacements(file);
    CallReplacementTransformer transformer =
        new CallReplacementTransformer(map, Collections.emptySet());

    String className = PasswordReader.class.getName();
    byte[] original = readClassFile(PasswordReader.class);
    ClassLoader loader = getClass().getClassLoader();
    byte[] transformed =
        transformer.transform(loader, className.replace('.', '/'), null, null, original);
    assertNotNull("No call was replaced", transformed);

    // The unreplaced DefaultConsole.readPassword would read from standard input.
    Class<?> c = new BytesClassLoader(loader).define(className, transformed);
    char[] password =
        (char[]) c.getMethod("read", Console.class).invoke(null, new DefaultConsole());
    assertArrayEquals(new char[0], password);
  }

  /**
   * Returns the class file of the given class.
   *
   * @param c a class
   * @return the class file of {@code c}
   * @throws IOException if the class file cannot be read
   */
  private static byte[] readClassFile(Class<?> c) throws IOException {
    String resource = "/" + c.getName().replace('.', '/') + ".class";
    try (InputStream in = c.getResourceAsStream(resource)) {
      assertNotNull("Cannot find " + resource, in);
      return in.readAllBytes();
    }
  }
}
