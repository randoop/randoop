package symexamples;

import java.util.HashSet;

public class BinomialHeap /*implements java.io.Serializable*/ {

    // internal class BinomialHeapNode
    public static class BinomialHeapNode /*implements java.io.Serializable*/ {
    //private static final long serialVersionUID=6495900899527469811L;

    //private int key;                      // element in current node
    private Integer key;                      // element in current node
    private int degree;                   // depth of the binomial tree having the current node as its root
    private BinomialHeapNode parent;      // pointer to the parent of the current node
    private BinomialHeapNode sibling;     // pointer to the next binomial tree in the list
    private BinomialHeapNode child;       // pointer to the first child of the current node
    
    /*
    public BinomialHeapNode() {
        this(new Integer(0));
    }
    */

    //public BinomialHeapNode(int k) {
    public BinomialHeapNode(Integer k) {
        key = k;
        degree = 0;
        parent = null;
        sibling = null;
        child = null;
    }

      //public int getKey() {                     // returns the element in the current node
    public Integer getKey() {                     // returns the element in the current node
          return key;
      }
    
      //private void setKey(int value) {          // sets the element in the current node
    private void setKey(Integer value) {          // sets the element in the current node
          key = value;
      }
    
      public int getDegree() {                  // returns the degree of the current node
          return degree;
      }
    
      private void setDegree(int deg) {         // sets the degree of the current node
          degree = deg;
      }
    
      public BinomialHeapNode getParent() {     // returns the father of the current node
          return parent;
     }
    
      private void setParent(BinomialHeapNode par) {          // sets the father of the current node
          parent = par;
      }
    
      public BinomialHeapNode getSibling() {                  // returns the next binomial tree in the list
          return sibling;
      }
    
      private void setSibling(BinomialHeapNode nextBr) {      // sets the next binomial tree in the list
          sibling = nextBr;
      }
    
      public BinomialHeapNode getChild() {                    // returns the first child of the current node
          return child;
      }
    
      private void setChild(BinomialHeapNode firstCh) {       // sets the first child of the current node
          child = firstCh;
      }
    
    public int getSize() {
        return (1 + ((child == null)?0:child.getSize()) + ((sibling == null)?0:sibling.getSize()));
    }

    private BinomialHeapNode reverse(BinomialHeapNode sibl) {
        BinomialHeapNode ret;
        if (sibling != null) ret = sibling.reverse(this);
        else ret = this;
        sibling = sibl;
        return ret;
    }

    /*
    // toString
    public String toString() {
        BinomialHeapNode temp = this;
        String ret = "";
        while (temp != null) {
        ret += "(";
        if (temp.parent == null) ret += "Parent: null";
        else ret += "Parent: "+temp.parent.key;
        ret += "  Degree: "+temp.degree+"  Key: "+temp.key+") ";
        if (temp.child != null) ret += temp.child.toString();
        temp = temp.sibling;
        }
        if (parent == null) ret += " ";
        return ret;
    }

    public boolean equals(Object that) {
        if ((that instanceof BinomialHeapNode) && (that != null)) {
        BinomialHeapNode bhn = (BinomialHeapNode)that;
        if ((key != bhn.key) || (degree != bhn.degree) ||
            ((parent == null) && (bhn.parent != null)) ||
            ((parent != null) && (bhn.parent == null)) ||
            ((child == null) && (bhn.child != null)) ||
            ((child != null) && (bhn.child == null)) ||
            ((child != null) && !child.equals(bhn.child)) ||
            ((sibling == null) && (bhn.sibling != null)) ||
            ((sibling != null) && (bhn.sibling == null)) ||
            ((sibling != null) && (!sibling.equals(bhn.sibling))))
            return false;
        else return true;
        }
        else return false;
    }
    */

    private BinomialHeapNode findMinNode() {
        BinomialHeapNode x = this, y = this;
        //int min = x.key;
        Integer min = x.key;
        
        while (x != null) {
        //if (x.key < min) {
        if (x.key.compareTo(min) < 0) {
            y = x;
            min = x.key;
        }
        x = x.sibling;
        }
        
        return y;
    }
    
