package randoop.plugin.internal.ui.launching;

import org.eclipse.debug.core.ILaunchConfiguration;

import randoop.plugin.internal.core.launching.RandoopArgumentCollector;


public class RandoopLaunchConfiguration {
  ILaunchConfiguration fConfig;

  public RandoopLaunchConfiguration(ILaunchConfiguration config) {
    fConfig = config;
  }
  
  public RandoopArgumentCollector getArguments() {
    return new RandoopArgumentCollector(fConfig);
  }
}
