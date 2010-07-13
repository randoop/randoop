package randoop.plugin.tests;

import org.junit.Test;

public class ResourcesTest {

  @Test
  public void testClearWorkspace() {
    WorkspaceManager.clearActiveWorkspace();
  }
  
  @Test
  public void testCreateProjects() {
    ProjectFactory.createPathPlannerProject();
    ProjectFactory.createKenKenProject();
  }  
}
