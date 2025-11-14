package randoop.types;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import org.junit.Test;
import org.plumelib.util.CollectionsPlume;

public class ClassOrInterfaceTypeTest {

  // protected @Nullable ClassOrInterfaceType enclosingType = null;
  // public static ClassOrInterfaceType forClass(Class<?> classType) {
  // public static ClassOrInterfaceType forType(java.lang.reflect.Type type) {
  // public boolean equals(@Nullable Object obj) {
  // public int hashCode() {
  // public abstract ClassOrInterfaceType substitute(Substitution substitution);
  // public abstract ClassOrInterfaceType applyCaptureConversion();
  // public String getSimpleName() {
  // public @Nullable String getCanonicalName() {
  // public @Nullable String getFqName() {
  // public String getBinaryName() {
  // public String getUnqualifiedBinaryName() {
  // public abstract List<ClassOrInterfaceType> getInterfaces();
  // public @Nullable Package getPackage() {
  // public abstract NonParameterizedType getRawtype();
  // public @Nullable InstantiatedType getMatchingSupertype(GenericClassType goalType) {
  // public @Nullable Substitution getInstantiatingSubstitution(ReferenceType goalType) {
  // public abstract ClassOrInterfaceType getSuperclass();

  // public Collection<ClassOrInterfaceType> getSuperTypes() {

  @Test
  public void getSuperTypesTest() {
    ClassOrInterfaceType cType = ClassOrInterfaceType.forClass(C.class);
    Collection<ClassOrInterfaceType> cSupers = cType.getSuperTypes();
    if (cSupers.size() != 6) {
      throw new Error("Expected size 6: " + cSupers);
    }
    assertTrue(CollectionsPlume.hasNoDuplicates(new ArrayList<>(cSupers)));

    ClassOrInterfaceType dType = ClassOrInterfaceType.forClass(D.class);
    Collection<ClassOrInterfaceType> dSupers = dType.getSuperTypes();
    if (dSupers.size() != 7) {
      throw new Error("Expected size 7: " + dSupers);
    }
    assertTrue(CollectionsPlume.hasNoDuplicates(new ArrayList<>(dSupers)));

    ClassOrInterfaceType objectType = ClassOrInterfaceType.forClass(Object.class);
    assertTrue(objectType.getSuperTypes().isEmpty());

    assertFalse(true); // for testing
  }

  @Test
  public void getSuperTypesNonstrictTest() {
    ClassOrInterfaceType cType = ClassOrInterfaceType.forClass(C.class);
    Collection<ClassOrInterfaceType> cSupers = cType.getSuperTypesNonstrict();
    if (cSupers.size() != 7) {
      throw new Error("Expected size 7: " + cSupers);
    }
    assertTrue(CollectionsPlume.hasNoDuplicates(new ArrayList<>(cSupers)));

    ClassOrInterfaceType dType = ClassOrInterfaceType.forClass(D.class);
    Collection<ClassOrInterfaceType> dSupers = dType.getSuperTypes();
    if (dSupers.size() != 8) {
      throw new Error("Expected size 8: " + dSupers);
    }
    assertTrue(CollectionsPlume.hasNoDuplicates(new ArrayList<>(dSupers)));

    ClassOrInterfaceType objectType = ClassOrInterfaceType.forClass(Object.class);
    assertEquals(1, objectType.getSuperTypesNonstrict().size())

    assertFalse(true); // for testing
  }

  // public List<ClassOrInterfaceType> getImmediateSupertypes() {
  // public Collection<ClassOrInterfaceType> getAllSupertypesInclusive() {
  // public abstract boolean isAbstract();
  // public boolean isGeneric(boolean ignoreWildcards) {
  // public boolean isInstantiationOf(ReferenceType otherType) {
  // public final boolean isNestedClass() {
  // public final boolean isMemberClass() {
  // public boolean isParameterized() {
  // public abstract boolean isStatic();
  // public boolean isSubtypeOf(Type otherType) {
  // public boolean hasWildcard() {
  // public boolean hasCaptureVariable() {
  // public List<TypeArgument> getTypeArguments() {
  // public List<TypeVariable> getTypeParameters() {
  // public boolean isClassOrInterfaceType() {

  // ///////////////////////////////////////////////////////////////////////////
  // Classes used within tests
  //

  interface I {}

  interface J extends I {}

  interface K extends I {}

  class A {}

  class B extends A implements J {}

  class C extends B implements K {}

  class D extends C {}
}
