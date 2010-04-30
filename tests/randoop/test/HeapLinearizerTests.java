package randoop.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;
import randoop.util.HeapLinearizer;
import randoop.util.HeapLinearizer.LinearizationKind;

public class HeapLinearizerTests extends TestCase {

  private static class Node {
    public Node a;
    public Node b;
    public int value;
    public Node(Node a, Node b, int value) {
      this.a = a;
      this.b = b;
      this.value = value;
    }
  }

  private Node tree1() {
    Node n3 = new Node(null, null, 10);
    Node n4 = new Node(null, null, 11);
    Node n2 = new Node(n3, n4, 12);
    Node n5 = new Node(null, null, 13);
    Node n1 = new Node(n2, n5, 14);
    return n1;
  }

  private Node tree2() {
    Node n3 = new Node(null, null, 20);
    Node n4 = new Node(null, null, 21);
    Node n2 = new Node(n3, n4, 22);
    Node n5 = new Node(null, null, 23);
    Node n1 = new Node(n2, n5, 24);
    return n1;
  }

  private Node tree3() {
    Node n3 = new Node(null, null, 10);
    Node n4 = new Node(null, null, 11);
    Node n2 = new Node(n3, null, 12);
    Node n5 = new Node(null, n4, 13);
    Node n1 = new Node(n2, n5, 14);
    return n1;
  }

  private Node tree4() {
    Node n3 = new Node(null, null, 10);
    Node n4 = new Node(null, null, 11);
    Node n2 = new Node(n3, n4, 12);
    Node n5 = new Node(n4, null, 13);
    Node n1 = new Node(n2, n5, 14);
    return n1;
  }