    // Find a node with the given key
    //private BinomialHeapNode findANodeWithKey(int value) {
    private BinomialHeapNode findANodeWithKey(Integer value) {
        BinomialHeapNode temp = this, node = null;
        while (temp != null) {
        //if (temp.key == value) {
        if (temp.key.compareTo(value) == 0) {
            node = temp;
            break;
        }
        if (temp.child == null) temp = temp.sibling;
        else {
            node = temp.child.findANodeWithKey(value);
            if (node == null) temp = temp.sibling;
            else break;
        }
        }
        
        return node;
    }

    // procedures used by Korat
    /*
    private boolean repCheckWithRepetitions(int key_, int degree_,                            // equal keys allowed
                        Object parent_, HashSet nodesSet) {
        BinomialHeapNode temp = this;
        
        int rightDegree = 0;
        if (parent_ == null) {
        while ((degree_ & 1) == 0) {
            rightDegree += 1;
            degree_ /= 2;
        }
        degree_ /= 2;
        }
        else rightDegree = degree_;
        
        while (temp != null) {
        if ((temp.degree != rightDegree) || (temp.parent != parent_) ||
            (temp.key < key_) || (nodesSet.contains(temp))) return false; 
        else {
            nodesSet.add(temp);
            if (temp.child == null) {
            temp = temp.sibling;
            
            if (parent_ == null) {
                if (degree_ == 0) return (temp == null);
                while ((degree_ & 1) == 0) {
                rightDegree += 1;
                degree_ /= 2;
                }
                degree_ /= 2;
                rightDegree++;
            }
            else rightDegree--;
            }
            else {
            boolean b = temp.child.repCheckWithRepetitions(temp.key, temp.degree - 1, temp, nodesSet);
            if (!b) return false;
            else {
                temp = temp.sibling;
                
                if (parent_ == null) {
                if (degree_ == 0) return (temp == null);
                while ((degree_ & 1) == 0) {
                    rightDegree += 1;
                    degree_ /= 2;
                }
                degree_ /= 2;
                rightDegree++;
                }
                else rightDegree--;
            }
            }
        }
        }
        
        return true;
    }
    
    private boolean repCheckWithoutRepetitions(int key_, HashSet keysSet, int degree_,        // equal keys not allowed
                           Object parent_, HashSet nodesSet) {
        BinomialHeapNode temp = this;
   
        int rightDegree = 0;
        if (parent_ == null) {
        while ((degree_ & 1) == 0) {
            rightDegree += 1;
            degree_ /= 2;
        }
        degree_ /= 2;
        }
        else rightDegree = degree_;

        while (temp != null) {
        if ((temp.degree != rightDegree) || (temp.parent != parent_) || (temp.key <= key_) ||
            (nodesSet.contains(temp)) || (keysSet.contains(new Integer(temp.key)))) {
            return false;
        }
        else {
            nodesSet.add(temp);
            keysSet.add(new Integer(temp.key));
            if (temp.child == null) {
            temp = temp.sibling;

            if (parent_ == null) {
                if (degree_ == 0) return (temp == null);
                while ((degree_ & 1) == 0) {
                rightDegree += 1;
                degree_ /= 2;
                }
                degree_ /= 2;
                rightDegree++;
            }
            else rightDegree--;
            }
            else {
            boolean b = temp.child.repCheckWithoutRepetitions(temp.key, keysSet, temp.degree - 1, temp, nodesSet);
            if (!b) return false;
            else {
                temp = temp.sibling;
                
                if (parent_ == null) {
                if (degree_ == 0) return (temp == null);
                while ((degree_ & 1) == 0) {
                    rightDegree += 1;
                    degree_ /= 2;
                }
                degree_ /= 2;
                rightDegree++;
                }
                else rightDegree--;
            }
            }
        }
        }
        
        return true;
    }
    
    
    public boolean repOk(int size) {
        // replace 'repCheckWithoutRepetitions' with 'repCheckWithRepetitions' if you do not want to allow equal keys
        //
               return repCheckWithRepetitions(0, size, null, new LinkedHashSet());
        //        return repCheckWithoutRepetitions(0, new LinkedHashSet(), size, null, new LinkedHashSet());
    }

        boolean checkDegree(int degree) {
            for (BinomialHeapNode current = this.child; current != null; current = current.sibling) {
                degree--;
                if (current.degree != degree) return false;
                if (!current.checkDegree(degree)) return false;
            }
            return (degree == 0);
        }

        boolean isHeapified() {
            touch(key);
            for (BinomialHeapNode current = this.child; current != null; current = current.sibling) {
                if (!(key <= current.key)) return false;
                if (!current.isHeapified()) return false;
            }
            return true;
        }
        void touch(int key) {}

        boolean isHeapifiedSpecial() {
            touch(key);
            for (BinomialHeapNode current = this.child; current != null; current = current.sibling) {
                boolean b = !_KORAT_POSTPROCESS_isReadField_BinomialHeapNode_key(current);
                int i = _KORAT_POSTPROCESS_indexField_BinomialHeapNode_key(current);
                if (b) korat.RepOkObserver.setSpecialGen(i, new korat.SpecialGenGEint(key));
                if (!(key <= current.key)) return false;
                if (!current.isHeapifiedSpecial()) return false;
            }
            return true;
        }
        final boolean _KORAT_POSTPROCESS_isReadField_BinomialHeapNode_key(BinomialHeapNode q) { return true; }
        final int _KORAT_POSTPROCESS_indexField_BinomialHeapNode_key(BinomialHeapNode q) { return -1; }
        
        boolean isTree(java.util.Set visited, BinomialHeapNode parent) {
            if (this.parent != parent) return false;
            for (BinomialHeapNode current = this.child; current != null; current = current.sibling) {
                if (!visited.add(new NodeWrapper(current))) return false;
                if (!current.isTree(visited, this)) return false;
            }
            return true;
        }

        boolean isTreeSpecial(korat.SpecialGen visited, BinomialHeapNode parent) {
            if (this.parent != parent) return false;
            boolean b = !_KORAT_POSTPROCESS_isReadField_BinomialHeapNode_child(this);
            int i = _KORAT_POSTPROCESS_indexField_BinomialHeapNode_child(this);
            BinomialHeapNode current = this.child; 
            while (current != null) {
                if (b) korat.RepOkObserver.setSpecialGen(i, new korat.SpecialGen(visited));
                if (!visited.add(current)) return false;
                if (!current.isTreeSpecial(visited, this)) return false;
                b = !_KORAT_POSTPROCESS_isReadField_BinomialHeapNode_sibling(current);
                i = _KORAT_POSTPROCESS_indexField_BinomialHeapNode_sibling(current);
                current = current.sibling;
            }
            return true;
        }
        final boolean _KORAT_POSTPROCESS_isReadField_BinomialHeapNode_child(BinomialHeapNode q) { return true; }
        final int _KORAT_POSTPROCESS_indexField_BinomialHeapNode_child(BinomialHeapNode q) { return -1; }
        final boolean _KORAT_POSTPROCESS_isReadField_BinomialHeapNode_sibling(BinomialHeapNode q) { return true; }
        final int _KORAT_POSTPROCESS_indexField_BinomialHeapNode_sibling(BinomialHeapNode q) { return -1; }
    }
    

    public static final class NodeWrapper {
        BinomialHeapNode node;
        NodeWrapper(BinomialHeapNode n) {
            this.node = n;
        }
        public boolean equals(Object o) {
            if (!(o instanceof NodeWrapper)) return false;
            return node == ((NodeWrapper)o).node;
        }
        public int hashCode() {
            return System.identityHashCode(node);
        }
        */
    }
    
