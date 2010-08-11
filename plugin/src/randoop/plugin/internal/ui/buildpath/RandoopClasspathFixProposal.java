package randoop.plugin.internal.ui.buildpath;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.text.java.ClasspathFixProcessor.ClasspathFixProposal;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.swt.graphics.Image;

import randoop.plugin.RandoopPlugin;

public class RandoopClasspathFixProposal extends ClasspathFixProposal {
  
  private final IJavaProject fProject;
  
  private final int fRelevance;

  public RandoopClasspathFixProposal(IJavaProject project, int relevance) {
    fProject = project;
    fRelevance = relevance;
  }

  @Override
  public String getAdditionalProposalInfo() {
    return "Adds the Randoop library to the build path.";
  }
  
  @Override
  public Change createChange(IProgressMonitor monitor) throws CoreException {
    if (monitor == null)
      monitor = new NullProgressMonitor();
    
    monitor.beginTask("Adding Randoop library", 1);
    
    try {
      IClasspathEntry entry = JavaCore.newLibraryEntry(RandoopPlugin.getRandoopJar(), null, null);
      
      IClasspathEntry[] oldEntries = fProject.getRawClasspath();
      ArrayList<IClasspathEntry> newEntries = new ArrayList<IClasspathEntry>(oldEntries.length + 1);
      
      for (int i = 0; i < oldEntries.length; i++) {
        IClasspathEntry curr = oldEntries[i];
        
        // Check if Randoop is already in the build path
        if (curr.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
          IPath path = curr.getPath();
          if (path.equals(entry.getPath())) {
            return new NullChange();
          }
        }
        
        if (curr != null) {
          newEntries.add(curr);
        }
      }
      
      // add Randoop.jar
      newEntries.add(entry);
      
      // Convert newEntries to an array
      IClasspathEntry[] newCPEntries = (IClasspathEntry[]) newEntries.toArray(new IClasspathEntry[newEntries.size()]);
      
      Change newClasspathChange = newClasspathChange(fProject, newCPEntries, fProject.getOutputLocation());
      if (newClasspathChange != null) {
        return newClasspathChange;
      }
    } finally {
      monitor.done();
    }
    
    return new NullChange();
  }

  @Override
  public String getDisplayString() {
    return "Add Randoop library to the build path";
  }

  @Override
  public Image getImage() {
    return JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_EXTERNAL_ARCHIVE);
  }

  @Override
  public int getRelevance() {
    return fRelevance;
  }
  
}
