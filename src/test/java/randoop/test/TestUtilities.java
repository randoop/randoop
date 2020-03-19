package randoop.test;

/** Utilities for tests. */
public class TestUtilities {

  public static List<TypedOperation> getConcreteOperations(List<Class<?>> classes) {
    final List<TypedOperation> operations = new ArrayList<>();
    final List<TypedOperation> omittedOperations = new ArrayList<>();
    VisibilityPredicate visibility = IS_PUBLIC;
    ReflectionManager mgr = new ReflectionManager(visibility);
    for (Class<?> c : classes) {
      ClassOrInterfaceType classType = ClassOrInterfaceType.forClass(c);
      final OperationExtractor extractor =
          new OperationExtractor(classType, new DefaultReflectionPredicate(), visibility);
      mgr.apply(extractor, c);
      operations.addAll(extractor.getOperations());
      omittedOperations.addAll(extractor.getOmittedOperations());
    }
    final List<TypedOperation> operationsFiltered =
        OmitMethodsPredicate.removeOverriddenOmitted(operations, omittedOperations);
    return operationsFiltered;
  }
}
