package randoop.plugin.internal.ui.views;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ui.PlatformUI;

import randoop.plugin.RandoopPlugin;
import randoop.plugin.internal.core.runtime.IMessageListener;
import randoop.runtime.CreatedJUnitFile;
import randoop.runtime.ErrorRevealed;
import randoop.runtime.IMessage;
import randoop.runtime.PercentDone;
import randoop.runtime.RandoopStarted;

public class MessageViewListener implements IMessageListener {
  private TestGeneratorViewPart fViewPart;

  public MessageViewListener(TestGeneratorViewPart viewPart) {
    fViewPart = viewPart;
  }

  @Override
  public void handleMessage(IMessage m) {
    if (m instanceof RandoopStarted) {
      PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
          @Override
          public void run() {
            fViewPart.getProgressBar().start();
            fViewPart.getCounterPanel().reset();
            fViewPart.randoopErrors.reset();
            
          }
        });
    } else if (m instanceof PercentDone) {
      final PercentDone p = (PercentDone)m;
      PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
        @Override
        public void run() {
          fViewPart.getProgressBar().setPercentDone(p.getPercentDone());
          fViewPart.getCounterPanel().numSequences(p.getSequencesGenerated());
        }
      });
    } else if (m instanceof ErrorRevealed) {
      final ErrorRevealed err = (ErrorRevealed)m;
      PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
        @Override
        public void run() {
          fViewPart.getProgressBar().error();
          fViewPart.getCounterPanel().errors();
          fViewPart.randoopErrors.add(err);          
        }
      });
    } else if (m instanceof CreatedJUnitFile) {
      final CreatedJUnitFile fileCreated = (CreatedJUnitFile) m;
      final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
      
      PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
        @Override
        public void run() {
          // Only worry about driver files
          if (!fileCreated.isDriver())
            return;
          
          try {
            File f = fileCreated.getFile();
            IPath path = new Path(f.toString());
            
            IProject project = root.getProject(path.segment(0));
            Assert.isTrue(project.exists());

            IJavaProject javaProject = (IJavaProject) project.getNature(JavaCore.NATURE_ID);
            Assert.isNotNull(javaProject);
            
            // Search for the package fragment root which is containing this JUnit file
            // so that we can quickly perform a refresh and see the new file
            IPackageFragmentRoot outputPfr = null;
            int matchingSegmentCount = 0;
            for (IPackageFragmentRoot pfr : javaProject.getPackageFragmentRoots()) {
              if(pfr.getKind() == IPackageFragmentRoot.K_SOURCE) {
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
            outputPfr.getCorrespondingResource().refreshLocal(IResource.DEPTH_INFINITE, null);
            
            IResource driverResource = root.findMember(path);

            if (driverResource != null) {
              Assert.isTrue(driverResource.getProject().equals(javaProject.getProject()));
              IJavaElement driverElement = JavaCore.create(driverResource, javaProject);
              Assert.isTrue(driverElement instanceof ICompilationUnit);

              fViewPart.setDriver((ICompilationUnit) driverElement);
            } else {
              // TODO root may be null. If it is, notify user that the given file was not found.
              
            }
          } catch (CoreException e) {
            RandoopPlugin.log(e);
          }
        }
      });
    }
  }

  @Override
  public void handleTermination() {
    PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
      @Override
      public void run() {
        fViewPart.getProgressBar().stop();
      }
    });
  }
}
