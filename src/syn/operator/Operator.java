package syn.operator;

import java.util.ArrayList;
import java.util.List;

import utils.Utils;

public class Operator {
	public static final double minLevel = 0;
	public static final double maxLevel = 1;
	public static final int maxDetune = 50;
	public static final int minFixedFrequency = 0;
	public static final int maxFixedFrequency = 20000;
	public static final int minProportionalFrequency = 0;
	public static final int maxProportionalFrequency = 32;
	public static final int minAttack = 0;
	public static final int maxAttack = 2;
	public static final int minDecay = 0;
	public static final int maxDecay = 2;
	public static final int minSustain = 0;
	public static final int maxSustain = 1;
	public static final int minRelease = 0;
	public static final int maxRelease = 5;

	private OperatorValues values = new OperatorValues();
	private boolean isModulator = false;
	private volatile List<Note> notes = new ArrayList<>(0);

	public static double timeStep = 0;

	private double getSampleValue(Note note) {
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
		double result = values.oscillator.getSampleValue(phase) * values.level * getADSREnvelope(note);
		if (isModulator)
			result = result / angularFrequency;

		return result;
	};

	private double getADSREnvelope(Note note) {
		double result;

		if (note.timeSinceHit < values.attack) {
			result = note.timeSinceHit / values.attack;
		} else if (note.timeSinceHit < values.attack + values.decay) {
			double decayProgress = (note.timeSinceHit - values.attack) / values.decay;
			result = 1 - decayProgress * (1 - values.sustain);
		} else
			result = values.sustain;

		if (note.released) {
			if (Utils.doubleEquals(values.release, 0))
				return 0;

			result *= 1 - note.timeSinceReleased / values.release;
		}

		return result;
	};

	public double getSampleValue() {
		List<Note> tmpNotes = notes;
		double result = 0;

		for (Note note : tmpNotes)
			result += getSampleValue(note);

		return result;
	};

	public void addNote(int keyCode) {
		Note note = new Note(keyCode);

		List<Note> tmpNotes;
		synchronized (notes) {
			tmpNotes = new ArrayList<>(notes.size() + 1);
			tmpNotes.add(note);
			tmpNotes.addAll(notes);
		}

		notes = tmpNotes;
	};

	public void releaseNote(int keyCode) {
		List<Note> tmpNotes = notes;
		for (Note note : tmpNotes)
			if (note.keyCode == keyCode) {
				note.released = true;
				break;
			}
	};

	public void doTimeStep() {
		List<Note> tmpNotes = notes;
		for (Note note : tmpNotes) {
			note.timeSinceHit += timeStep;

			if (note.released)
				note.timeSinceReleased += timeStep;
		}
	};

	public void removeFinishedNotes() {
		List<Note> tmpNotes = notes;
		for (Note note : tmpNotes)
			if (note.released && note.timeSinceReleased > values.release)
				removeNote(note);
	};

	private void removeNote(Note note) {
		List<Note> tmpNotes;

		synchronized (notes) {
			tmpNotes = new ArrayList<>(notes.size());
			for (Note tmpNote : notes)
				if (tmpNote != note)
					tmpNotes.add(tmpNote);
		}

		notes = tmpNotes;
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

	public Operator setLevel(double value) {
		if (checkRange(value, minLevel, maxLevel, "level", ""))
			values.level = value;

		return this;
	};

	public Operator setDetune(double value) {
		if (checkRange(value, -maxDetune, maxDetune, "detune", "Hz"))
			values.detune = value;

		return this;
	};

	public Operator setFrequencyFixed(double value) {
		if (checkRange(value, minFixedFrequency, maxFixedFrequency, "fixed frequency", "Hz")) {
			values.frequencyLevel = value;
			values.frequencyFixed = true;
		}

		return this;
	};

	public Operator setFrequencyProportional(double value) {
		if (checkRange(value, minProportionalFrequency, maxProportionalFrequency, "proportional frequency", "times")) {
			values.frequencyLevel = value;
			values.frequencyFixed = false;
		}

		return this;
	};

	public Operator setAttack(double value) {
		if (checkRange(value, minAttack, maxAttack, "attack", "seconds"))
			values.attack = value;

		return this;
	};

	public Operator setDecay(double value) {
		if (checkRange(value, minDecay, maxDecay, "decay", "seconds"))
			values.decay = value;

		return this;
	};

	public Operator setSustain(double value) {
		if (checkRange(value, minSustain, maxSustain, "sustain", ""))
			values.sustain = value;

		return this;
	};

	public Operator setRelease(double value) {
		if (checkRange(value, minRelease, maxRelease, "release", "seconds"))
			values.release = value;

		return this;
	};

	public boolean checkRange(double paramValue, double min, double max, String paramName, String measureUnit) {
		if (paramValue < min || paramValue > max) {
			String complainMessage = String.format(
				"Operator's %s should be in a range between %s %s and %s %s. Value provided: %s.",
				paramName, min, measureUnit, max, measureUnit, paramValue
			);
			Utils.complain(complainMessage);
			return false;
		}

		return true;
	};

	public OperatorValues getOperatorValues() {
		OperatorValues result = new OperatorValues();

		result.detune = values.detune;
		result.frequencyFixed = values.frequencyFixed;
		result.frequencyLevel = values.frequencyLevel;
		result.level = values.level;
		result.oscillator = values.oscillator;
		result.modulators = new ArrayList<>(values.modulators);

		result.attack = values.attack;
		result.decay = values.decay;
		result.sustain = values.sustain;
		result.release = values.release;

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
