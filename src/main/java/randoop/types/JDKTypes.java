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

public class JDKTypes {
  public static final GenericClassType COLLECTION_TYPE = new GenericClassType(Collection.class);
  public static final GenericClassType DEQUE_TYPE = new GenericClassType(Deque.class);
  public static final GenericClassType LIST_TYPE = new GenericClassType(List.class);
  public static final GenericClassType SET_TYPE = new GenericClassType(Set.class);
  public static final GenericClassType SORTED_SET_TYPE = new GenericClassType(SortedSet.class);
  public static final GenericClassType QUEUE_TYPE = new GenericClassType(Queue.class);
  public static final GenericClassType ARRAY_LIST_TYPE = new GenericClassType(ArrayList.class);
  public static final GenericClassType LINKED_LIST_TYPE = new GenericClassType(LinkedList.class);
  public static final GenericClassType LINKED_HASH_SET_TYPE = new GenericClassType(LinkedHashSet.class);
  public static final GenericClassType STACK_TYPE = new GenericClassType(Stack.class);
  public static final GenericClassType ARRAY_DEQUE_TYPE = new GenericClassType(ArrayDeque.class);
  public static final GenericClassType TREE_SET_TYPE = new GenericClassType(TreeSet.class);
  public static final GenericClassType ENUM_SET_TYPE = new GenericClassType(EnumSet.class);
  public static final GenericClassType HASH_SET_TYPE = new GenericClassType(HashSet.class);
  public static final GenericClassType VECTOR_TYPE = new GenericClassType(Vector.class);

  public static final GenericClassType MAP_TYPE = new GenericClassType(Map.class);
  public static final GenericClassType SORTED_MAP_TYPE = new GenericClassType(SortedMap.class);
  public static final GenericClassType TREE_MAP_TYPE = new GenericClassType(TreeMap.class);
  public static final GenericClassType LINKED_HASH_MAP_TYPE = new GenericClassType(LinkedHashMap.class);

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
