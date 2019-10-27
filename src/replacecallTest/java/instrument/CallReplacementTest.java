package instrument;

import static org.junit.Assert.assertTrue;

import components.DialogDemo;
import input.SystemExitClass;
import javax.swing.*;
import org.junit.Rule;
import org.junit.Test;
import randoop.SystemExitCalledError;

/** Tests the replacecall agent. */
@SuppressWarnings("deprecation") // ExpectedException deprecated in JUnit 4.12, replaced in 4.13.
public class CallReplacementTest {

  @Rule public org.junit.rules.ExpectedException thrown = org.junit.rules.ExpectedException.none();

  @Test
  public void systemExitTest() {

    SystemExitClass obj = new SystemExitClass(5);
    obj.checkValue(5);
    assertTrue("this should pass", true);

    int value = 0;
    thrown.expect(SystemExitCalledError.class);
    String expected =
        String.format("Call to System exit(%d) detected; terminating execution", value);
    thrown.expectMessage(expected);
    obj.checkValue(value);
  }

  // code borrowed from {@code createAndShowGUI()} in Oracle example components.DialogDemo
  @Test
  public void swingTest() {
    JFrame frame = new JFrame("TestFrame");

    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    // Create and set up the content pane.
    DialogDemo newContentPane = new DialogDemo(frame);
    newContentPane.setOpaque(true); // content panes must be opaque
    frame.setContentPane(newContentPane);

    // Display the window.
    frame.pack();

    frame.setVisible(true);
  }
}
