package application.swingUI.dialogs;

import javax.swing.JOptionPane;

public class ConfirmationDialog {
	public static boolean confirmed(String question) {
		return JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(
			null, question, "Synth", JOptionPane.OK_CANCEL_OPTION
		);
	}
}