    // end of helper class BinomialHeapNode

    private BinomialHeapNode Nodes;
    private int size;

    public BinomialHeap() {
    Nodes = null;
    size = 0;
    }

    /*
    public BinomialHeap(BinomialHeapNode Nodes) {
    this.Nodes = Nodes;
    if (Nodes == null) size = 0;
    else size = Nodes.getSize();
    }

    public BinomialHeap(int size) {
    Nodes = null;
    this.size = 0;
    for (int i = 0; i < size; i++) insert(i + 1);
    }
    */

    /*
    public int getSize() {
    return size;
    }

    public void setSize(int size) {
    this.size = size;
    }
    */

    //public boolean contains(int value) {
    /*
    private boolean contains(Integer value) {
    return ((Nodes != null) && (Nodes.findANodeWithKey(value) != null));
    }
    */

    /*
    // debugging procedure
    private void debug(String msg) {
    System.out.println(msg);
    }

    // toString()
    public String toString() {
    if (Nodes == null) return size + "\n()\n";
    else return size + "\n" + Nodes.toString();
    }

    public boolean equals(Object that) {
    if ((that instanceof BinomialHeap) && (that != null)) {
        BinomialHeap bh = (BinomialHeap)that;
        if ((size != bh.size) || 
        ((Nodes == null) && (bh.Nodes != null)) ||
        ((Nodes != null) && (bh.Nodes == null)) ||
        ((Nodes != null) && (!Nodes.equals(bh.Nodes))))
        return false;
        else return true;
    }
    else return false;
    }
    */

