package randoop.plugin.internal.ui.launching;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;

import randoop.plugin.RandoopPlugin;
import randoop.plugin.internal.core.launching.IRandoopLaunchConfigurationConstants;

public class RandoopLaunchConfigurationStore {
  Map<ILaunchConfiguration, RandoopLaunchConfiguration> fLaunchConfigs;

  private static RandoopLaunchConfigurationStore instance;
  
  private RandoopLaunchConfigurationStore() throws CoreException {
    fLaunchConfigs = new HashMap<ILaunchConfiguration, RandoopLaunchConfiguration>();

    ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
    ILaunchConfigurationType randoopLaunchType = launchManager.getLaunchConfigurationType(
        IRandoopLaunchConfigurationConstants.ID_RANDOOP_TEST_GENERATION);
    ILaunchConfiguration[] launchConfigs = launchManager.getLaunchConfigurations();
    for (ILaunchConfiguration config : launchConfigs) {
      try {
        if (config.getType().equals(randoopLaunchType)) {
          addLaunchConfiguration(config);
        }
      } catch (CoreException e) {
        // Unable to retrieve or instantiate this config's type.
      }
    }
  }
  
  public static void initialize() {
    if (instance == null) {
      try {
        instance = new RandoopLaunchConfigurationStore();
      } catch (CoreException e) {
        instance = null;
        RandoopPlugin.log(e);
      }
    }
  }
  
  public static RandoopLaunchConfigurationStore getInstance() {
    if (instance == null)
      initialize();

    return instance;
  }
  
  public boolean addLaunchConfiguration(ILaunchConfiguration config) {
    if (!fLaunchConfigs.containsKey(config)) {
      fLaunchConfigs.put(config, new RandoopLaunchConfiguration(config));
      return true;
    } else {
      return false;
    }
  }
}
