package randoop.plugin.internal.ui.launchConfigurations;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;

import randoop.plugin.RandoopPlugin;

public class RandoopLaunchConfigurationStore {
  Map<ILaunchConfiguration, RandoopLaunchConfiguration> fLaunchConfigs;

  private static RandoopLaunchConfigurationStore instance;
  
  private RandoopLaunchConfigurationStore() throws CoreException {
    fLaunchConfigs = new HashMap<ILaunchConfiguration, RandoopLaunchConfiguration>();

    ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
    ILaunchConfiguration[] launchConfigs = launchManager.getLaunchConfigurations();
    for (ILaunchConfiguration config : launchConfigs) {
      System.out.println();
      addLaunchConfiguration(config);
    }
  }
  
  private static void initialize() {
    try {
      instance = new RandoopLaunchConfigurationStore();
    } catch (CoreException e) {
      instance = null;
      RandoopPlugin.log(e);
    }
  }

  public boolean addLaunchConfiguration(ILaunchConfiguration config) {
    if (!fLaunchConfigs.containsKey(config)) {
      fLaunchConfigs.put(config, new RandoopLaunchConfiguration(config));
      return true;
    } else {
      return false;
    }
  }
  
  public static RandoopLaunchConfigurationStore getInstance() {
    if (instance == null)
      initialize();

    return instance;
  }
}
