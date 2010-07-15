package mcdrivertest;

public class Bam {
    
    public Bam(Baz b) {
        // no body.
    }
    
    @Override
    public String toString() {
        throw new RuntimeException();
    }
    
    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }
    
    @Override
    public int hashCode() {
        throw new RuntimeException();
    }

}
