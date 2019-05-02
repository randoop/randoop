package randoop.output;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

/** JavaParser visitor to simplify type names in method calls. */
public class MethodTypeNameSimplifyVisitor extends VoidVisitorAdapter<ClassOrInterfaceType> {
  /**
   * Visit every method call expression. Simplify the type name by removing the scope component if
   * the visited object is of the same type as that contained in the argument that is passed in.
   *
   * @param type a {@code ClassOrInterfaceType} object
   */
  @Override
  public void visit(MethodCallExpr methodCallExpr, ClassOrInterfaceType type) {
    if (methodCallExpr.getScope() != null
        && type.getScope() != null
        && methodCallExpr.getScope().toString().equals(type.getScope() + "." + type.getName())) {
      try {
        // Set scope to be just the name of the type.
        methodCallExpr.setScope(JavaParser.parseExpression(type.getName()));
      } catch (ParseException e) {
        // Error parsing type name.
        throw new Error(e);
      }
    }
  }
}
