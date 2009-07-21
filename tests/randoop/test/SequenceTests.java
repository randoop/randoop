package randoop.test;

import java.io.InputStream;

import junit.framework.TestCase;

public class SequenceTests extends TestCase {

  public void test1() throws Exception {
    if (true) return;
    InputStream stream = SequenceTests.class.getResourceAsStream("resources/sequence_tests_script.txt");
    try{
      SequenceTester.test(stream);
    } finally {
      stream.close();
    }
  }

}
