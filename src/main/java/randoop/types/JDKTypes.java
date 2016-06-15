package randoop.types;

import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.AbstractQueue;
import java.util.AbstractSequentialList;
import java.util.AbstractSet;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.WeakHashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TransferQueue;

import randoop.BugInRandoopException;

/**
 * Defines type constants for classes in the JDK Collections.
 */
public class JDKTypes {
  public static final GenericClassType COLLECTION_TYPE = GenericClassType.forClass(Collection.class);
  public static final GenericClassType DEQUE_TYPE = GenericClassType.forClass(Deque.class);
  public static final GenericClassType LIST_TYPE = GenericClassType.forClass(List.class);
  public static final GenericClassType SET_TYPE = GenericClassType.forClass(Set.class);
  public static final GenericClassType SORTED_SET_TYPE = GenericClassType.forClass(SortedSet.class);
  public static final GenericClassType QUEUE_TYPE = GenericClassType.forClass(Queue.class);
  public static final GenericClassType ARRAY_LIST_TYPE = GenericClassType.forClass(ArrayList.class);
  public static final GenericClassType LINKED_LIST_TYPE = GenericClassType.forClass(LinkedList.class);
  public static final GenericClassType LINKED_HASH_SET_TYPE = GenericClassType.forClass(LinkedHashSet.class);
  public static final GenericClassType STACK_TYPE = GenericClassType.forClass(Stack.class);
  public static final GenericClassType ARRAY_DEQUE_TYPE = GenericClassType.forClass(ArrayDeque.class);
  public static final GenericClassType TREE_SET_TYPE = GenericClassType.forClass(TreeSet.class);
  public static final GenericClassType ENUM_SET_TYPE = GenericClassType.forClass(EnumSet.class);
  public static final GenericClassType HASH_SET_TYPE = GenericClassType.forClass(HashSet.class);
  public static final GenericClassType VECTOR_TYPE = GenericClassType.forClass(Vector.class);
  public static final GenericClassType NAVIGABLE_SET_TYPE = GenericClassType.forClass(NavigableSet.class);
  public static final GenericClassType BLOCKING_QUEUE_TYPE = GenericClassType.forClass(BlockingQueue.class);
  public static final GenericClassType TRANSFER_QUEUE_TYPE = GenericClassType.forClass(TransferQueue.class);
  public static final GenericClassType BLOCKING_DEQUE_TYPE = GenericClassType.forClass(BlockingDeque.class);
  public static final GenericClassType PRIORITY_QUEUE_TYPE = GenericClassType.forClass(PriorityQueue.class);
  public static final GenericClassType CONCURRENT_LINKED_QUEUE_TYPE = GenericClassType.forClass(ConcurrentLinkedQueue.class);
  public static final GenericClassType LINKED_BLOCKING_QUEUE_TYPE = GenericClassType.forClass(LinkedBlockingQueue.class);
  public static final GenericClassType ARRAY_BLOCKING_QUEUE_TYPE = GenericClassType.forClass(ArrayBlockingQueue.class);
  public static final GenericClassType PRIORITY_BLOCKING_QUEUE_TYPE = GenericClassType.forClass(PriorityBlockingQueue.class);
  public static final GenericClassType DELAY_QUEUE_TYPE = GenericClassType.forClass(DelayQueue.class);
  public static final GenericClassType SYNCHRONOUS_QUEUE_TYPE = GenericClassType.forClass(SynchronousQueue.class);
  public static final GenericClassType LINKED_BLOCKING_DEQUE_TYPE = GenericClassType.forClass(LinkedBlockingDeque.class);
  public static final GenericClassType LINKED_TRANSFER_QUEUE_TYPE = GenericClassType.forClass(LinkedTransferQueue.class);
  public static final GenericClassType CONCURRENT_SKIP_LIST_SET_TYPE = GenericClassType.forClass(ConcurrentSkipListSet.class);

  public static final GenericClassType MAP_TYPE = GenericClassType.forClass(Map.class);
  public static final GenericClassType SORTED_MAP_TYPE = GenericClassType.forClass(SortedMap.class);
  public static final GenericClassType TREE_MAP_TYPE = GenericClassType.forClass(TreeMap.class);
  public static final GenericClassType HASH_MAP_TYPE = GenericClassType.forClass(HashMap.class);
  public static final GenericClassType HASH_TABLE_TYPE = GenericClassType.forClass(Hashtable.class);
  public static final GenericClassType LINKED_HASH_MAP_TYPE = GenericClassType.forClass(LinkedHashMap.class);
  public static final GenericClassType NAVIGABLE_MAP_TYPE = GenericClassType.forClass(NavigableMap.class);
  public static final GenericClassType CONCURRENT_MAP_TYPE = GenericClassType.forClass(ConcurrentMap.class);
  public static final GenericClassType CONCURRENT_NAVIGABLE_MAP_TYPE = GenericClassType.forClass(ConcurrentNavigableMap.class);
  public static final GenericClassType CONCURRENT_HASH_MAP_TYPE = GenericClassType.forClass(ConcurrentHashMap.class);
  public static final GenericClassType CONCURRENT_SKIP_LIST_MAP_TYPE = GenericClassType.forClass(ConcurrentSkipListMap.class);
  public static final GenericClassType IDENTITY_HASH_MAP_TYPE = GenericClassType.forClass(IdentityHashMap.class);
  public static final GenericClassType WEAK_HASH_MAP_TYPE = GenericClassType.forClass(WeakHashMap.class);
  public static final GenericClassType ENUM_MAP_TYPE = GenericClassType.forClass(EnumMap.class);