    // operations with BinomialHeaps

    /*
    // 1. Make an empty binomial heap
    public static BinomialHeap makeBinomialHeapNode() {
    return (new BinomialHeap());
    }
    */

    // 2. Find the minimum key
    //public int findMinimum() {
    private Integer findMinimum() {
    return Nodes.findMinNode().key;
    }

    // 3. Unite two binomial heaps
    // helper procedure
    private void merge(BinomialHeapNode binHeap) {
    BinomialHeapNode temp1 = Nodes, temp2 = binHeap;
    while ((temp1 != null) && (temp2 != null)) {
        if (temp1.degree == temp2.degree) {
        BinomialHeapNode tmp = temp2;
        temp2 = temp2.sibling;
        tmp.sibling = temp1.sibling;
        temp1.sibling = tmp;
        temp1 = tmp.sibling;
        }
        else {
        if (temp1.degree < temp2.degree) {
            if ((temp1.sibling == null) || (temp1.sibling.degree > temp2.degree)) {
            BinomialHeapNode tmp = temp2;
            temp2 = temp2.sibling;
            tmp.sibling = temp1.sibling;
            temp1.sibling = tmp;
            temp1 = tmp.sibling;
            }
            else temp1 = temp1.sibling;
        }
        else {
            BinomialHeapNode tmp = temp1;
            temp1 = temp2;
            temp2 = temp2.sibling;
            temp1.sibling = tmp;
            if (tmp == Nodes) Nodes = temp1;
        }
        }
    }
    
    if (temp1 == null) {
          temp1 = Nodes;
          while (temp1.sibling != null) temp1 = temp1.sibling;
          temp1.sibling = temp2;
      }
    }
    
    // another helper procedure
    private void unionNodes(BinomialHeapNode binHeap) {
    merge(binHeap);

    BinomialHeapNode prevTemp = null, temp = Nodes, nextTemp = Nodes.sibling;
    
    while (nextTemp != null) {
        if ((temp.degree != nextTemp.degree) || ((nextTemp.sibling != null) &&
                             (nextTemp.sibling.degree == temp.degree))) {
        prevTemp = temp;
        temp = nextTemp;
        }
        else {
        //if (temp.key <= nextTemp.key) {
        if (temp.key.compareTo(nextTemp.key) <=0) {
            temp.sibling = nextTemp.sibling;
            nextTemp.parent = temp;
            nextTemp.sibling = temp.child;
            temp.child = nextTemp;
            temp.degree++;
        }
        else {
            if (prevTemp == null) Nodes = nextTemp;
            else prevTemp.sibling = nextTemp;
            temp.parent = nextTemp;
            temp.sibling = nextTemp.child;
            nextTemp.child = temp;
            nextTemp.degree++;
            temp = nextTemp;
        }
        }
        
        nextTemp = temp.sibling;
    }
    }
    
