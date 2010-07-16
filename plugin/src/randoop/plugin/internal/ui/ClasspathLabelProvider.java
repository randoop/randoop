package randoop.plugin.internal.ui;

import java.io.File;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.debug.ui.JavaDebugImages;
import org.eclipse.jdt.internal.debug.ui.classpath.ClasspathEntry;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE.SharedImages;

public class ClasspathLabelProvider extends LabelProvider {

  /**
   * The <code>LabelProvider</code> implementation of this
   * <code>ILabelProvider</code> method returns <code>null</code>. Subclasses
   * may override.
   */
  @Override
  public Image getImage(Object element) {
    if (element instanceof IClasspathEntry) {
      IClasspathEntry entry = (IClasspathEntry) element;

      switch (entry.getEntryKind()) {
      case IClasspathEntry.CPE_SOURCE:
        return JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_PACKFRAG_ROOT);
      case IClasspathEntry.CPE_LIBRARY:
        IPath workspaceLocation = ResourcesPlugin.getWorkspace().getRoot().getLocation();

        IPath path = entry.getPath();
        boolean external = path.uptoSegment(workspaceLocation.segmentCount()).equals(workspaceLocation);
        boolean source = (entry.getSourceAttachmentPath() != null && !Path.EMPTY.equals(entry.getSourceAttachmentPath()));
        String key = null;
        if (external) {
          if (path != null) {
            File file = path.toFile();
            if (file.exists() && file.isDirectory()) {
              key = ISharedImages.IMG_OBJS_PACKFRAG_ROOT;
            } else if (source) {
              key = ISharedImages.IMG_OBJS_EXTERNAL_ARCHIVE_WITH_SOURCE;
            } else {
              key = ISharedImages.IMG_OBJS_EXTERNAL_ARCHIVE;
            }
          }
        } else {
          if (source) {
            key = ISharedImages.IMG_OBJS_JAR_WITH_SOURCE;
          } else {
            key = ISharedImages.IMG_OBJS_JAR;
          }
        }
        return JavaUI.getSharedImages().getImage(key);
      case IClasspathEntry.CPE_PROJECT:
        return PlatformUI.getWorkbench().getSharedImages().getImage(SharedImages.IMG_OBJ_PROJECT_CLOSED);
      case IClasspathEntry.CPE_VARIABLE:
        return DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_ENV_VAR);
      case IClasspathEntry.CPE_CONTAINER:
        return JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_LIBRARY);
      }
    }

    return null;
  }

  /**
   * The <code>LabelProvider</code> implementation of this
   * <code>ILabelProvider</code> method returns the element's
   * <code>toString</code> string. Subclasses may override.
   */
  @Override
  public String getText(Object element) {
    if (element instanceof IClasspathEntry) {
      IClasspathEntry entry = (IClasspathEntry) element;

      switch (entry.getEntryKind()) {
      case IClasspathEntry.CPE_SOURCE:
      case IClasspathEntry.CPE_LIBRARY:
      case IClasspathEntry.CPE_PROJECT:
        return entry.getPath().lastSegment();
      case IClasspathEntry.CPE_VARIABLE:
      case IClasspathEntry.CPE_CONTAINER:
        return entry.getPath().toString();
      }
    }
    return super.getText(element);
  }
}
