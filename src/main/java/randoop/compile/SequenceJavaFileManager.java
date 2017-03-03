package randoop.compile;

import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;

/**
 * Created by bjkeller on 3/2/17.
 */
public class SequenceJavaFileManager extends ForwardingJavaFileManager<JavaFileManager> {
  /**
   * Creates a new instance of ForwardingJavaFileManager.
   *
   * @param fileManager delegate to this file manager
   * @param classLoader
   */
  protected SequenceJavaFileManager(JavaFileManager fileManager, ClassLoader classLoader) {
    super(fileManager);
  }
}
