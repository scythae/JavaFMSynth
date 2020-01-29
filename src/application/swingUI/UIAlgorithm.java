package application.swingUI;

import javax.swing.JComponent;

import syn.operator.Algorithm;
import syn.operator.Operator;

public abstract class UIAlgorithm {
	public interface OnAlgorithmOperatorChange {
		void execute(Algorithm algorithm, Operator operator, boolean connecting);
	}
	public interface OnOperatorSelected {
		void execute(Operator operator);
	}

	public OnAlgorithmOperatorChange onAlgorithmOperatorChange;
	public OnOperatorSelected onOperatorSelected;

	public abstract void setAlgorithm(Algorithm algorithm);
	public abstract void refresh();
	public abstract JComponent getMainComponent();
}
