/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */ 

package components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

/*
 * FrameDemo2.java shows off the window decoration features added in
 * 1.4, plus some window positioning code and (optionally)
 * setIconImage. It uses the file images/FD.jpg.
 */
public class FrameDemo2 extends WindowAdapter
                        implements ActionListener {
    private Point lastLocation = null;
    private int maxX = 500;
    private int maxY = 500;

    //the main frame's default button
    private static JButton defaultButton = null;

    //constants for action commands
    protected final static String NO_DECORATIONS = "no_dec";
    protected final static String LF_DECORATIONS = "laf_dec";
    protected final static String WS_DECORATIONS = "ws_dec";
    protected final static String CREATE_WINDOW = "new_win";
    protected final static String DEFAULT_ICON = "def_icon";
    protected final static String FILE_ICON = "file_icon";
    protected final static String PAINT_ICON = "paint_icon";

    //true if the next frame created should have no window decorations
    protected boolean noDecorations = false;

    //true if the next frame created should have setIconImage called
    protected boolean specifyIcon = false;

    //true if the next frame created should have a custom painted icon
    protected boolean createIcon = false;

    //Perform some initialization.
    public FrameDemo2() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        maxX = screenSize.width - 50;
        maxY = screenSize.height - 50;
    }

    //Create a new MyFrame object and show it.
    public void showNewWindow() {
        JFrame frame = new MyFrame();

        //Take care of the no window decorations case.
        //NOTE: Unless you really need the functionality
        //provided by JFrame, you would usually use a
        //Window or JWindow instead of an undecorated JFrame.
        if (noDecorations) {
            frame.setUndecorated(true);
        }

        //Set window location.
        if (lastLocation != null) {
            //Move the window over and down 40 pixels.
            lastLocation.translate(40, 40);
            if ((lastLocation.x > maxX) || (lastLocation.y > maxY)) {
                lastLocation.setLocation(0, 0);
            }
            frame.setLocation(lastLocation);
        } else {
            lastLocation = frame.getLocation();
        }

        //Calling setIconImage sets the icon displayed when the window
        //is minimized.  Most window systems (or look and feels, if
        //decorations are provided by the look and feel) also use this
        //icon in the window decorations.
        if (specifyIcon) {
            if (createIcon) {
                frame.setIconImage(createFDImage()); //create an icon from scratch
            } else {
                frame.setIconImage(getFDImage());    //get the icon from a file
            }
        }

        //Show window.
        frame.setSize(new Dimension(170, 100));
        frame.setVisible(true);
    }

    // Create the window-creation controls that go in the main window.
    protected JComponent createOptionControls() {
        JLabel label1 = new JLabel("Decoration options for subsequently created frames:");
        ButtonGroup bg1 = new ButtonGroup();
        JLabel label2 = new JLabel("Icon options:");
        ButtonGroup bg2 = new ButtonGroup();

        //Create the buttons
        JRadioButton rb1 = new JRadioButton();
        rb1.setText("Look and feel decorated");
        rb1.setActionCommand(LF_DECORATIONS);
        rb1.addActionListener(this);
        rb1.setSelected(true);
        bg1.add(rb1);
        //
        JRadioButton rb2 = new JRadioButton();
        rb2.setText("Window system decorated");
        rb2.setActionCommand(WS_DECORATIONS);
        rb2.addActionListener(this);
        bg1.add(rb2);
        //
        JRadioButton rb3 = new JRadioButton();
        rb3.setText("No decorations");
        rb3.setActionCommand(NO_DECORATIONS);
        rb3.addActionListener(this);
        bg1.add(rb3);
        //
        //
        JRadioButton rb4 = new JRadioButton();
        rb4.setText("Default icon");
        rb4.setActionCommand(DEFAULT_ICON);
        rb4.addActionListener(this);
        rb4.setSelected(true);
        bg2.add(rb4);
        //
        JRadioButton rb5 = new JRadioButton();
        rb5.setText("Icon from a JPEG file");
        rb5.setActionCommand(FILE_ICON);
        rb5.addActionListener(this);
        bg2.add(rb5);
        //
        JRadioButton rb6 = new JRadioButton();
        rb6.setText("Painted icon");
        rb6.setActionCommand(PAINT_ICON);
        rb6.addActionListener(this);
        bg2.add(rb6);

        //Add everything to a container.
        Box box = Box.createVerticalBox();
        box.add(label1);
        box.add(Box.createVerticalStrut(5)); //spacer
        box.add(rb1);
        box.add(rb2);
        box.add(rb3);
        //
        box.add(Box.createVerticalStrut(15)); //spacer
        box.add(label2);
        box.add(Box.createVerticalStrut(5)); //spacer
        box.add(rb4);
        box.add(rb5);
        box.add(rb6);

        //Add some breathing room.
        box.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        return box;
    }

    //Create the button that goes in the main window.
    protected JComponent createButtonPane() {
        JButton button = new JButton("New window");
        button.setActionCommand(CREATE_WINDOW);
        button.addActionListener(this);
        defaultButton = button; //Used later to make this the frame's default button.

        //Center the button in a panel with some space around it.
        JPanel pane = new JPanel(); //use default FlowLayout
        pane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        pane.add(button);

        return pane;
    }

    //Handle action events from all the buttons.
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        //Handle the New window button.
        if (CREATE_WINDOW.equals(command)) {
            showNewWindow();

        //Handle the first group of radio buttons.
        } else if (NO_DECORATIONS.equals(command)) {
            noDecorations = true;
            JFrame.setDefaultLookAndFeelDecorated(false);
        } else if (WS_DECORATIONS.equals(command)) {
            noDecorations = false;
            JFrame.setDefaultLookAndFeelDecorated(false);
        } else if (LF_DECORATIONS.equals(command)) {
            noDecorations = false;
            JFrame.setDefaultLookAndFeelDecorated(true);

        //Handle the second group of radio buttons.
        } else if (DEFAULT_ICON.equals(command)) {
            specifyIcon = false;
        } else if (FILE_ICON.equals(command)) {
            specifyIcon = true;
            createIcon = false;
        } else if (PAINT_ICON.equals(command)) {
            specifyIcon = true;
            createIcon = true;
        }
    }

    //Creates an icon-worthy Image from scratch.
    protected static Image createFDImage() {
        //Create a 16x16 pixel image.
        BufferedImage bi = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);

        //Draw into it.
        Graphics g = bi.getGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, 15, 15);
        g.setColor(Color.RED);
        g.fillOval(5, 3, 6, 6);

        //Clean up.
        g.dispose();

        //Return it.
        return bi;
    }

    //Returns an Image or null.
    protected static Image getFDImage() {
        java.net.URL imgURL = FrameDemo2.class.getResource("images/FD.jpg");
        if (imgURL != null) {
            return new ImageIcon(imgURL).getImage();
        } else {
            return null;
        }
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        //Use the Java look and feel.
        try {
            UIManager.setLookAndFeel(
                UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) { }

        //Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);

        //Instantiate the controlling class.
        JFrame frame = new JFrame("FrameDemo2");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        FrameDemo2 demo = new FrameDemo2();

        //Add components to it.
        Container contentPane = frame.getContentPane();
        contentPane.add(demo.createOptionControls(),
                        BorderLayout.CENTER);
        contentPane.add(demo.createButtonPane(),
                        BorderLayout.PAGE_END);
        frame.getRootPane().setDefaultButton(defaultButton);

        //Display the window.
        frame.pack();
        frame.setLocationRelativeTo(null); //center it
        frame.setVisible(true);
    }

    //Start the demo.
    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

    class MyFrame extends JFrame implements ActionListener {

        //Create a frame with a button.
        public MyFrame() {
            super("A window");
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);

            //This button lets you close even an undecorated window.
            JButton button = new JButton("Close window");
            button.addActionListener(this);

            //Place the button near the bottom of the window.
            Container contentPane = getContentPane();
            contentPane.setLayout(new BoxLayout(contentPane,
                                                BoxLayout.PAGE_AXIS));
            contentPane.add(Box.createVerticalGlue()); //takes all extra space
            contentPane.add(button);
            button.setAlignmentX(Component.CENTER_ALIGNMENT); //horizontally centered
            contentPane.add(Box.createVerticalStrut(5)); //spacer
        }

        //Make the button do the same thing as the default close operation
        //(DISPOSE_ON_CLOSE).
        public void actionPerformed(ActionEvent e) {
            setVisible(false);
            dispose();
        }
    }
}
