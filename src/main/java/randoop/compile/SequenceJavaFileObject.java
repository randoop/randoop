package randoop.compile;

import static randoop.compile.CompileUtil.toURI;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import javax.tools.SimpleJavaFileObject;

/**
 * A {@code JavaFileObject} for source code in memory.
 *
 * <p>based on {@code javaxtools.compiler.JavaFileObjectImple} from <a
 * href="https://www.ibm.com/developerworks/library/j-jcomp/index.html">Create dynamic applications
 * with javax.tools</a>.
 */
class SequenceJavaFileObject extends SimpleJavaFileObject {

  /** The source code text. */
  private final String source;

  /** The stream for reading the source code. */
  private ByteArrayOutputStream byteStream;

  /**
   * Creates a {@link SequenceJavaFileObject} for the given class name and kind.
   *
   * @param classFileName the name of the class
   * @param kind either {@code SOURCE} or {@code CLASS}
   */
  SequenceJavaFileObject(final String classFileName, final Kind kind) {
    super(toURI(classFileName), kind);
    this.source = null;
  }

  /**
   * Creates a {@link SequenceJavaFileObject} with the given name and class source.
   *
   * @param classFileName the name of the class
   * @param sequenceClass the class source
   */
  SequenceJavaFileObject(String classFileName, String sequenceClass) {
    super(toURI(classFileName), Kind.SOURCE);
    this.source = sequenceClass;
  }

  @Override
  public CharSequence getCharContent(final boolean ignoreEncodingErrors)
      throws UnsupportedOperationException {
    if (source == null) {
      throw new UnsupportedOperationException("getCharContent()");
    }
    return source;
  }

  @Override
  public InputStream openInputStream() {
    return new ByteArrayInputStream(getByteCode());
  }

  @Override
  public OutputStream openOutputStream() {
    byteStream = new ByteArrayOutputStream();
    return byteStream;
  }

  /**
   * Returns the byte stream as a byte array.
   *
   * @return the byte array for the byte stream
   */
  byte[] getByteCode() {
    return byteStream.toByteArray();
  }
}
