package randoop.output;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

/** JavaParser visitor to rename classes. */
public class ClassRenamingVisitor extends VoidVisitorAdapter<String[]> {
  /**
   * Rename the overall class to class name + suffix.
   *
   * @param arg a String array where the first element is the class name and the second element is
   *     the suffix that we will append
   */
  @Override
  public void visit(ClassOrInterfaceDeclaration n, String[] arg) {
    String className = arg[0];
    String suffix = arg[1];
    if (className.equals(n.getName())) {
      n.setName(className + suffix);
    }
  }
}
