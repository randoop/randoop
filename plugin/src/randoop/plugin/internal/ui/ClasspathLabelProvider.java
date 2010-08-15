package randoop.plugin.internal.ui;

import java.io.File;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE.SharedImages;

import randoop.plugin.RandoopPlugin;
import randoop.plugin.internal.core.RandoopStatus;

public class ClasspathLabelProvider extends LabelProvider {
  private IJavaProject fJavaProject;
  
  public ClasspathLabelProvider(IJavaProject javaProject) {
    fJavaProject = javaProject;
  }
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
      IClasspathEntry cpentry = (IClasspathEntry) element;

      switch (cpentry.getEntryKind()) {
      case IClasspathEntry.CPE_SOURCE:
      case IClasspathEntry.CPE_LIBRARY:
      case IClasspathEntry.CPE_PROJECT:
        return cpentry.getPath().lastSegment();
      case IClasspathEntry.CPE_VARIABLE:
        return getText(JavaCore.getResolvedClasspathEntry(cpentry));
      case IClasspathEntry.CPE_CONTAINER:
        try {
          IClasspathContainer cpcontainer = JavaCore.getClasspathContainer(cpentry.getPath(), fJavaProject);
          return cpcontainer.getDescription();
        } catch (JavaModelException e) {
          IStatus s = RandoopStatus.JAVA_MODEL_EXCEPTION.getStatus(e);
          RandoopPlugin.log(s);
          return cpentry.getPath().toString();
        }
      }
    }
    return super.getText(element);
  }
  
}
