package application;

import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;

public class JLabeledSlider extends JSlider {
	private static final long serialVersionUID = -7462953376272524277L;
	private JLabel label;

	public JLabeledSlider() {
		super();

		label = new JLabel();
		label.setBorder(new EmptyBorder(0, 20, 0, 0));
		addChangeListener((ChangeEvent e) -> {
			label.setText(getValueForLabel());
		});
		fireStateChanged();
	}

	public JLabel getLabel() {
		return label;
	}

	protected String getValueForLabel() {
		return "" + getValue();
	}
}