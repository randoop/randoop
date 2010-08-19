package randoop.plugin.tests.core;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import randoop.plugin.tests.WorkspaceManager;
import randoop.plugin.tests.WorkspaceManagerTest;
import randoop.plugin.tests.core.runtime.MessageReceiverTest;

@RunWith(Suite.class)
@SuiteClasses({
  WorkspaceManagerTest.class,
  TypeMnemonicTest.class,
  MethodMnemonicsTest.class,
  MessageReceiverTest.class
})
@SuppressWarnings("nls")
public class CoreTestSuite {
  @BeforeClass
  public static void setup() throws IOException, CoreException {
    System.out.println("Setting up the JUnit workspace...");
    WorkspaceManager.setupDemoWorkspace();
    System.out.println("Building workspace...");
    WorkspaceManager.buildWorkspace();
    System.out.println("Starting tests...");
  }

  @AfterClass
  public static void teardown() {
    System.out.println("Done!");
  }
}
