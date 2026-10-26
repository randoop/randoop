package randoop.instrument;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;

import com.beust.jcommander.internal.Console;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import org.apache.bcel.classfile.JavaClass;
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

  /** A console whose {@code readPassword} must not be called, because it should be replaced. */
  public static class UnreadableConsole implements Console {
    @Override
    public void print(String msg) {}

    @Override
    public void println(String msg) {}

    @Override
    public char[] readPassword(boolean echoInput) {
      throw new AssertionError("Console.readPassword was not replaced");
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
    JavaClass javaClass = ReplacementFileReader.getJavaClassFromClassname(className);
    assertNotNull("Cannot find " + className, javaClass);
    byte[] original = javaClass.getBytes();
    ClassLoader loader = getClass().getClassLoader();
    byte[] transformed =
        transformer.transform(loader, className.replace('.', '/'), null, null, original);
    assertNotNull("No call was replaced", transformed);

    Class<?> c = new BytesClassLoader(loader).define(className, transformed);
    char[] password =
        (char[]) c.getMethod("read", Console.class).invoke(null, new UnreadableConsole());
    assertArrayEquals(new char[0], password);
  }
}