    /*
    // actual procedure of uniting two binomial heaps
    public void union(BinomialHeap binHeap) {
    if ((binHeap != null) && (binHeap.Nodes != null)) {
        if (Nodes == null) {
        Nodes = binHeap.Nodes;
        size = Nodes.getSize();
        }
        else {
        unionNodes(binHeap.Nodes);
        size = Nodes.getSize();
        }
    }
    }
    */

    // 4. Insert a node with a specific value
    //public void insert(int value) {
    public void insert(Integer value) {
    //if (value > 0) {
    if (value.compareTo(new Integer(0)) > 0) {
        BinomialHeapNode temp = new BinomialHeapNode(value);
        if (Nodes == null) {
        Nodes = temp;
        size = 1;
        }
        else {
        unionNodes(temp);
        size++;
        }
    }
    }
    
    // 5. Extract the node with the minimum key
    //public int extractMin() {
    public Integer extractMin() {
    if (Nodes == null) return new Integer(-1);

    BinomialHeapNode temp = Nodes, prevTemp = null;
    BinomialHeapNode minNode = Nodes.findMinNode();
    //while (temp.key != minNode.key) {
    while (temp.key.compareTo(minNode.key) != 0) {
        prevTemp = temp;
        temp = temp.sibling;
    }
    
    if (prevTemp == null) Nodes = temp.sibling;
    else prevTemp.sibling = temp.sibling;
    temp = temp.child;
    BinomialHeapNode fakeNode = temp;
    while (temp != null) {
        temp.parent = null;
        temp = temp.sibling;
    }

    if ((Nodes == null) && (fakeNode == null)) size = 0;
    else {
        if ((Nodes == null) && (fakeNode != null)) {
        Nodes = fakeNode.reverse(null);
        size = Nodes.getSize();
        }
        else {
        if ((Nodes != null) && (fakeNode == null)) size = Nodes.getSize();
        else {
            unionNodes(fakeNode.reverse(null));
            size = Nodes.getSize();
        }
        }
    }

    return minNode.key;
    }

    // 6. Decrease a key value
    //public void decreaseKeyValue(int old_value, int new_value) {
    private void decreaseKeyValue(Integer old_value, Integer new_value) {
    BinomialHeapNode temp = Nodes.findANodeWithKey(old_value);
    temp.key = new_value;
    BinomialHeapNode tempParent = temp.parent;
    
    //while ((tempParent != null) && (temp.key < tempParent.key)) {
    while ((tempParent != null) && (temp.key.compareTo(tempParent.key) < 0)) {
        Integer z = temp.key;
        temp.key = tempParent.key;
        tempParent.key = z;
        
        temp = tempParent;
        tempParent = tempParent.parent;
    }
    }

    // 7. Delete a node with a certain key
    //public void delete(int value) {
    public void delete(Integer value) {
    if ((Nodes != null) && (Nodes.findANodeWithKey(value) != null)) {
        decreaseKeyValue(value, new Integer(findMinimum().intValue() - 1));
        extractMin();
    }
    }


