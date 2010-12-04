package randoop.plugin.tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.junit.Test;

@SuppressWarnings("nls")
public class WorkspaceManagerTest {
  static IJavaProject boundary;
  static IJavaProject compilationError;
  static IJavaProject kenken;
  static IJavaProject pathplanner;

  @Test
  public void testProjectsCreatedSuccessfully() throws CoreException {
    assertNull(WorkspaceManager.getJavaProject("Jf9jf23v55b4338cJR83"));

    kenken = WorkspaceManager.getJavaProject(WorkspaceManager.BOUNDARY);
    assertNotNull(kenken);
    assertTrue(kenken.exists());

    kenken = WorkspaceManager.getJavaProject(WorkspaceManager.COMPILATION_ERROR);
    assertNotNull(kenken);
    assertTrue(kenken.exists());

    kenken = WorkspaceManager.getJavaProject(WorkspaceManager.KENKEN);
    assertNotNull(kenken);
    assertTrue(kenken.exists());

    pathplanner = WorkspaceManager.getJavaProject(WorkspaceManager.PATH_PLANNER);
    assertNotNull(pathplanner);
    assertTrue(pathplanner.exists());
  }

}
