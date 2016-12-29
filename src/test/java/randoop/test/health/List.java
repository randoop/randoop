package randoop.test.health;

import java.util.Enumeration;

/**
 * A linked list container.
 */
public class List {
  ListNode head;
  ListNode tail;

  public List() {
    head = null;
    tail = null;
  }

  public void add(Patient p) {
    ListNode node = new ListNode(p);
    if (head == null) {
      head = node;
    } else {
      tail.next = node;
    }
    tail = node;
  }

  public void delete(Object o) {
    if (head == null) {
      return;
    }

    if (tail.object == o) {
      tail = null;
    }

    if (head.object == o) {
      head = head.next;
      return;
    }

    ListNode p = head;
    for (ListNode ln = head.next; ln != null; ln = ln.next) {
      if (ln.object == o) {
        p.next = ln.next;
        return;
      }
      p = ln;
    }
  }

  public Enumeration elements() {
    return new ListEnumerator(this);
  }

  public final class ListNode {
    public ListNode next;
    public Object object;

    ListNode(Object o) {
      object = o;
      next = null;
    }
  }

  public final class ListEnumerator implements Enumeration {
    ListNode curNode;

    public ListEnumerator(List l) {
      curNode = l.head;
    }

    public Object nextElement() {
      Object o = curNode.object;
      curNode = curNode.next;
      return o;
    }

    public boolean hasMoreElements() {
      return curNode != null;
    }
  }
}
