package syn.operator;

import syn.Note;
import utils.Log;
import utils.Utils;

public class Operator {
	private Oscillator oscillator = Oscillators.Sine;
	private double level = 1;
	private double detune = 0;
	private double frequencyLevel = 1.0;
	private boolean frequencyFixed = false;
	private Operator modulator = null;
	private boolean isModulator = false;

	public double getSampleValue(Note note) {
		/* Value of carrier, modulated by modulator:
		 *
		 * carrierVal = sin(Wc * time + (Wc / Wm) * sin(Wm * time))
		 *
		 * Wc, Wm - angular frequencies of carrier and modulator.
		 * W = 2Pi * frequency;
		 * */

		double frequency = frequencyFixed ? frequencyLevel : frequencyLevel * (note.frequency + detune);
		double angularFrequency = frequency * Utils.doublePi;
		double modulation = (modulator == null) ? 0 : modulator.getSampleValue(note);
		double phase = angularFrequency * (note.timeSinceHit + modulation);

		double result = oscillator.getSampleValue(phase) * level;
		if (isModulator)
			result = result / angularFrequency;

		return result;
	};

	public Operator setOscillator(Oscillator oscillator) {
		if (oscillator == null)
			Log.out("Operator requires non-null oscillator.");
		else
			this.oscillator = oscillator;

		return this;
	};

	public Operator setModulator(Operator modulator) {
		this.modulator = modulator;
		if (modulator != null)
			modulator.isModulator = true;

		return this;
	};

	public Operator setLevel(double level) {
		if (level < 0 || level > 1)
			Log.out("Operator's level's range should be from 0 to 1.");
		else
			this.level = level;

		return this;
	};

	public Operator setDetune(double detune) {
		if (detune < -50 || detune > 50)
			Log.out("Operator's detune should be in a range between -50 Hz and 50 Hz.");
		else
			this.detune = detune;

		return this;
	};

	public Operator setFrequencyFixed(double frequency) {
		if (frequency < 0 || frequency > 20000)
			Log.out("Operator's fixed frequency should be in a range between 0 Hz and 20000 Hz.");
		else {
			frequencyLevel = frequency;
			frequencyFixed = true;
		}

		return this;
	};

	public Operator setFrequencyProportional(double frequency) {
		if (frequency < 0 || frequency > 32)
			Log.out("Operator's proportional frequency should be in a range between 0 times and 32 times.");
		else {
			frequencyLevel = frequency;
			frequencyFixed = false;
		}

		return this;
	};

}
