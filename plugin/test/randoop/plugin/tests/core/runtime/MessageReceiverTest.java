package randoop.plugin.tests.core.runtime;

import junit.framework.TestCase;

import org.eclipse.jdt.core.IJavaProject;

import randoop.plugin.tests.ui.launching.ProjectCreator;

public class MessageReceiverTest extends TestCase {
  IJavaProject fJavaProject;
  
  @Override
  protected void setUp() throws Exception {
    fJavaProject = ProjectCreator.createStandardDemoProject();
  }
}
