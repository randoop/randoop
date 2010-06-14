package randoop.plugin.tests.core;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import randoop.plugin.RandoopPlugin;
import randoop.plugin.internal.core.TestGroupResources;
import junit.framework.TestCase;

public class RandoopResourcesTest extends TestCase {
  /*
   * Deletes the directory
   */
  private void delete(File file) {
    if (file.isDirectory()) {
      for (File f : file.listFiles()) {
        delete(f);
      }
    }
    file.delete();
  }

  @Override
  protected void setUp() throws Exception {
    // Delete the plug-in's temp folder
    delete(TestGroupResources.TEMP_PATH.toFile());
  }
}
