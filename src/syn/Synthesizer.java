package syn;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import syn.operator.Algorithm;
import syn.operator.AlgorithmBank;
import syn.operator.Operator;
import utils.Paths;

public class Synthesizer {
	private MainWaveGenerator waveGenerator;
	private SoundPlayer soundPlayer;

	private final double defaultGain = 0.25;
	public volatile double gain = 1.0;
	public volatile Algorithm algorithm;

	public Synthesizer() {
		Operator.timeStep = SoundPlayer.sampleStepSeconds;

		waveGenerator = new MainWaveGenerator();
		soundPlayer = new SoundPlayer(waveGenerator);

		load();
	}

	public List<Double> getLastSamples() {
		return waveGenerator.getLastSamples();
	}

	public void close() {
		save();
		soundPlayer.stop();
	}

	private class MainWaveGenerator implements WaveGenerator {
		private static final int sampleStoreSize = 2000;
		private LinkedList<Double> samplesStore = new LinkedList<Double>();

		MainWaveGenerator() {
			for (int i = 0; i < sampleStoreSize; i++)
				samplesStore.add(0.0);
		}

		@Override
		public double getSampleValue() {
			if (algorithm == null)
				return 0;

			double result = 0;
			int carriersCount = 0;

			for (Operator op: algorithm.getOperators()) {
				result += op.getSampleValue();
				carriersCount++;
				op.removeFinishedNotes();
				op.doTimeStep();
			}

			result *= defaultGain * gain * getSampleValueReducer(carriersCount);

			synchronized (samplesStore) {
				samplesStore.removeLast();
				samplesStore.addFirst(result);
			}

			return result;
		}

		private double getSampleValueReducer(int sourceCount) {
			return sourceCount == 0 ? 1 : Math.sqrt(1.0 / sourceCount);
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

	private void save() {
		algorithm.saveToFile(Paths.lastAlgorithm);
	}

	private void load() {
		algorithm = Algorithm.loadFromFile(Paths.lastAlgorithm);
		if (algorithm == null)
			algorithm = AlgorithmBank.TubularBell;
	}
}
