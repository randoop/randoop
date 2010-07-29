package randoop.plugin;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import randoop.plugin.internal.core.TestGroupResources;

/**
 * The activator class controls the plug-in life cycle. It stores a shared
 * instance of the plug-in and provides a static method to access it.
 * <code>RandoopActivator</code> also provides static convenience methods for
 * logging statuses and exceptions and for accessing the <code>Shell</code> that
 * the shared instance is running in.
 */
public class RandoopPlugin extends AbstractUIPlugin {
  /** The plug-in's unique identifier */
  public static final String PLUGIN_ID = "randoop"; //$NON-NLS-1$

  // TODO: Use archives when building update site
  //  private static final IPath RANDOOP_JAR = new Path("/randoop.jar"); //$NON-NLS-1$
  //
    private static final IPath PLUME_JAR = new Path("/plume.jar"); //$NON-NLS-1$

  private static final IPath RANDOOP_JAR = new Path("../bin/"); //$NON-NLS-1$

  // private static final IPath PLUME_JAR = new Path("../lib/plume.jar"); //$NON-NLS-1$
  
  /** The shared instance */
  private static RandoopPlugin plugin = null;

  /**
   * Indicator of when the shared instance is stopped. This is not reset when
   * <code>stop</code> is called
   */
  private static boolean isStopped = false;

  /**
   * Constructs the plug-in and sets the shared instance to <code>this</code>.
   */
  public RandoopPlugin() {
    plugin = this;
  }
  
  /**
   * Returns the shared instance of this plug-in.
   * 
   * @return the shared instance
   */
  public static RandoopPlugin getDefault() {
    return plugin;
  }

  /**
   * Starts up this plug-in and sets <code>this</code> to be the shared
   * instance.
   * 
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);
    plugin = this;
  }

  /**
   * Stops this plug-in and sets the shared instance to <code>null</code>.
   * 
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop(BundleContext context) throws Exception {
    // Remove all files from the temp folder in the state location
    TestGroupResources.clearTempLocation();
    
    plugin = null;
    isStopped = true;
    
    super.stop(context);
  }

  /**
   * Returns an image descriptor for the image file at the given plug-in
   * relative path.
   * 
   * @param path
   *          the path
   * @return the image descriptor
   */
  public static ImageDescriptor getImageDescriptor(String path) {
    return imageDescriptorFromPlugin(PLUGIN_ID, path);
  }

  /**
   * Returns <code>true</code> if the share instance is stopped.
   * 
   * @return <code>true</code> if <code>stop</code> has been called prior
   */
  public static boolean isStopped() {
    return isStopped;
  }

  /**
   * Convenience method that returns the plug-in's unique identifier.
   * 
   * @return the plug-in's ID, also <code>PLUGIN_ID</code>
   */
  public static String getPluginId() {
    return PLUGIN_ID;
  }

  /**
   * Logs a status with the specified <code>Exception</code> in the shared
   * instance's log. The status logged will use a severity of
   * <code>IStatus.ERROR</code>.
   * 
   * @param e
   *          the <code>Exception</code> to log
   */
  public static void log(Throwable e) {
    log(new Status(IStatus.ERROR, getPluginId(), IStatus.ERROR, "Error", e)); //$NON-NLS-1$
  }
  
  public static void log(Throwable e, String message) {
    log(new Status(IStatus.ERROR, getPluginId(), IStatus.ERROR, message, e));
  }

  /**
   * Logs a status in the shared instance's log
   * 
   * @param status
   *          the status to log
   * 
   * @see org.eclipse.core.runtime.ILog#log(IStatus)
   */
  public static void log(IStatus status) {
    getDefault().getLog().log(status);
  }

  /**
   * Returns the active workbench shell.
   * 
   * @return the active workbench shell
   */
  public static Shell getActiveWorkbenchShell() {
    IWorkbenchWindow workBenchWindow = getActiveWorkbenchWindow();
    if (workBenchWindow == null)
      return null;
    return workBenchWindow.getShell();
  }
  
  /**
   * Returns the active workbench window.
   * 
   * @return the active workbench window
   */
  public static IWorkbenchWindow getActiveWorkbenchWindow() {
    if (plugin == null)
      return null;
    IWorkbench workBench = plugin.getWorkbench();
    if (workBench == null)
      return null;
    return workBench.getActiveWorkbenchWindow();
  }

  /**
   * Returns the full path to the randoop.jar runtime archive.
   * 
   * @return full path to randoop.jar, or <code>null</code> if no the
   *         <code>IPath</code> could not be created
   */
  public static IPath getRandoopJar() {
    return getFullPath(RANDOOP_JAR);
  }

  /**
   * Returns the full path to the plume.jar runtime archive.
   * 
   * @return full path to plume.jar, or <code>null</code> if no the
   *         <code>IPath</code> could not be created
   */
  public static IPath getPlumeJar() {
    return getFullPath(PLUME_JAR);
  }

  /**
   * Returns the full path to the the given Path.
   * 
   * @return local path to the , or <code>null</code> if no the
   *         <code>IPath</code> could not be created
   */
  private static IPath getFullPath(IPath localPath) {
    URL url = FileLocator.find(getDefault().getBundle(), localPath, null);
    try {
      url = FileLocator.toFileURL(url);
    } catch (IOException e) {
      return null;
    }
    return new Path(url.getPath());
  }
  
  /**
   * Returns the current display, or the default display if the currently
   * running thread is not a user-interface thread for any display.
   * 
   * @see org.eclipse.swt.widgets.Display#getCurrent()
   * @see org.eclipse.swt.widgets.Display#getDefault()
   */
  public static Display getDisplay() {
    Display display;
    display = Display.getCurrent();
    if (display == null)
      display = Display.getDefault();
    return display;
  }

  public static IWorkspaceRoot getWorkspaceRoot() {
    return ResourcesPlugin.getWorkspace().getRoot();
  }
  
}
