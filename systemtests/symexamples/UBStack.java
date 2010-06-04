package symexamples;

import java.io.*;
import java.awt.*;

public class UBStack {
    private int [] elems;
    private int numberOfElements;
    private int max;

    public UBStack() {
        numberOfElements = 0;
        max = 5;
        elems = new int[max];
    }

    public void push(int k) {
        int index;
        boolean alreadyMember;

        alreadyMember = false;

        for(index=0; index<numberOfElements; index++) {
            if(k == elems[index]) {
                alreadyMember = true;
                break;
            }
        }

        if (alreadyMember) {
            for (int j=index; j<numberOfElements-1; j++) {
                elems[j] = elems[j+1];
            }
            elems[numberOfElements-1] = k;
        }
        else {
            if (numberOfElements < max) {
                elems[numberOfElements] = k;
                numberOfElements++;
                return;
            } else {
                //System.out.println("Stack full, cannot push");
                return;
            }
        }
    }

    public void pop() {
        numberOfElements --;
    }


    public int top() {
        if (numberOfElements < 1) {
            //System.out.println("Empty Stack");
            return -1;
        } else
            return elems[numberOfElements-1];
    }

    public boolean isEmpty() {
        if (numberOfElements==0)
            return true;
        else
            return false;
    }

    public int maxSize() {
        return max;
    }

    public boolean isMember(int k) {
        for(int index=0; index<numberOfElements; index++)
            if( k==elems[index])
                return true;
        return false;
    }

    public boolean equals(UBStack s) {
        if (s.maxSize() != max)
            return false;
        if (s.getNumberOfElements() != numberOfElements)
            return false;
        int [] sElems = s.getArray();
        for (int j=0; j<numberOfElements; j++)    {
            if ( elems[j] != sElems[j])
                return false;
        }
        return true;
    }


    public int[] getArray() {
        int [] a;
        a = new int[max];
        for (int j=0; j<numberOfElements; j++)
            a[j] = elems[j];
        return a;
    }

    public int getNumberOfElements() {
        return numberOfElements;
    }

    public boolean isFull() {
        return numberOfElements == max;
    }

//    public String toString() {
//        if (1==1) throw new RuntimeException();
//        StringBuilder b = new StringBuilder();
//        b.append("[");
//        for (int i = 0 ; i < this.numberOfElements ; i++) {
//            b.append(" " + this.elems[i]);
//        }
//        b.append("]");
//        return b.toString();
//    }
}
