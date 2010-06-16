package randoop.plugin.tests.ui.launchConfigurations;

import randoop.plugin.internal.ui.launchConfigurations.RandoopLaunchConfigurationStore;
import junit.framework.TestCase;

public class RandoopLaunchConfigurationStoreTest extends TestCase {
  
  
  public void testGetInstance() {
    RandoopLaunchConfigurationStore.getInstance();
  }
}
