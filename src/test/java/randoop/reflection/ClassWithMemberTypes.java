package randoop.reflection;

/** Used to test whether the member types are being collected from a class. */
public class ClassWithMemberTypes {
  public static class StaticClass {}

  public class InnerClass {}

  public interface MemberInterface {}

  public enum InnerEnum {
    A,
    B;
  }

  static class PackagePrivateStaticClass {}

  class PackagePrivateInnerClass {}

  interface PackagePrivateMemberInterface {}

  enum PackagePrivateInnerEnum {
    A,
    B;
  }
}
