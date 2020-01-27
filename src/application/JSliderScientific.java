package application;

import java.awt.event.MouseWheelEvent;
import java.text.NumberFormat;

import utils.Utils;

public class JSliderScientific extends JLabeledSlider {
	private static final long serialVersionUID = 4285476851687514518L;

	private double minimum = 0;
	private double maximum = 1;
	private double exponent = 1;

	private NumberFormat labelFormat = NumberFormat.getInstance();

	public JSliderScientific() {
		super();
		init(minimum, maximum, minimum, exponent);
	}

	public JSliderScientific(double minimum, double maximum, double value, double exponent) {
		super();
		init(minimum, maximum, value, exponent);
	}

	private void init(double minimum, double maximum, double value, double exponent) {
		setMinMax(0, 100000);
		this.minimum = minimum;
		this.maximum = maximum;
		setExponent(exponent);
		setValueSc(value);

		this.addMouseWheelListener((MouseWheelEvent e) -> {
			float step = Math.abs(getMaximum() - getMinimum()) / 10000;
			if (step < 1)
				step = 1;

			if (e.isControlDown() && e.isShiftDown())
				step *= 1000;
			else if (e.isControlDown())
				step *= 100;
			else if (e.isShiftDown())
				step *= 10;
			else
				step = 1;

			setValue(getValue() + e.getWheelRotation() * Math.round(step));
		});
	}

	public void setExponent(double exponent) {
		if (Utils.doubleEquals(exponent, 0))
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

	public void setRounding(int maxFractionDigits) {
		labelFormat.setMaximumFractionDigits(maxFractionDigits);
		fireStateChanged();
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
			Utils.complain("Normalized value should be in a range between 0 and 1. Value provided: " + normalizedValue);
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
		if (labelFormat == null)
			labelFormat = NumberFormat.getInstance();

		return labelFormat.format(getValueSc());
	}
}
