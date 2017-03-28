package randoop.compile;

import static randoop.compile.CompileUtil.toURI;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import javax.tools.SimpleJavaFileObject;

/**
 * based on {@code javaxtools.compiler.JavaFileObjectImple} from <a
 * href="http://www.ibm.com/developerworks/library/j-jcomp/index.html">Create dynamic applications
 * with javax.tools</a>.
 */
class SequenceJavaFileObject extends SimpleJavaFileObject {

  private final String source;
  private ByteArrayOutputStream byteCode;

  SequenceJavaFileObject(final String classFileName, final Kind kind) {
    super(toURI(classFileName), kind);
    this.source = null;
  }

  SequenceJavaFileObject(String classFileName, String sequenceClass) {
    super(toURI(classFileName), Kind.SOURCE);
    this.source = sequenceClass;
  }

  @Override
  public CharSequence getCharContent(final boolean ignoreEncodingErrors)
      throws UnsupportedOperationException {
    if (source == null) throw new UnsupportedOperationException("getCharContent()");
    return source;
  }

  @Override
  public InputStream openInputStream() {
    return new ByteArrayInputStream(getByteCode());
  }

  @Override
  public OutputStream openOutputStream() {
    byteCode = new ByteArrayOutputStream();
    return byteCode;
  }

  byte[] getByteCode() {
    return byteCode.toByteArray();
  }
}
