package randoop.output;

import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import java.util.Set;

/** Visitor for Class types in JavaParser AST. */
public class ClassTypeVisitor extends VoidVisitorAdapter<Set<ClassOrInterfaceType>> {
  /**
   * If the class or interface type is in a package that's not visible by default, add the type to
   * the set of types that is passed in as an argument. For instance, suppose that the type {@code
   * org.apache.commons.lang3.MutablePair} appears in the program. This type will be added to the
   * set of types. This is used for type name simplifications to simplify {@code
   * org.apache.commons.lang3.MutablePair} into {@code MutablePair} after adding the import
   * statement {@code import org.apache.commons.lang3.MutablePair; }.
   *
   * @param params a set of {@code Type} objects; will be modified if the class or interface type is
   *     a non-visible type by default
   */
  @SuppressWarnings("unchecked")
  @Override
  public void visit(ClassOrInterfaceType n, Set<ClassOrInterfaceType> params) {

    // If the class type is a generic types, visit each one of the
    // parameter types as well.

    if (n.getTypeArguments().isPresent()) {
      for (Type argType : n.getTypeArguments().get()) {
        ReferenceType rType = (ReferenceType) argType;
        if (rType instanceof ClassOrInterfaceType) {
          this.visit((ClassOrInterfaceType) rType, params);
        }
      }
    }

    // Add the type to the set if it's not a visible type be default.
    if (n.getScope() != null) {
      // Add a copy, so that modifying removing the scope later won't
      // affect this instance which is used for comparisons only.
      params.add(n.clone());
    }
  }
}
