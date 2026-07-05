package randoop.output;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import java.util.Optional;

/** JavaParser visitor to simplify field access in a test method. */
public class FieldAccessTypeNameSimplifyVisitor extends VoidVisitorAdapter<ClassOrInterfaceType> {

  /** An instance of a Java parser. */
  private static final JavaParser javaParser = new JavaParser();

  /** Creates a FieldAccessTypeNameSimplifyVisitor. */
  public FieldAccessTypeNameSimplifyVisitor() {}

  /**
   * Visit every field access expression. Simplify the type name by removing the scope component if
   * the visited object is of the same type as that contained in the argument that is passed in.
   *
   * @param type a {@code ClassOrInterfaceType} object
   */
  @Override
  public void visit(FieldAccessExpr n, ClassOrInterfaceType type) {
    // FieldAccessExpr.getScope() returns a non-null Expression, but ClassOrInterfaceType.getScope()
    // returns an Optional, so its presence must be tested and its contents unwrapped.
    Optional<ClassOrInterfaceType> typeScope = type.getScope();
    if (typeScope.isPresent()
        && n.getScope().toString().equals(typeScope.get() + "." + type.getName())) {
      String typeName = type.getName().toString();
      ParseResult<Expression> parseExpression = javaParser.parseExpression(typeName);
      if (!parseExpression.isSuccessful()) {
        // TODO: Could show the diagnostics, but it might be obvious.
        throw new Error("Problem parsing expression: " + typeName);
      }
      // Set scope to be just the name of the type.
      n.setScope(parseExpression.getResult().get());
    }
  }
}
