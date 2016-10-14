package randoop.reflection;

/**
 * Input class for test inspired by {@code org.dyn4j.BinarySearchTree<E>}.
 * Note: if commit to instantiation of {@code E} for {@code BST<E>} for which there is no
 * implementation of {@code C_BST<E>}
 */
interface C_BST<E extends Comparable<E>> {}

class B_BST implements C_BST<Long> {}

class D_BST implements C_BST<String> {}

public class BST<E extends Comparable<E>> {
  public <T extends C_BST<E>> T m(T c) {
    return null;
  }
}
