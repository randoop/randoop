package instrument;

import static org.junit.Assert.assertTrue;

import components.DialogDemo;
import input.SystemExitClass;
import javax.swing.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import randoop.SystemExitCalledError;

/** Tests the replacecall agent. */
public class CallReplacementTest {

  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void systemExitTest() {

    SystemExitClass obj = new SystemExitClass(5);
    obj.checkValue(5);
    assertTrue("this should pass", true);

    int value = 0;
    thrown.expect(SystemExitCalledError.class);
    String expected = String.format("System exit(%d) ignored", value);
    thrown.expectMessage(expected);
    obj.checkValue(value);
  }

  // code borrowed from {@code createAndShowGUI()} in Oracle example components.DialogDemo
  @Test
  public void swingTest() {
    thrown = ExpectedException.none();

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
