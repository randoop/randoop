/*******************************************************************************
 *
 * Skeleton activator class for Randoop.
 *
 * Some of the code in this class is based on code from the Eclipse JUnit plugin.
 *
 *******************************************************************************/
package randoop.plugin;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class RandoopActivator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "randoop";

	// The shared instance
	private static RandoopActivator plugin = null;

	private static boolean  fIsStopped = false;
	
	/**
	 * List storing the registered test run listeners
	 */
	private ListenerList/*<TestRunListener>*/ fNewTestRunListeners;

	
	/**
	 * The constructor
	 */
	public RandoopActivator() {
		plugin = this;
		fNewTestRunListeners= new ListenerList();
	}
	

	/**
	 * @return a <code>ListenerList</code> of all <code>TestRunListener</code>s
	 */
	public ListenerList/*<TestRunListener>*/ getNewTestRunListeners() {
		return fNewTestRunListeners;
	}


	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static RandoopActivator getDefault() {
		return plugin;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		fIsStopped= true;
		super.stop(context);
	}


	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	public static boolean isStopped() {
		return fIsStopped;
	}
	
	public static String getPluginId() {
		return PLUGIN_ID;
	}

	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, getPluginId(), IStatus.ERROR, "Error", e)); //$NON-NLS-1$
	}

	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}
	

	public static Shell getActiveWorkbenchShell() {
		IWorkbenchWindow workBenchWindow= getActiveWorkbenchWindow();
		if (workBenchWindow == null)
			return null;
		return workBenchWindow.getShell();
	}
	
	/**
	 * Returns the active workbench window
	 *
	 * @return the active workbench window
	 */
	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		if (plugin == null)
			return null;
		IWorkbench workBench= plugin.getWorkbench();
		if (workBench == null)
			return null;
		return workBench.getActiveWorkbenchWindow();
	}

}
