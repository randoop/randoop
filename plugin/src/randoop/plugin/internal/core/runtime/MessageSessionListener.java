package randoop.plugin.internal.core.runtime;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import randoop.plugin.RandoopPlugin;
import randoop.plugin.internal.core.RandoopStatus;
import randoop.runtime.CreatedJUnitFile;
import randoop.runtime.ErrorRevealed;
import randoop.runtime.IMessage;
import randoop.runtime.PercentDone;
import randoop.runtime.RandoopFinished;
import randoop.runtime.RandoopStarted;

public class MessageSessionListener implements IMessageListener {
  TestGeneratorSession fSession;

  public MessageSessionListener(TestGeneratorSession session) {
    fSession = session;
  }

  public void handleMessage(IMessage m) {
    if (m instanceof RandoopStarted) {
      fSession.start();
    } else if (m instanceof PercentDone) {
      PercentDone p = (PercentDone) m;
      
      fSession.setPercentDone(p.getPercentDone());
      fSession.setSequenceCount(p.getSequencesGenerated());
    } else if (m instanceof ErrorRevealed) {
      ErrorRevealed err = (ErrorRevealed) m;

      fSession.addRevealedError(err);
    } else if (m instanceof RandoopFinished) {
      fSession.stop(false);
    } else if (m instanceof CreatedJUnitFile) {
      final CreatedJUnitFile fileCreated = (CreatedJUnitFile) m;
      final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

      // Only worry about driver files
      if (!fileCreated.isDriver()) {
        return;
      }

      File f = fileCreated.getFile();

      IPath path = new Path(f.toString());
      if (root.getLocation().isPrefixOf(path)) {
        path = path.removeFirstSegments(root.getLocation().segmentCount());
        path = path.setDevice(null);
      }

      String projectName = path.segment(0);
      IProject project = root.getProject();

      IJavaProject javaProject = JavaCore.create(project);
      if (javaProject == null || !javaProject.exists()) {
        IStatus s = RandoopStatus.NO_JAVA_PROJECT.getStatus(projectName, null);
        RandoopPlugin.log(s);
      } else {
        try {
          // Search for the package fragment root which is containing this
          // JUnit file so that we can quickly perform a refresh and see the new
          // file
          javaProject.getProject().refreshLocal(IResource.DEPTH_ONE, null);
          IPackageFragmentRoot outputPfr = null;
          int matchingSegmentCount = 0;
          for (IPackageFragmentRoot pfr : javaProject.getPackageFragmentRoots()) {
            if (pfr.getKind() == IPackageFragmentRoot.K_SOURCE) {
              IPath pfrPath = pfr.getPath();
              if (pfrPath.isPrefixOf(path)) {
                int newMatchingSegmentCount = pfrPath.segmentCount();
                if (matchingSegmentCount < newMatchingSegmentCount) {
                  matchingSegmentCount = newMatchingSegmentCount;
                  outputPfr = pfr;
                }
              }
            }
          }

          Assert.isNotNull(outputPfr);
          outputPfr.getCorrespondingResource().refreshLocal(IResource.DEPTH_INFINITE,
              null);

          IResource driverResource = root.findMember(path);

          if (driverResource != null) {
            Assert.isTrue(driverResource.getProject().equals(javaProject.getProject()));
            IJavaElement driverElement = JavaCore.create(driverResource, javaProject);
            Assert.isTrue(driverElement instanceof ICompilationUnit);

            fSession.setJUnitDriver((ICompilationUnit) driverElement);
          } else {
            // TODO root may be null. If it is, notify user that the given
            // file was not found.

          }
        } catch (JavaModelException e) {
          IStatus s = RandoopStatus.JAVA_MODEL_EXCEPTION.getStatus(e);
          RandoopPlugin.log(s);
        } catch (CoreException e) {
          IStatus s = RandoopStatus.RESOURCE_REFRESH_EXCEPTION.getStatus(e);
          RandoopPlugin.log(s);
        }
      }
    }
  }

  public void handleTermination() {
    RandoopPlugin.getDisplay().syncExec(new Runnable() {
      public void run() {
        fSession.stop(true);
      }
    });
  }
}
