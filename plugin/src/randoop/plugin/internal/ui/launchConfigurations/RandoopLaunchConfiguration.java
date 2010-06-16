package randoop.plugin.internal.ui.launchConfigurations;

import org.eclipse.debug.core.ILaunchConfiguration;


public class RandoopLaunchConfiguration {
  ILaunchConfiguration fConfig;

  public RandoopLaunchConfiguration(ILaunchConfiguration config) {
    fConfig = config;
  }
  
  public RandoopArgumentCollector getArguments() {
    return new RandoopArgumentCollector(fConfig);
  }
}