  public void test1() {
    List<Object> tree1Full = HeapLinearizer.linearize(tree1(), LinearizationKind.FULL, false);
    assertEquals(Arrays.asList(new Object[] { "1", "2", "3", "null", "null", "10", "4", "null", "null", "11", "12", "5", "null", "null", "13", "14" }), tree1Full);

    List<Object> tree1Shape = HeapLinearizer.linearize(tree1(), LinearizationKind.SHAPE, false);
    assertEquals(Arrays.asList(new Object[] { "1", "2", "3", "null", "null", "4", "null", "null", "5", "null", "null" }), tree1Shape);

    List<Object> tree2Full = HeapLinearizer.linearize(tree2(), LinearizationKind.FULL, false);
    assertEquals(Arrays.asList(new Object[] { "1", "2", "3", "null", "null", "20", "4", "null", "null", "21", "22", "5", "null", "null", "23", "24" }), tree2Full);

    List<Object> tree2Shape = HeapLinearizer.linearize(tree2(), LinearizationKind.SHAPE, false);
    assertEquals(tree1Shape, tree2Shape);

    List<Object> tree3Full = HeapLinearizer.linearize(tree3(), LinearizationKind.FULL, false);
    assertEquals(Arrays.asList(new Object[] { "1", "2", "3", "null", "null", "10", "null", "12", "4", "null", "5", "null", "null", "11", "13", "14" }), tree3Full);

    List<Object> tree3Shape = HeapLinearizer.linearize(tree3(), LinearizationKind.SHAPE, false);
    assertEquals(Arrays.asList(new Object[] { "1", "2", "3", "null", "null", "null", "4", "null", "5", "null", "null" }), tree3Shape);

    List<Object> tree4Full = HeapLinearizer.linearize(tree4(), LinearizationKind.FULL, false);
    assertEquals(Arrays.asList(new Object[] { "1", "2", "3", "null", "null", "10", "4", "null", "null", "11", "12", "5", "4", "null", "13", "14" }), tree4Full);

    List<Object> tree4Shape = HeapLinearizer.linearize(tree4(), LinearizationKind.SHAPE, false);
    assertEquals(Arrays.asList(new Object[] { "1", "2", "3", "null", "null", "4", "null", "null", "5", "4", "null" }), tree4Shape);

    assertEquals("[1]", HeapLinearizer.linearize(new Object(), LinearizationKind.FULL, false).toString());
    assertEquals("[null]", HeapLinearizer.linearize(null, LinearizationKind.FULL, false).toString());
    assertNotSame(HeapLinearizer.linearize(new Object(), LinearizationKind.FULL, false) , HeapLinearizer.linearize(null, LinearizationKind.FULL, false));
    assertNotSame(HeapLinearizer.linearize(null, LinearizationKind.FULL, false) , HeapLinearizer.linearize(new Object(), LinearizationKind.FULL, false));

    assertNotSame(HeapLinearizer.linearize(new int[]{1}, LinearizationKind.FULL, false) , HeapLinearizer.linearize(new int[0], LinearizationKind.FULL, false));
    assertEquals(HeapLinearizer.linearize(new int[]{1}, LinearizationKind.SHAPE, false) , HeapLinearizer.linearize(new int[0], LinearizationKind.SHAPE, false));

    assertNotSame(HeapLinearizer.linearize(new int[]{1}, LinearizationKind.FULL, false) , HeapLinearizer.linearize(new int[]{1,2}, LinearizationKind.FULL, false));
    assertNotSame(HeapLinearizer.linearize(new int[]{1}, LinearizationKind.SHAPE, false) , HeapLinearizer.linearize(new int[]{1,2}, LinearizationKind.SHAPE, false));

    assertNotSame(HeapLinearizer.linearize(new int[]{2,1}, LinearizationKind.FULL, false) , HeapLinearizer.linearize(new int[]{1,2}, LinearizationKind.FULL, false));
    assertEquals(HeapLinearizer.linearize(new int[]{2,1}, LinearizationKind.SHAPE, false) , HeapLinearizer.linearize(new int[]{1,2}, LinearizationKind.SHAPE, false));

    List<Integer> l0 = new ArrayList<Integer>();

    List<Integer> l1 = new ArrayList<Integer>();
    l1.add(1);

    List<Integer> l2 = new ArrayList<Integer>();
    l2.add(1);
    l2.remove(0);

    List<Integer> l3 = new ArrayList<Integer>();
    l3.add(2);
    l3.remove(0);

    assertEquals(HeapLinearizer.linearize(l0, LinearizationKind.FULL, false) , HeapLinearizer.linearize(l0, LinearizationKind.FULL, false));
    assertNotSame(HeapLinearizer.linearize(l0, LinearizationKind.FULL, false) , HeapLinearizer.linearize(l1, LinearizationKind.FULL, false));
    assertNotSame(HeapLinearizer.linearize(l0, LinearizationKind.FULL, false) , HeapLinearizer.linearize(l2, LinearizationKind.FULL, false));
    assertNotSame(HeapLinearizer.linearize(l1, LinearizationKind.FULL, false) , HeapLinearizer.linearize(l0, LinearizationKind.FULL, false));
    assertNotSame(HeapLinearizer.linearize(l1, LinearizationKind.FULL, false) , HeapLinearizer.linearize(l2, LinearizationKind.FULL, false));
    assertNotSame(HeapLinearizer.linearize(l2, LinearizationKind.FULL, false) , HeapLinearizer.linearize(l0, LinearizationKind.FULL, false));
    assertNotSame(HeapLinearizer.linearize(l2, LinearizationKind.FULL, false) , HeapLinearizer.linearize(l1, LinearizationKind.FULL, false));

    // AbstractList has a "times modified" field.
    assertNotSame(HeapLinearizer.linearize(l0, LinearizationKind.FULL, false) , HeapLinearizer.linearize(l2, LinearizationKind.FULL, false));
    assertEquals(HeapLinearizer.linearize(l0, LinearizationKind.SHAPE, false) , HeapLinearizer.linearize(l2, LinearizationKind.SHAPE, false));

    assertEquals(HeapLinearizer.linearize(l2, LinearizationKind.FULL, false) , HeapLinearizer.linearize(l3, LinearizationKind.FULL, false));

    List<Integer> l0repeated = new ArrayList<Integer>();

    List<Integer> l1repeated = new ArrayList<Integer>();
    l1repeated.add(1);

    List<Integer> l2repeated = new ArrayList<Integer>();
    l2repeated.add(1);
    l2repeated.remove(0);

    // Test that canonicalization works for equivalent objects that are distinct.
    assertEquals(HeapLinearizer.linearize(l0, LinearizationKind.FULL, false) , HeapLinearizer.linearize(l0repeated, LinearizationKind.FULL, false));
    assertEquals(HeapLinearizer.linearize(l1, LinearizationKind.FULL, false) , HeapLinearizer.linearize(l1repeated, LinearizationKind.FULL, false));
    assertEquals(HeapLinearizer.linearize(l2, LinearizationKind.FULL, false) , HeapLinearizer.linearize(l2repeated, LinearizationKind.FULL, false));
  }

}
