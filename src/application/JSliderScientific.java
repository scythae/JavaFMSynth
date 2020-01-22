package application;

import utils.Utils;

public class JSliderScientific extends JLabeledSlider {
	private static final long serialVersionUID = 4285476851687514518L;

	private double minimum = 0;
	private double maximum = 1;
	private double exponent = 1;

	public JSliderScientific() {
		super();
		init();
	}

	public JSliderScientific(double minimum, double maximum, double value, double exponent) {
		super();
		init();
		this.minimum = minimum;
		this.maximum = maximum;
		setExponent(exponent);
		setValueSc(value);
	}

	private void init() {
		setMinMax(0, 100000);
		setValue(0);
	}

	public void setExponent(double exponent) {
		if (Double.compare(exponent, 0) == 0)
			Utils.complain("Exponent should have non-zero value.");
		else
			this.exponent = exponent;
	}

	public double getValueSc() {
		return minimum + Math.pow(getNormalizedValue(), exponent) * (maximum - minimum);
	}

	public void setValueSc(double value) {
		double normalizedValue = Math.pow((value - minimum) / (maximum - minimum), 1 / exponent);
		setNormalizedValue(normalizedValue);
	}

	private double getNormalizedValue() {
		double val, min, max;
		val = getValue();
		min = getMinimum();
		max = getMaximum();

		return (val - min) / (max - min);
	}

	private void setNormalizedValue(double normalizedValue) {
		if (normalizedValue < 0 || normalizedValue > 1) {
			Utils.complain("Normalized value should be in a range between 0 and 1.");
			return;
		}

		double value = normalizedValue * (getMaximum() - getMinimum());
		setValue(getMinimum() + (int) Math.round(value));
	}

	public void setMinMax(int min, int max) {
		setMinimum(min);
		setMaximum(max);
	}

	@Override
	public String getValueForLabel() {
		return "" + getValueSc();
	}
}
