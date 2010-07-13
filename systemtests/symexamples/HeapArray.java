/*KORAT_START*/
package symexamples;
/*KORAT_END*/
public class HeapArray /*implements java.io.Serializable*/{
    private /*KORAT_START*//*@ spec_public @*//*KORAT_END*/int size; // number of elements in the heap
    private /*KORAT_START*//*@ spec_public @*//*KORAT_END*/Comparable[] array; // heap elements     
    /*KORAT_START*/
    /*
    public int getSize() {
        return size;
    }

    public Comparable[] getArray() {
        return array;
    }
    */
    /*KORAT_END*/
    //@ public invariant repOk();
    /*
    public boolean repOk() {
        // checks that array is non-null
        if (array == null) return false;
        // checks that size is within array bounds
        if (size < 0 || size > array.length) 
            return false;
        for (int i = 0; i < size; i++) {
            // checks that elements are non-null 
            if (array[i] == null) return false;
            // checks that array is heapified
            if (i > 0 && 
                array[i].compareTo(array[(i-1)/2]) > 0) 
                return false;
        }
        // checks that non-heap elements are null
        for (int i = size; i < array.length; i++)
            if (array[i] != null) return false;
        return true;
    }
    */
    /*KORAT_START*/
    /*
    public boolean repOkSpecial() {
        // checks that array is non-null
        if (array == null) return false;
        // checks that size is within array bounds
        if (size < 0 || size > array.length) 
            return false;
        for (int i = 0; i < size; i++) {
            boolean be = !_KORAT_POSTPROCESS_isReadArray_array(i);
            int ie = _KORAT_POSTPROCESS_indexArray_array(i);
            // checks that elements are non-null 
            if (array[i] == null) return false;
            // checks that array is heapified
            if (i > 0) {
                if (be) korat.RepOkObserver.setSpecialGen(ie, new korat.SpecialGenLE(array[(i-1)/2]));
                if (array[i].compareTo(array[(i-1)/2]) > 0) {
                    return false;
                }
            }
        }
        // checks that non-heap elements are null
        for (int i = size; i < array.length; i++) {
            boolean be = !_KORAT_POSTPROCESS_isReadArray_array(i);
            int ie = _KORAT_POSTPROCESS_indexArray_array(i);
            if (be) korat.RepOkObserver.setSpecialGen(ie, new korat.SpecialGenEq(null));
            if (array[i] != null) return false;
        }
        return true;
    }
    
    final boolean _KORAT_POSTPROCESS_isReadArray_array(int i) { return true; }
    final int _KORAT_POSTPROCESS_indexArray_array(int i) { return -1; }
    */
    /*@ public normal_behavior
      @     requires repOk() && size > 0;
      @     ensures repOk() && \result == \old(array[0]);
      @ also public exceptional_behavior
      @     requires repOk() && size == 0;
      @     signals (IllegalArgumentException e) true;
      @*/
    public Object extractMax() {
        if (size == 0) 
            throw new IllegalArgumentException();
        Object o = array[0];
        array[0] = array[--size];
        array[size] = null;
        heapifyDown(0);
        return o;
    }
    
    private void heapifyDown(int index){
        int son;
        Comparable elm = array[index];
        son = index * 2 + 1;
        if ((son + 1 < size) && (array[son].compareTo(array[son + 1]) < 0)) son = son + 1;
        while ((son < size) && (elm.compareTo(array[son]) < 0)) {
            array[index] = array[son];
            index = son;
            son = son * 2 + 1;
            if ((son + 1 < size) && (array[son].compareTo(array[son + 1]) < 0)) son = son + 1;
            if (son >= size) break;
        }
        array[index] = elm;
    }

    /*@ public normal_behavior
      @     requires repOk();
      @     ensures repOk();
      @*/
    public boolean insert(Comparable element) {
        /*
        if (element == null)
            return false;
        */

        if (size >= array.length) { // then double the length of the array (=> O(1) amortized cost ;)
            Comparable temp[] = new Comparable[2*array.length+1];
            for(int i = 0; i < size; i++)
                temp[i] = array[i];
            array = temp;
        }
        
        array[size] = element;
        heapifyUp(size);
        size++;
        return true;
    }

    private void heapifyUp(int index) {
        while (index > 0 && array[(index-1) / 2].compareTo(array[index]) < 0) {
            Comparable t = array[index];
            array[index] = array[(index-1) / 2];
            array[(index-1) / 2] = t;
            index = (index-1) / 2;
        }
    }
    /*KORAT_END*/
    /*KORAT_START*/
    
    public HeapArray() { array = new Integer[5]; }
    //public HeapArray(int maxSize) { array = new Integer[maxSize]; }
    /*
    public static void main(String[] args) {
        HeapArray h = new HeapArray(args.length);
        h.size = args.length;
        h.array = new Integer[h.size];
        for (int i = 0; i < h.array.length; i++) 
            h.array[i] = new Integer(Integer.parseInt(args[i]));
        if (h.repOk())
            System.out.println("Okay.");
        else
            System.out.println("Not Okay!");
    }
    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append(size);
        s.append(" [");
        for (int i = 0; i < array.length-1; i++) {
            s.append(array[i]);
            s.append(", ");
        }
        if (array.length > 0)
            s.append(array[array.length-1]);
        s.append(']');
        return s.toString();
    }
    public boolean equals(Object that){
    if (!(that instanceof HeapArray))
        return false;
    HeapArray h = (HeapArray)that;
    if (size != h.getSize()){
        //System.out.print("(sizes: " + size + ":" + h.getSize() + ")");
        return false;
    }
    //System.out.print("(size ok)");
    Comparable []a = h.getArray();
    for(int i = 0; i < size; i++){
        if (array[i].compareTo(a[i]) != 0)
        return false;
    }
    return true;
    }
    */
    /*KORAT_END*/
}
