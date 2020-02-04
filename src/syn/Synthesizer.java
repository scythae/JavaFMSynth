package syn;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import syn.operator.Algorithm;
import syn.operator.Operator;
import utils.Utils;

public class Synthesizer {
	private MainWaveGenerator waveGenerator;
	private SoundPlayer soundPlayer;
	private Patch patch = new Patch();

	public Synthesizer() {
		Operator.timeStep = SoundPlayer.sampleStepSeconds;

		waveGenerator = new MainWaveGenerator();
		soundPlayer = new SoundPlayer(waveGenerator);
	}

	public List<Double> getLastSamples() {
		return waveGenerator.getLastSamples();
	}

	public void close() {
		soundPlayer.stop();
	}

	public void pressNote(int keyCode) {
		getAlgorithm().addNote(keyCode);
	}

	public void releaseNote(int keyCode) {
		getAlgorithm().releaseNote(keyCode);
	}

	public Algorithm getAlgorithm() {
		return patch.algorithm;
	}

	public void setPatch(Patch newPatch) {
		if (newPatch == null) {
			Utils.complain("Patch for synthesizer shouldn't have null value.");
			return;
		}

		getAlgorithm().reset();
		patch = newPatch;
	}

	public Patch getPatch() {
		return patch;
	}

	private class MainWaveGenerator implements WaveGenerator {
		private final double defaultGain = 0.25;
		private static final int sampleStoreSize = 2000;
		private LinkedList<Double> samplesStore = new LinkedList<Double>();

		MainWaveGenerator() {
			for (int i = 0; i < sampleStoreSize; i++)
				samplesStore.add(0.0);
		}

		@Override
		public double getSampleValue() {
			if (patch.algorithm == null)
				return 0;

			double result = 0;
			int carriersCount = 0;

			for (Operator op: getAlgorithm().getOperators()) {
				result += op.getSampleValue();
				carriersCount++;
				op.removeFinishedNotes();
				op.doTimeStep();
			}

			result *= defaultGain * patch.gain * getSampleValueReducer(carriersCount);

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
}
