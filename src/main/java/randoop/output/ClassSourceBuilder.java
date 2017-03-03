package randoop.output;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import plume.UtilMDE;
import randoop.Globals;

/**
 * Builds the source code for a (non-generic) class in a string.
 */
public class ClassSourceBuilder extends SourceBuilder {

  /** The name of the class */
  private final String classname;

  /** The package of the class */
  private String packageName;

  /** The imports for the class */
  private List<String> importNames;

  /** The annotations for the class */
  private List<String> classAnnotation;

  /** The members of the class */
  private List<String> memberDeclarations;

  public ClassSourceBuilder(String classname, String packageName) {
    super();
    this.classname = classname;
    this.packageName = packageName;
    this.classAnnotation = new ArrayList<>();
    this.memberDeclarations = new ArrayList<>();
  }

  public void addImports(Collection<String> importNames) {
    if (importNames != null) {
      this.importNames.addAll(importNames);
    }
  }

  public void addClassAnnotation(Collection<String> classAnnotation) {
    if (classAnnotation != null) {
      this.classAnnotation.addAll(classAnnotation);
    }
  }

  public void addMember(String memberDeclaration) {
    if (memberDeclaration != null && !memberDeclaration.isEmpty()) {
      memberDeclarations.add(memberDeclaration);
    }
  }

  @Override
  public String toString() {
    if (packageName != null && !packageName.isEmpty()) {
      appendLine("package", packageName, ";");
    }

    for (String importName : importNames) {
      appendLine("import", importName, ";");
    }

    for (String annotation : classAnnotation) {
      appendLine(annotation);
    }

    appendLine("public", "class", classname, "{");

    for (String memberDeclaration : memberDeclarations) {
      appendLine(memberDeclaration);
    }

    appendLine("}");
    return super.toString();
  }
}
