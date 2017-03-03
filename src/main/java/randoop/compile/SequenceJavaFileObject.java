package randoop.compile;

import java.net.URI;

import javax.tools.SimpleJavaFileObject;

/**
 * Created by bjkeller on 3/2/17.
 */
public class SequenceJavaFileObject extends SimpleJavaFileObject {
  /**
   * Construct a SimpleJavaFileObject of the given kind and with the
   * given URI.
   *
   * @param uri  the URI for this file object
   * @param kind the kind of this file object
   */
  protected SequenceJavaFileObject(URI uri, Kind kind) {
    super(uri, kind);
  }
}
