package components;

import javax.swing.*;

/**
 * This duplicates {@link DialogDemo#main(String[])} so that Randoop can call it.
 */
public class DialogRunner {
  public static void runDialogDemo() {
    JFrame frame = new JFrame("TestFrame");

    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    //Create and set up the content pane.
    DialogDemo newContentPane = new DialogDemo(frame);
    newContentPane.setOpaque(true); //content panes must be opaque
    frame.setContentPane(newContentPane);

    //Display the window.
    frame.pack();
    frame.setVisible(true);
  }
}
