package randoop.plugin.internal.ui.buildpath;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.ui.text.java.ClasspathFixProcessor;

/**
 * 
 * @author Peter Kalauskas
 */
public class RandoopClasspathFixProcessor extends ClasspathFixProcessor {
  
  @Override
  public ClasspathFixProposal[] getFixImportProposals(IJavaProject project, String missingType) throws CoreException {

    if (missingType.startsWith("randoop.") || missingType.equals("CheckRep")) { //$NON-NLS-1$ //$NON-NLS-2$
      ClasspathFixProposal[] proposals = new ClasspathFixProposal[1];

      proposals[0] = new RandoopClasspathFixProposal(project, 15);

      return proposals;
    }
    return null;
  }
  
}
