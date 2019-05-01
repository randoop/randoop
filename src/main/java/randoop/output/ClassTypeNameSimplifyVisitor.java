package randoop.output;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import java.util.Optional;

/** JavaParser Visitor to simplify type names. */
public class ClassTypeNameSimplifyVisitor extends VoidVisitorAdapter<ClassOrInterfaceType> {
  /**
   * Visit every class or interface type. Simplify the type name by removing the scope component if
   * the visited object is of the same type as that contained in the argument that is passed in.
   *
   * @param type a {@code ClassOrInterfaceType} object
   */
  @Override
  public void visit(ClassOrInterfaceType classType, ClassOrInterfaceType type) {

    // Remove the scope component of the type.
    if (classType.getScope() != null
        && type.getScope() != null
        && type.getScope().equals(classType.getScope())
        && type.getName().equals(classType.getName())) {
      classType.setScope(null);
    }

    // If the class type is a generic types, visit each one of the
    // parameter types as well.

    Optional<NodeList<Type>> oTypes = classType.getTypeArguments();
    if (oTypes.isPresent()) {
      for (Type argType : oTypes.get()) {
        ReferenceType rType = (ReferenceType) argType;
        if (rType instanceof ClassOrInterfaceType) {
          this.visit((ClassOrInterfaceType) rType, type);
        }
      }
    }
  }
}
