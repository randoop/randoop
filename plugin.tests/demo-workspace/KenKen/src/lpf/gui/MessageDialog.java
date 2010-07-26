package lpf.gui;

import javax.swing.JOptionPane;

/**
 * This class is for common message/confirm dialog
 * @author Wang Han
 * @author Nam Do
 * 
 */
public class MessageDialog {

	/**
	 * Alert Dialog with Yes & No option
	 * @param msg
	 * @return 	true if user selects Yes
	 * 			false if user selects No
	 */
	/*  */
	public static boolean showAlertDlg(String msg){
		Object[] options = {"Yes","No"};
		
		if (JOptionPane.showOptionDialog(null, msg, "Alert", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[1]) == JOptionPane.YES_OPTION) {
			return true;
		} else {
			return false;
		}
	}
}
