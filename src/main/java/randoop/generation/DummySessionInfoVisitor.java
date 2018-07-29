package randoop.generation;

import org.jacoco.core.data.ISessionInfoVisitor;
import org.jacoco.core.data.SessionInfo;

/**
 * This class provides an {@link ISessionInfoVisitor} that does not make use of the data from the
 * coverage runtime. This is intended to be used by {@link CoverageTracker} which does not need the
 * given {@link SessionInfo}.
 */
public class DummySessionInfoVisitor implements ISessionInfoVisitor {
  /** Singleton instance of this class. */
  public static final DummySessionInfoVisitor instance = new DummySessionInfoVisitor();

  /** Initializes the session info visitor. */
  private DummySessionInfoVisitor() {}

  /**
   * Required by the {@link ISessionInfoVisitor} but the session information is not used by this
   * class.
   *
   * @param info session information
   */
  @Override
  public void visitSessionInfo(final SessionInfo info) {}
}
