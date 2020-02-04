package syn.operator;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;

import utils.Utils;


public class Operator extends Algorithm {
	public static double timeStep = 0;
	private static final ArrayList<Note> emptyNoteList = new ArrayList<>(0);

	private volatile ArrayList<Note> notes = emptyNoteList;

	private OperatorValues values = new OperatorValues();
	private boolean isModulator = false;

	public Operator() {
		notes = new ArrayList<>(0);
	}

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
		for (Operator modulator : getOperators())
			modulation += modulator.getSampleValue(note);

		double phase = angularFrequency * (note.timeSinceHit + modulation);
		double result = values.oscillator.getSampleValue(phase) * values.level * getADSREnvelope(note);
		if (isModulator)
			result = angularFrequency == 0 ? 0 : result / angularFrequency;

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

			double releaseMultiplier = 1 - note.timeSinceReleased / values.release;
			if (releaseMultiplier > 0)
				result *= releaseMultiplier;
			else
				result = 0;
		}

		return result;
	};

	public double getSampleValue() {
		double result = 0;
		for (Note note : notes)
			result += getSampleValue(note);

		return result;
	};

	@Override
	public Algorithm addOperator(Operator operator) {
		if (operator != null)
			operator.isModulator = true;

		return super.addOperator(operator);
	};

	@Override
	public void addNote(int keyCode) {
		Note note = new Note(keyCode);

		ArrayList<Note> tmpNotes;
		synchronized (notes) {
			tmpNotes = new ArrayList<>(notes.size() + 1);
			tmpNotes.add(note);
			tmpNotes.addAll(notes);
		}

		notes = tmpNotes;
	};

	@Override
	public void releaseNote(int keyCode) {
		for (Note note : notes)
			if (note.keyCode == keyCode) {
				note.released = true;
				break;
			}
	};

	public void doTimeStep() {
		for (Note note : notes) {
			note.timeSinceHit += timeStep;

			if (note.released)
				note.timeSinceReleased += timeStep;
		}
	};

	public void removeFinishedNotes() {
		for (Note note : notes)
			if (note.released && note.timeSinceReleased > values.release)
				removeNote(note);
	};

	private void removeNote(Note note) {
		ArrayList<Note> tmpNotes;

		synchronized (notes) {
			tmpNotes = new ArrayList<>(notes.size());
			for (Note tmpNote : notes)
				if (tmpNote != note)
					tmpNotes.add(tmpNote);
		}

		notes = tmpNotes;
	};

	void removeAllNotes() {
		notes = emptyNoteList;
	}

	public void setValues(OperatorValues sourceValues) {
		setOscillator(sourceValues.oscillator);
		setLevel(sourceValues.level);
		setDetune(sourceValues.detune);
		if (sourceValues.frequencyFixed)
			setFrequencyFixed(sourceValues.frequencyLevel);
		else
			setFrequencyProportional(sourceValues.frequencyLevel);

		setAttack(sourceValues.attack);
		setDecay(sourceValues.decay);
		setSustain(sourceValues.sustain);
		setRelease(sourceValues.release);
	};


	public Operator setOscillator(Oscillator oscillator) {
		if (oscillator == null)
			Utils.complain("Operator requires non-null oscillator.");
		else
			values.oscillator = oscillator;

		return this;
	};

	public Operator setLevel(double value) {
		if (checkRange(value, OperatorConsts.minLevel, OperatorConsts.maxLevel, "level", ""))
			values.level = value;

		return this;
	};

	public Operator setDetune(double value) {
		if (checkRange(value, -OperatorConsts.maxDetune, OperatorConsts.maxDetune, "detune", "Hz"))
			values.detune = value;

		return this;
	};

	public Operator setFrequencyFixed(double value) {
		if (checkRange(value, OperatorConsts.minFixedFrequency, OperatorConsts.maxFixedFrequency, "fixed frequency", "Hz")) {
			values.frequencyLevel = value;
			values.frequencyFixed = true;
		}

		return this;
	};

	public Operator setFrequencyProportional(double value) {
		if (checkRange(value, OperatorConsts.minProportionalFrequency, OperatorConsts.maxProportionalFrequency, "proportional frequency", "times")) {
			values.frequencyLevel = value;
			values.frequencyFixed = false;
		}

		return this;
	};

	public Operator setAttack(double value) {
		if (checkRange(value, OperatorConsts.minAttack, OperatorConsts.maxAttack, "attack", "seconds"))
			values.attack = value;

		return this;
	};

	public Operator setDecay(double value) {
		if (checkRange(value, OperatorConsts.minDecay, OperatorConsts.maxDecay, "decay", "seconds"))
			values.decay = value;

		return this;
	};

	public Operator setSustain(double value) {
		if (checkRange(value, OperatorConsts.minSustain, OperatorConsts.maxSustain, "sustain", ""))
			values.sustain = value;

		return this;
	};

	public Operator setRelease(double value) {
		if (checkRange(value, OperatorConsts.minRelease, OperatorConsts.maxRelease, "release", "seconds"))
			values.release = value;

		return this;
	};

	private boolean checkRange(double paramValue, double min, double max, String paramName, String measureUnit) {
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

	private static final long serialVersionUID = -2968290946211356242L;
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		isModulator = in.readBoolean();
		values.level = in.readDouble();
		values.detune = in.readDouble();
		values.frequencyLevel = in.readDouble();
		values.frequencyFixed = in.readBoolean();
		values.attack = in.readDouble();
		values.decay = in.readDouble();
		values.sustain = in.readDouble();
		values.release = in.readDouble();
		String oscillatorName = (String) in.readObject();
		values.oscillator = Oscillators.getByName(oscillatorName);

		super.readExternal(in);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeBoolean(isModulator);
		out.writeDouble(values.level);
		out.writeDouble(values.detune);
		out.writeDouble(values.frequencyLevel);
		out.writeBoolean(values.frequencyFixed);
		out.writeDouble(values.attack);
		out.writeDouble(values.decay);
		out.writeDouble(values.sustain);
		out.writeDouble(values.release);
		out.writeObject(values.oscillator.toString());

		super.writeExternal(out);
	};
}
