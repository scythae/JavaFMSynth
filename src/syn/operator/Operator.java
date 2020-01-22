package syn.operator;

import java.util.ArrayList;
import java.util.List;

import syn.Note;
import utils.Utils;

public class Operator {
	public static final double minLevel = 0;
	public static final double maxLevel = 1;
	public static final int maxDetune = 50;
	public static final int minFixedFrequency = 0;
	public static final int maxFixedFrequency = 20000;
	public static final int minProportionalFrequency = 0;
	public static final int maxProportionalFrequency = 32;

	private OperatorValues values = new OperatorValues();

	private boolean isModulator = false;


	public double getSampleValue(Note note) {
		/* Value of carrier, modulated by modulator:
		 *
		 * carrierVal = sin(Wc * time + (Wc / Wm) * sin(Wm * time))
		 *
		 * Wc, Wm - angular frequencies of carrier and modulator.
		 * W = 2Pi * frequency;
		 * */

		double frequency = values.frequencyFixed ? values.frequencyLevel : values.frequencyLevel * (note.frequency + values.detune);
		double angularFrequency = frequency * Utils.doublePi;

		double modulation = 0;
		List<Operator> modulators = values.modulators;
		for (Operator modulator : modulators)
				modulation += modulator.getSampleValue(note);

		double phase = angularFrequency * (note.timeSinceHit + modulation);

		double result = values.oscillator.getSampleValue(phase) * values.level;
		if (isModulator)
			result = result / angularFrequency;

		return result;
	};

	public Operator setOscillator(Oscillator oscillator) {
		if (oscillator == null)
			Utils.complain("Operator requires non-null oscillator.");
		else
			values.oscillator = oscillator;

		return this;
	};

	public synchronized Operator addModulator(Operator modulator) {
		List<Operator> modulators = new ArrayList<>(values.modulators.size() + 1);
		modulators.addAll(values.modulators);

		if (modulator != null) {
			modulator.isModulator = true;
			modulators.add(modulator);
		}
		values.modulators = modulators;

		return this;
	};

	public synchronized void cleanModulators() {
		values.modulators = OperatorValues.emptyOperatorList;
	};

	public Operator setLevel(double level) {
		if (level < minLevel || level > maxLevel)
			Utils.complain("Operator's level's range should be from " + minLevel + " to " + maxLevel + ".");
		else
			values.level = level;

		return this;
	};

	public Operator setDetune(double detune) {
		if (detune < -maxDetune || detune > maxDetune)
			Utils.complain("Operator's detune should be in a range between -" + maxDetune + " Hz and " + maxDetune + " Hz.");
		else
			values.detune = detune;

		return this;
	};

	public Operator setFrequencyFixed(double frequency) {
		if (frequency < minFixedFrequency || frequency > maxFixedFrequency)
			Utils.complain("Operator's fixed frequency should be in a range between " + minFixedFrequency + " Hz and " + maxFixedFrequency + " Hz.");
		else {
			values.frequencyLevel = frequency;
			values.frequencyFixed = true;
		}

		return this;
	};

	public Operator setFrequencyProportional(double frequency) {
		if (frequency < minProportionalFrequency || frequency > maxProportionalFrequency)
			Utils.complain("Operator's proportional frequency should be in a range between " + minProportionalFrequency + " times and " + maxProportionalFrequency + " times.");
		else {
			values.frequencyLevel = frequency;
			values.frequencyFixed = false;
		}

		return this;
	};

	public OperatorValues getOperatorValues() {
		OperatorValues result = new OperatorValues();

		result.detune = values.detune;
		result.frequencyFixed = values.frequencyFixed;
		result.frequencyLevel = values.frequencyLevel;
		result.level = values.level;
		result.oscillator = values.oscillator;
		result.modulators = new ArrayList<>(values.modulators);

		return result;
	}

	@Override
	public String toString() {
		if (isModulator)
			return "Modulator";
		else
			return "Carrier";
	}
}
