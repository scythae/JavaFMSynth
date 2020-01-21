package syn.operator;

import java.util.ArrayList;
import java.util.List;

import syn.Note;
import utils.Log;
import utils.Utils;

public class Operator {
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
			Log.out("Operator requires non-null oscillator.");
		else
			values.oscillator = oscillator;

		return this;
	};

	public Operator addModulator(Operator modulator) {
		List<Operator> modulators = new ArrayList<>(values.modulators.size() + 1);
		modulators.addAll(values.modulators);

		if (modulator != null) {
			modulator.isModulator = true;
			modulators.add(modulator);
		}
		values.modulators = modulators;

		return this;
	};

	public Operator setLevel(double level) {
		if (level < 0 || level > 1)
			Log.out("Operator's level's range should be from 0 to 1.");
		else
			values.level = level;

		return this;
	};

	public Operator setDetune(double detune) {
		if (detune < -50 || detune > 50)
			Log.out("Operator's detune should be in a range between -50 Hz and 50 Hz.");
		else
			values.detune = detune;

		return this;
	};

	public Operator setFrequencyFixed(double frequency) {
		if (frequency < 0 || frequency > 20000)
			Log.out("Operator's fixed frequency should be in a range between 0 Hz and 20000 Hz.");
		else {
			values.frequencyLevel = frequency;
			values.frequencyFixed = true;
		}

		return this;
	};

	public Operator setFrequencyProportional(double frequency) {
		if (frequency < 0 || frequency > 32)
			Log.out("Operator's proportional frequency should be in a range between 0 times and 32 times.");
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
		return "Operator";
	}
}
