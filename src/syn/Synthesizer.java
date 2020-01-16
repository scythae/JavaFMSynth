package syn;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import syn.input.TimeProvider;
import syn.operator.Operator;
import syn.operator.Oscillators;

public class Synthesizer implements TimeProvider{
	private MainWaveGenerator waveGenerator;
	private NotesProvider notesProvider;

	private double timeSeconds = 0.0;

	private ArrayList<Operator> rootOperators = new ArrayList<>();

	public Synthesizer(NotesProvider notesProvider) {

		rootOperators.add(
			new Operator().setModulator(
				new Operator().setLevel(.5).setOscillator(Oscillators.Flat)
			)
		);

		/*
		rootOperators.add(
			new Operator().setFrequencyFixed(1).setModulator(
				new Operator().setFrequencyFixed(1).setLevel(1)
			)
		);*/

		this.notesProvider = notesProvider;
		if (notesProvider == null)
			notesProvider = () -> {return new LinkedList<>();};

		waveGenerator = new MainWaveGenerator();
		new SoundPlayer(waveGenerator);
	}

	@Override
	public double getTime() {
		return timeSeconds;
	}

	public List<Double> getLastSamples() {
		return waveGenerator.getLastSamples();
	}

	private class MainWaveGenerator implements WaveGenerator {
		private static final double DefaultVolumeNormalizingCoefficient = 0.9;
		private static final int sampleStoreSize = 2000;
		private LinkedList<Double> samplesStore = new LinkedList<Double>();

		MainWaveGenerator() {
			for (int i = 0; i < sampleStoreSize; i++)
				samplesStore.add(0.0);
		}

		@Override
		public double getSampleValue() {
			double result = 0;
			int strokesCount = 0;

			for (Note note : notesProvider.getNotes()) {
				result += getSampleValue(note);
				strokesCount++;
			}

			if (strokesCount > 0) {
				double normalizingCoef = Math.pow(1.0 / strokesCount, 1) * DefaultVolumeNormalizingCoefficient;
				result = result * normalizingCoef;
			}

			synchronized (samplesStore) {
				samplesStore.removeLast();
				samplesStore.addFirst(result);
			}

			timeSeconds += SoundPlayer.sampleStepSeconds;

			return result;
		}

		private double getSampleValue(Note note) {
			double result = 0;

			for (Operator op: rootOperators)
				result += op.getSampleValue(note);

			result /= rootOperators.size();

			return result;
		}

		public List<Double> getLastSamples() {
			List<Double> result = new ArrayList<>(samplesStore.size());

			synchronized (samplesStore) {
				for (double sample : samplesStore)
					result.add(sample);
			}

			return result;
		}
	}
}