    // procedures used by Korat
    /*
    public boolean repOkOld() {
        if (size == 0) return (Nodes == null);
        if (Nodes == null) return false;    

    return (Nodes.repOk(size)) && (size == Nodes.getSize());
    }

    boolean checkDegrees() {
        int degree_ = size;
        int rightDegree = 0;
        for (BinomialHeapNode current = Nodes; current != null; current = current.sibling) {
            if (degree_ == 0) return false;
            while ((degree_ & 1) == 0) {
                rightDegree++;
                degree_ /= 2;
            }
            if (current.degree != rightDegree) return false;
            if (!current.checkDegree(rightDegree)) return false;
            rightDegree++;
            degree_ /= 2;
        }
        return (degree_ == 0);
    }

    boolean checkHeapified() {
        for (BinomialHeapNode current = Nodes; current != null; current = current.sibling) {
            if (!current.isHeapified()) return false;
        }
        return true;
    }

    boolean checkHeapifiedSpecial() {
        for (BinomialHeapNode current = Nodes; current != null; current = current.sibling) {
            if (!current.isHeapifiedSpecial()) return false;
        }
        return true;
    }

    public boolean repOk() {
        if (size == 0) return (Nodes == null);
        if (Nodes == null) return false;
        // checks that list of trees has no cycles
        java.util.Set visited = new java.util.HashSet();
        for (BinomialHeapNode current = Nodes; current != null; current = current.sibling) {
            // checks that the list has no cycle
            if (!visited.add(new NodeWrapper(current))) return false;
            if (!current.isTree(visited, null)) return false;
        }
        // checks that the total size is consistent
        if (visited.size() != size) return false;
        // checks that the degrees of all trees are binomial
        if (!checkDegrees()) return false;
        // checks that keys are heapified
        if (!checkHeapified()) return false;
        return true;
    }

    public boolean repOkSpecial() {
        if (size == 0) return (Nodes == null);
        if (Nodes == null) return false;
        // checks that list of trees has no cycles
        korat.SpecialGen visited = new korat.SpecialGen();
        boolean b = !_KORAT_POSTPROCESS_isReadField_BinomialHeap_Nodes(this);
        int i = _KORAT_POSTPROCESS_indexField_BinomialHeap_Nodes(this);
        BinomialHeapNode current = this.Nodes; 
        while (current != null) {
            if (b) korat.RepOkObserver.setSpecialGen(i, new korat.SpecialGen(visited));
            if (!visited.add(current)) return false;
            if (!current.isTreeSpecial(visited, null)) return false;
            b = !_KORAT_POSTPROCESS_isReadField_BinomialHeapNode_sibling(current);
            i = _KORAT_POSTPROCESS_indexField_BinomialHeapNode_sibling(current);
            current = current.sibling;
        }
        // checks that the total size is consistent
        if (visited.size() != size) return false;
        // checks that the degrees of all trees are binomial
        if (!checkDegrees()) return false;
        // checks that keys are heapified
        if (!checkHeapifiedSpecial()) return false;
        return true;
    }
    final boolean _KORAT_POSTPROCESS_isReadField_BinomialHeap_Nodes(BinomialHeap q) { return true; }
    final int _KORAT_POSTPROCESS_indexField_BinomialHeap_Nodes(BinomialHeap q) { return -1; }
    final boolean _KORAT_POSTPROCESS_isReadField_BinomialHeapNode_sibling(BinomialHeapNode q) { return true; }
    final int _KORAT_POSTPROCESS_indexField_BinomialHeapNode_sibling(BinomialHeapNode q) { return -1; }

    public static Finitization finBinomialHeap(int SIZE) {
    Finitization f = new Finitization(BinomialHeap.class);
    ObjSet heaps = 
        f.createObjects(BinomialHeap.BinomialHeapNode.class, SIZE);
    heaps.add(null);
    f.setFieldIn("size", new IntSet(0, SIZE));
    f.set("Nodes", heaps);
    f.set(BinomialHeap.BinomialHeapNode.class, "parent", heaps);
    f.set(BinomialHeap.BinomialHeapNode.class, "sibling", heaps);
    f.set(BinomialHeap.BinomialHeapNode.class, "child", heaps);
    f.set(BinomialHeap.BinomialHeapNode.class, "key", new IntSet(1, SIZE));
    f.set(BinomialHeap.BinomialHeapNode.class, "degree", new IntSet(0, SIZE));
    return f;
    }
    */
}
// end of class BinomialHeap
