package randoop.output;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

/** JavaParser visitor to simplify field access in a test method. */
public class FieldAccessTypeNameSimplifyVisitor extends VoidVisitorAdapter<ClassOrInterfaceType> {
  /**
   * Visit every field access expression. Simplify the type name by removing the scope component if
   * the visited object is of the same type as that contained in the argument that is passed in.
   *
   * @param type a {@code ClassOrInterfaceType} object
   */
  @Override
  public void visit(FieldAccessExpr n, ClassOrInterfaceType type) {
    if (n.getScope() != null
        && type.getScope() != null
        && n.getScope().toString().equals(type.getScope() + "." + type.getName())) {
      try {
        // Set scope to be just the name of the type.
        n.setScope(JavaParser.parseExpression(type.getName()));
      } catch (ParseException e) {
        // Error parsing type name.
        throw new Error(e);
      }
    }
  }
}