  public static final GenericClassType ABSTRACT_COLLECTION_TYPE = GenericClassType.forClass(AbstractCollection.class);
  public static final GenericClassType ABSTRACT_SET_TYPE = GenericClassType.forClass(AbstractSet.class);
  public static final GenericClassType ABSTRACT_LIST_TYPE = GenericClassType.forClass(AbstractList.class);
  public static final GenericClassType ABSTRACT_SEQUENTIAL_LIST_TYPE = GenericClassType.forClass(AbstractSequentialList.class);
  public static final GenericClassType ABSTRACT_QUEUE_TYPE = GenericClassType.forClass(AbstractQueue.class);
  public static final GenericClassType ABSTRACT_MAP_TYPE = GenericClassType.forClass(AbstractMap.class);

  /** Implementing type map for interface and abstract class types */
  private static Map<GenericClassType, GenericClassType> implementingTypeMap = new LinkedHashMap<>();

  static {
    // interfaces
    implementingTypeMap.put(COLLECTION_TYPE, ARRAY_LIST_TYPE);
    implementingTypeMap.put(LIST_TYPE, ARRAY_LIST_TYPE);
    implementingTypeMap.put(SET_TYPE, LINKED_HASH_SET_TYPE);
    implementingTypeMap.put(QUEUE_TYPE, LINKED_LIST_TYPE);
    implementingTypeMap.put(DEQUE_TYPE, ARRAY_DEQUE_TYPE);
    implementingTypeMap.put(SORTED_SET_TYPE, TREE_SET_TYPE);
    implementingTypeMap.put(NAVIGABLE_SET_TYPE, TREE_SET_TYPE);
    implementingTypeMap.put(BLOCKING_DEQUE_TYPE, LINKED_BLOCKING_DEQUE_TYPE);
    implementingTypeMap.put(BLOCKING_QUEUE_TYPE, ARRAY_BLOCKING_QUEUE_TYPE);
    implementingTypeMap.put(TRANSFER_QUEUE_TYPE, LINKED_TRANSFER_QUEUE_TYPE);

    implementingTypeMap.put(MAP_TYPE, LINKED_HASH_MAP_TYPE);
    implementingTypeMap.put(NAVIGABLE_MAP_TYPE, TREE_MAP_TYPE);
    implementingTypeMap.put(SORTED_MAP_TYPE, TREE_MAP_TYPE);
    implementingTypeMap.put(CONCURRENT_MAP_TYPE, CONCURRENT_SKIP_LIST_MAP_TYPE);
    implementingTypeMap.put(CONCURRENT_NAVIGABLE_MAP_TYPE, CONCURRENT_SKIP_LIST_MAP_TYPE);

    // abstract classes
    implementingTypeMap.put(ABSTRACT_COLLECTION_TYPE, ARRAY_LIST_TYPE);
    implementingTypeMap.put(ABSTRACT_LIST_TYPE, ARRAY_LIST_TYPE);
    implementingTypeMap.put(ABSTRACT_SET_TYPE, LINKED_HASH_SET_TYPE);
    implementingTypeMap.put(ABSTRACT_SEQUENTIAL_LIST_TYPE, LINKED_LIST_TYPE);
    implementingTypeMap.put(ABSTRACT_QUEUE_TYPE, ARRAY_BLOCKING_QUEUE_TYPE);
    implementingTypeMap.put(ABSTRACT_MAP_TYPE, HASH_MAP_TYPE);
  }
  /**
   * Returns a concrete collection type for each Collections type
   * (e.g., returns {@code ArrayList} for {@code List}).
   * If a type is already concrete, then returns that type.
   *
   * @param type  the (abstract) Collections type
   * @return a concrete Collection type implementing the given type
   */
  public static GenericClassType getImplementingType(ParameterizedType type) {
    GenericClassType genericType = type.getGenericClassType();
    if (! genericType.isSubtypeOf(COLLECTION_TYPE) && ! genericType.isSubtypeOf(MAP_TYPE)) {
      throw new IllegalArgumentException("type must be a JDK Collections type, got " + type);
    }

    if (!(genericType.isInterface() || genericType.isAbstract())
            || genericType.equals(ENUM_SET_TYPE)) {
        return genericType;
    } else {
      GenericClassType implementingType = implementingTypeMap.get(genericType);
      if (implementingType != null) {
        return implementingType;
      } else {
        throw new BugInRandoopException("no implementing type for collection class: " + genericType);
      }
    }

  }
}
