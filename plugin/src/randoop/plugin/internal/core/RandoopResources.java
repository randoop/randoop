package randoop.plugin.internal.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import randoop.plugin.RandoopPlugin;

public class RandoopResources {
  private static URI pluginDirectory;
  private static IPath pluginPath;

  static {
    try {
      pluginDirectory = FileLocator
          .toFileURL(
              Platform.getBundle(RandoopPlugin.getPluginId()).getEntry("/")).toURI(); //$NON-NLS-1$
      pluginPath = Path.fromOSString(new File(pluginDirectory).getAbsolutePath());
    } catch (URISyntaxException e) {
      pluginDirectory = null;
    } catch (IOException e) {
      pluginDirectory = null;
    }
  }
  
  public static IPath getFullPath(String string) {
    return getFullPath(new Path(string));
  }

  /**
   * 
   * @param relativePath
   *          path to the file relative to the plugins base directory
   * @return the absolute path to the resource, or <code>null</code> if
   *         <code>RandoopResources</code> was initialized improperly
   */
  public static IPath getFullPath(IPath path) {
    if (pluginDirectory == null)
      return null;

    return pluginPath.append(path);
  }
  
  /**
   * Copies a file from a source to the supplied destination
   * 
   * XXX This isn't built into the API somewhere?
   * 
   * @param source
   *          file to be copied
   * @param destination
   *          location of new file
   * @param monitor
   * @throws IOException
   */
  public static void copyFile(File source, File destination,
      IProgressMonitor monitor) throws IOException {
    // Create the directories and file for dest
    new File(destination.getParent()).mkdirs();
    destination.createNewFile();

    InputStream in = new FileInputStream(source);
    OutputStream out = new FileOutputStream(destination);

    byte[] buffer = new byte[1024];
    int length;

    while ((length = in.read(buffer)) > 0) {
      out.write(buffer, 0, length);
      if (monitor != null) {
        monitor.worked(1);
      }
    }

    in.close();
    out.close();
  }
}
