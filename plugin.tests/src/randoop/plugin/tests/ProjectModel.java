package randoop.plugin.tests;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swtbot.swt.finder.utils.internal.Assert;

public class ProjectModel {
  String fProjectName;
  File fProjectDirectory;

  // Which of the contained directories are source folders
  List<String> fSourceFolders;

  /**
   * 
   * @param name
   *          name of java project to be created in the workspace
   * @param path
   *          relative path in test bundle
   */
  public ProjectModel(String name, File projectDirectory) {
    Assert.isLegal(name != null);
    Assert.isLegal(projectDirectory != null);
    Assert.isLegal(projectDirectory.exists());
    Assert.isLegal(projectDirectory.isDirectory());

    fProjectName = name;
    fProjectDirectory = projectDirectory;

    fSourceFolders = new ArrayList<String>();
  }

  public void addSourceFolder(String relativePath) {
    File sourceDir = new File(fProjectDirectory, relativePath);
    Assert.isLegal(sourceDir.exists());
    Assert.isLegal(sourceDir.isDirectory());

    fSourceFolders.add(relativePath);
  }

  public String getProjectName() {
    return fProjectName;
  }

  public File getProjectDirectory() {
    return fProjectDirectory;
  }

  public String[] getSourceFolders() {
    return fSourceFolders.toArray(new String[fSourceFolders.size()]);
  }

}
