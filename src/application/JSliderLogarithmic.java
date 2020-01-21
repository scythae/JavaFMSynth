package application;

import javax.swing.JSlider;

public class JSliderLogarithmic extends JSlider {
	private static final long serialVersionUID = 4285476851687514518L;

	private double exponent = Math.E;

	public JSliderLogarithmic() {
		super();
		setMinimum(0);
		setMaximum(100);
		setValue(0);
	}

	public void setExponent(double exponent) {
		this.exponent = exponent;
	}

	public double getValueLog() {
		double val, min, max;
		val = getValue();
		min = getMinimum();
		max = getMaximum();

		double result = Math.pow((val - min) / (max - min), exponent);
		return result;
	}

	public void setValueLog(double valueLog) {
		double value = Math.pow(valueLog, 1 / exponent) * (getMaximum() - getMinimum());
		setValue(getMinimum() + (int) Math.round(value));
	}
}
