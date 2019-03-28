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
import javax.swing.event.*;
import javax.swing.table.*;

import java.util.*;
import java.awt.*;
import java.awt.event.*;

public class SharedModelDemo extends JPanel {
    JTextArea output;
    JList list; 
    JTable table;
    String newline = "\n";
    ListSelectionModel listSelectionModel;

    public SharedModelDemo() {
        super(new BorderLayout());

        Vector data = new Vector(7);
        String[] columnNames = { "French", "Spanish", "Italian" };
        String[] oneData =     { "un",     "uno",     "uno"     };
        String[] twoData =     { "deux",   "dos",     "due"     };
        String[] threeData =   { "trois",  "tres",    "tre"     };
        String[] fourData =    { "quatre", "cuatro",  "quattro" };
        String[] fiveData =    { "cinq",   "cinco",   "cinque"  };
        String[] sixData =     { "six",    "seis",    "sei"     };
        String[] sevenData =   { "sept",   "siete",   "sette"   };

        //Build the model.
        SharedDataModel dataModel = new SharedDataModel(columnNames);
        dataModel.addElement(oneData);
        dataModel.addElement(twoData);
        dataModel.addElement(threeData);
        dataModel.addElement(fourData);
        dataModel.addElement(fiveData);
        dataModel.addElement(sixData);
        dataModel.addElement(sevenData);

        list = new JList(dataModel);
        list.setCellRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList l, 
                                                          Object value,
                                                          int i,
                                                          boolean s,
                                                          boolean f) {
                String[] array = (String[])value;
                return super.getListCellRendererComponent(l,
                                                          array[0],
                                                          i, s, f);
            }
        });

        listSelectionModel = list.getSelectionModel();
        listSelectionModel.addListSelectionListener(
                                new SharedListSelectionHandler());
        JScrollPane listPane = new JScrollPane(list);

        table = new JTable(dataModel);
        table.setSelectionModel(listSelectionModel);
        JScrollPane tablePane = new JScrollPane(table);

        //Build control area (use default FlowLayout).
        JPanel controlPane = new JPanel();
        String[] modes = { "SINGLE_SELECTION",
                           "SINGLE_INTERVAL_SELECTION",
                           "MULTIPLE_INTERVAL_SELECTION" };

        final JComboBox comboBox = new JComboBox(modes);
        comboBox.setSelectedIndex(2);
        comboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String newMode = (String)comboBox.getSelectedItem();
                if (newMode.equals("SINGLE_SELECTION")) {
                    listSelectionModel.setSelectionMode(
                        ListSelectionModel.SINGLE_SELECTION);
                } else if (newMode.equals("SINGLE_INTERVAL_SELECTION")) {
                    listSelectionModel.setSelectionMode(
                        ListSelectionModel.SINGLE_INTERVAL_SELECTION);
                } else {
                    listSelectionModel.setSelectionMode(
                        ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
                }
                output.append("----------"
                              + "Mode: " + newMode
                              + "----------" + newline);
            }
        });
        controlPane.add(new JLabel("Selection mode:"));
        controlPane.add(comboBox);

        //Build output area.
        output = new JTextArea(10, 40);
        output.setEditable(false);
        JScrollPane outputPane = new JScrollPane(output,
                         ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                         ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

        //Do the layout.
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        add(splitPane, BorderLayout.CENTER);

        JPanel topHalf = new JPanel();
        topHalf.setLayout(new BoxLayout(topHalf, BoxLayout.X_AXIS));
        JPanel listContainer = new JPanel(new GridLayout(1,1));
        listContainer.setBorder(BorderFactory.createTitledBorder(
                                                "List"));
        listContainer.add(listPane);
        JPanel tableContainer = new JPanel(new GridLayout(1,1));
        tableContainer.setBorder(BorderFactory.createTitledBorder(
                                                "Table"));
        tableContainer.add(tablePane);
        tablePane.setPreferredSize(new Dimension(300, 100));
        topHalf.setBorder(BorderFactory.createEmptyBorder(5,5,0,5));
        topHalf.add(listContainer);
        topHalf.add(tableContainer);

        topHalf.setMinimumSize(new Dimension(400, 50));
        topHalf.setPreferredSize(new Dimension(400, 110));
        splitPane.add(topHalf);

        JPanel bottomHalf = new JPanel(new BorderLayout());
        bottomHalf.add(controlPane, BorderLayout.NORTH);
        bottomHalf.add(outputPane, BorderLayout.CENTER);
        //XXX: next line needed if bottomHalf is a scroll pane:
        //bottomHalf.setMinimumSize(new Dimension(400, 50));
        bottomHalf.setPreferredSize(new Dimension(450, 135));
        splitPane.add(bottomHalf);
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("SharedModelDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        JComponent newContentPane = new SharedModelDemo();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

    class SharedListSelectionHandler implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) { 
            ListSelectionModel lsm = (ListSelectionModel)e.getSource();

            int firstIndex = e.getFirstIndex();
            int lastIndex = e.getLastIndex();
            boolean isAdjusting = e.getValueIsAdjusting(); 
            output.append("Event for indexes "
                          + firstIndex + " - " + lastIndex
                          + "; isAdjusting is " + isAdjusting
                          + "; selected indexes:");

            if (lsm.isSelectionEmpty()) {
                output.append(" <none>");
            } else {
                // Find out which indexes are selected.
                int minIndex = lsm.getMinSelectionIndex();
                int maxIndex = lsm.getMaxSelectionIndex();
                for (int i = minIndex; i <= maxIndex; i++) {
                    if (lsm.isSelectedIndex(i)) {
                        output.append(" " + i);
                    }
                }
            }
            output.append(newline);
        }
    }

    class SharedDataModel extends DefaultListModel
                          implements TableModel {
        public String[] columnNames;

        public SharedDataModel(String[] columnNames) {
            super();
            this.columnNames = columnNames;
        }

        public void rowChanged(int row) {
            fireContentsChanged(this, row, row); 
        }

        private TableModel tableModel = new AbstractTableModel() {
            public String getColumnName(int column) {
                return columnNames[column];
            }
            public int getRowCount() { 
                return size();
            }
            public int getColumnCount() {
                return columnNames.length;
            }
            public Object getValueAt(int row, int column) {
                String[] rowData = (String [])elementAt(row);
                return rowData[column];
            }
            public boolean isCellEditable(int row, int column) {
                return true;
            }
            public void setValueAt(Object value, int row, int column) {
                String newValue = (String)value;
                String[] rowData = (String [])elementAt(row);
                rowData[column] = newValue;
                fireTableCellUpdated(row, column); //table event
                rowChanged(row);                   //list event
            }
        };

        //Implement the TableModel interface.
        public int getRowCount() {
            return tableModel.getRowCount();
        }
        public int getColumnCount() {
            return tableModel.getColumnCount();
        }
        public String getColumnName(int columnIndex) {
            return tableModel.getColumnName(columnIndex);
        }
        public Class getColumnClass(int columnIndex) {
            return tableModel.getColumnClass(columnIndex);
        }
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return tableModel.isCellEditable(rowIndex, columnIndex);
        }
        public Object getValueAt(int rowIndex, int columnIndex) {
            return tableModel.getValueAt(rowIndex, columnIndex);
        }
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            tableModel.setValueAt(aValue, rowIndex, columnIndex);
        }
        public void addTableModelListener(TableModelListener l) {
            tableModel.addTableModelListener(l);
        }
        public void removeTableModelListener(TableModelListener l) {
            tableModel.removeTableModelListener(l);
        }
    }
}
