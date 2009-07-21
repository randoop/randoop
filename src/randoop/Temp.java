package randoop;

import java.io.IOException;
import java.util.List;

import randoop.Sequence;
import randoop.util.Files;


public class Temp {

  public static void main(String[] args) throws IOException {
        
    List<String> lines = Files.readWhole(args[0]);
    
    Sequence s = Sequence.parse(lines);
    
    String str = s.toCodeString();
    
    str = str.replaceAll("java2.util2.", "");
    str = str.replaceAll("java.lang.", "");

    System.out.println(str);
    
  }
  
}
