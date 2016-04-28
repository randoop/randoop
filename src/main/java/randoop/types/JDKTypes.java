package randoop.types;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

/**
 * Created by bjkeller on 4/25/16.
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

  public static final GenericClassType MAP_TYPE = GenericClassType.forClass(Map.class);
  public static final GenericClassType SORTED_MAP_TYPE = GenericClassType.forClass(SortedMap.class);
  public static final GenericClassType TREE_MAP_TYPE = GenericClassType.forClass(TreeMap.class);
  public static final GenericClassType LINKED_HASH_MAP_TYPE = GenericClassType.forClass(LinkedHashMap.class);

  public static GenericClassType getImplementingType(ParameterizedType type) {
    if (type.isInstantiationOf(COLLECTION_TYPE)) {

      if (type.isInstantiationOf(LIST_TYPE)) {
        if (type.isInstantiationOf(ARRAY_LIST_TYPE)) {
          return ARRAY_LIST_TYPE;
        }
        if (type.isInstantiationOf(LINKED_LIST_TYPE)) {
          return LINKED_LIST_TYPE;
        }
        if (type.isInstantiationOf(STACK_TYPE)) {
          return STACK_TYPE;
        }
        if (type.isInstantiationOf(VECTOR_TYPE)) {
          return VECTOR_TYPE;
        }

        return ARRAY_LIST_TYPE;
      }

      if (type.isInstantiationOf(SET_TYPE)) {
        if (type.isInstantiationOf(ENUM_SET_TYPE)) {
          return ENUM_SET_TYPE;
        }
        if (type.isInstantiationOf(TREE_SET_TYPE)) {
          return TREE_SET_TYPE;
        }

        if (type.isInstantiationOf(LINKED_HASH_SET_TYPE)) {
          return LINKED_HASH_SET_TYPE;
        }
        if (type.isInstantiationOf(HASH_SET_TYPE)) {
          return LINKED_HASH_SET_TYPE;
        }
        if (type.isInstantiationOf(SORTED_SET_TYPE)) {
          return TREE_SET_TYPE;
        }

        return LINKED_HASH_SET_TYPE;
      }

      if (type.isInstantiationOf(QUEUE_TYPE)) {
        if (type.isInstantiationOf(DEQUE_TYPE)) {
          return ARRAY_DEQUE_TYPE;
        }
        return LINKED_LIST_TYPE;
      }
    }
    
    if (type.isInstantiationOf(MAP_TYPE)) {
      if (type.isInstantiationOf(SORTED_MAP_TYPE)) {
        return TREE_MAP_TYPE;
      }
      return LINKED_HASH_MAP_TYPE;
    }
      
    throw new IllegalArgumentException("type must be a JDK Collections type");  
  }
}
