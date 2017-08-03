package randoop.output;

import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import java.util.Set;

/**
 * Visit every variable declaration. Adds to a set of strings for all the names of variables that
 * are either primitive or wrapped types.
 */
public class PrimitiveAndWrappedTypeVarNameCollector extends VoidVisitorAdapter<Set<String>> {
  /**
   * Visit every variable declaration.
   *
   * @param variableNames a set containing the names of all the variables that are of primitive or
   *     wrapped types
   */
  @SuppressWarnings("unchecked")
  @Override
  public void visit(VariableDeclarationExpr n, Set<String> variableNames) {

    ClassOrInterfaceType classType = null;
    if (n.getType() instanceof ReferenceType) {
      ReferenceType rType = (ReferenceType) n.getType();
      if (rType.getType() instanceof ClassOrInterfaceType) {
        classType = (ClassOrInterfaceType) rType.getType();
      }
    }

    // Check if the variable's type is a primitive or a wrapped type.
    if (n.getType() instanceof PrimitiveType || (classType != null && classType.isBoxedType())) {
      for (VariableDeclarator vd : n.getVars()) {
        variableNames.add(vd.getId().getName());
      }
    }
  }
}
