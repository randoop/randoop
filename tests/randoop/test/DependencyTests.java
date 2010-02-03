package randoop.test;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import randoop.DependencyUtils;
import randoop.RecordListReader;
import randoop.RecordProcessor;
import randoop.Sequence;
import utilpag.Pair;

public class DependencyTests extends TestCase {

  
  public void test() throws URISyntaxException {
    
    URL url = DependencyTests.class.getResource("resources/dependencytests.txt");
    String filename = new File(url.toURI()).getAbsolutePath();
    List<Pair<Sequence,Integer>> inputs = parseInputs(filename);

    url = DependencyTests.class.getResource("resources/dependencytests-output.txt");
    filename = new File(url.toURI()).getAbsolutePath();
    List<Sequence> outputs = new ArrayList<Sequence>();
    Sequence.readTextSequences(filename, outputs);

    assertEquals(inputs.size(), outputs.size());

    for (int i = 0 ; i < inputs.size() ; i++) {
      Pair<Sequence,Integer> inputpair = inputs.get(i);
      Sequence input = inputpair.a;
      int index = inputpair.b;
      Sequence output = outputs.get(i);
      
      Sequence pred = DependencyUtils.predecessorSequence(input, index);
      
      System.out.println(DependencyUtils.longestDepSet(input));
      System.out.println(">>" + DependencyUtils.longestDepSet(input));
      System.out.println(">>>" + DependencyUtils.getLongestDepSetSubSequence(input));

      String msg = "\nFAILURE ON INPUT " + i + "\n\n" +
      "INDEX:" + index + 
        "\n\nINPUT:\n\n" + input + "\n\nPRED:\n\n" + pred.toString() + "\n\nOUTPUT:" + output;
      assertTrue(msg, pred.equals(output));
    }
    
    System.out.println("DependencyTests: ran " + inputs.size() + " tests.");
  }
  
 public static List<Pair<Sequence,Integer>> parseInputs(String inFile) {
   final List<Pair<Sequence,Integer>> inputs = new ArrayList<Pair<Sequence,Integer>>();  
    RecordProcessor processor = new RecordProcessor() {
      public void processRecord(List<String> lines) {
        inputs.add(parseRecord(lines));
      }
    };
    RecordListReader reader = new RecordListReader("RECORD", processor);
    reader.parse(inFile);
    return inputs;
  }
    
  
  private static Pair<Sequence, Integer> parseRecord(List<String> lines) {
    try {
      assert lines.get(0).equals("INDEX");
      Integer idx = Integer.parseInt(lines.get(1));
      assert lines.get(2).equals("SEQUENCE");
      // Line indices 3--end is the sequence.
      Sequence sequence = Sequence.parse(lines.subList(3, lines.size()));
      return new Pair<Sequence,Integer>(sequence, idx);
    } catch (Exception e) {
      throw new Error(e);
    }
  }

  
}
