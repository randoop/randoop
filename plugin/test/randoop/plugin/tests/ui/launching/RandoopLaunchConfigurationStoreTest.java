package randoop.plugin.tests.ui.launching;

import randoop.plugin.internal.ui.launching.RandoopLaunchConfigurationStore;
import junit.framework.TestCase;

public class RandoopLaunchConfigurationStoreTest extends TestCase {
  
  
  public void testGetInstance() {
    RandoopLaunchConfigurationStore.getInstance();
  }
}
