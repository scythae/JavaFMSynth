package application.swingUI.dialogs;

import javax.swing.JOptionPane;

public class TextInputDialog {
	public interface OnInputValidation {
		boolean isValid(String inputText);
	}

	public String title = "Synth";
	public String prompt = "";
	public String initialValue = "";
	public OnInputValidation onInputValidation;

	public String getInput() {
		String result = initialValue;
		do {
			result = JOptionPane.showInputDialog(prompt, result);
		} while (result != null && onInputValidation != null && !onInputValidation.isValid(result));

		return result;
	}
}
