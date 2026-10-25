package randoop.output;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import java.util.Optional;

/** JavaParser visitor to simplify type names in method calls. */
public class MethodTypeNameSimplifyVisitor extends VoidVisitorAdapter<ClassOrInterfaceType> {

  /** An instance of a Java parser. */
  private static final JavaParser javaParser = new JavaParser();

  /** Creates a MethodTypeNameSimplifyVisitor. */
  public MethodTypeNameSimplifyVisitor() {}

  /**
   * Visit every method call expression. Simplify the type name by removing the scope component if
   * the visited object is of the same type as that contained in the argument that is passed in.
   *
   * @param type a {@code ClassOrInterfaceType} object
   */
  @Override
  public void visit(MethodCallExpr methodCallExpr, ClassOrInterfaceType type) {
    // In JavaParser, MethodCallExpr.getScope() and ClassOrInterfaceType.getScope() both return an
    // Optional, so their presence must be tested and their contents unwrapped before comparison.
    Optional<Expression> callScope = methodCallExpr.getScope();
    Optional<ClassOrInterfaceType> typeScope = type.getScope();
    if (callScope.isPresent()
        && typeScope.isPresent()
        && callScope.get().toString().equals(typeScope.get() + "." + type.getName())) {
      String typeName = type.getName().toString();
      ParseResult<Expression> parseExpression = javaParser.parseExpression(typeName);
      if (!parseExpression.isSuccessful()) {
        // TODO: Could show the diagnostics, but it might be obvious.
        throw new Error("Problem parsing expression: " + typeName);
      }
      // Set scope to be just the name of the type.
      methodCallExpr.setScope(parseExpression.getResult().get());
    }
  }
}
