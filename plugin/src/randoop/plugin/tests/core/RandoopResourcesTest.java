package randoop.plugin.tests.core;


import java.io.File;
import java.io.IOException;

import randoop.plugin.internal.core.RandoopResources;
import junit.framework.TestCase;

public class RandoopResourcesTest extends TestCase {
  private static String TEMP_FOLDER = "temp"; //$NON-NLS-1$
  
  @Override
  protected void setUp() throws Exception {
    // Delete the plug-in's temp folder
//    File f = RandoopResources.getPluginBase();
//    new File(f, TEMP_FOLDER).delete();
  }
  
  public void testGetFile() {
//    File f = RandoopResources.getFile("/"); //$NON-NLS-1$
//    System.out.println(f);
//    System.out.println(f.equals(RandoopResources.getPluginBase()));
//    System.out.println(RandoopResources.getPluginBase());
//    assertEquals(RandoopResources.getPluginBase(), f);
//    
//    assertTrue(f.exists());
//    assertFalse(f.isFile());
//    
//    f = new File(f, TEMP_FOLDER);
//    assertFalse(f.exists());
//    assertFalse(f.isFile());
//    
//    f.mkdir();
//    assertTrue(f.exists());
//    f = new File(f, "method.txt"); //$NON-NLS-1$
//    
//    try {
//      assertTrue(f.createNewFile());
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
  }
}
