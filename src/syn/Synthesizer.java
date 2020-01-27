package syn;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import syn.operator.Operator;

public class Synthesizer {
	private MainWaveGenerator waveGenerator;
	private final List<Operator> emptyOperatorList = new ArrayList<>(0);
	private final double defaultGain = 0.25;
	public volatile double gain = 1.0;

	public Synthesizer() {
		waveGenerator = new MainWaveGenerator();
		Operator.timeStep = SoundPlayer.sampleStepSeconds;
		new SoundPlayer(waveGenerator);
	}

	public synchronized void addCarrier(Operator carrier) {
		List<Operator> carriers = new ArrayList<>(waveGenerator.carriers.size() + 1);
		carriers.addAll(waveGenerator.carriers);
		carriers.add(carrier);

		waveGenerator.carriers = carriers;
	}

	public synchronized void cleanCarriers() {
		waveGenerator.carriers = emptyOperatorList;
	}

	public List<Double> getLastSamples() {
		return waveGenerator.getLastSamples();
	}

	public void keyDown(int keyCode) {
		waveGenerator.addNote(keyCode);
	}

	public void keyUp(int keyCode) {
		waveGenerator.releaseNote(keyCode);
	}

	private class MainWaveGenerator implements WaveGenerator {
		private static final int sampleStoreSize = 2000;
		private LinkedList<Double> samplesStore = new LinkedList<Double>();

		private volatile List<Operator> carriers = emptyOperatorList;

		MainWaveGenerator() {
			for (int i = 0; i < sampleStoreSize; i++)
				samplesStore.add(0.0);
		}

		public void addNote(int keyCode) {
			List<Operator> tmpCarriers = carriers;
			for (Operator op: tmpCarriers)
				op.addNote(keyCode);
		}

		public void releaseNote(int keyCode) {
			List<Operator> tmpCarriers = carriers;
			for (Operator op: tmpCarriers)
				op.releaseNote(keyCode);
		}

		@Override
		public double getSampleValue() {
			double result = 0;

			List<Operator> tmpCarriers = carriers;
			for (Operator op: tmpCarriers) {
				result += op.getSampleValue();

				op.removeFinishedNotes();
				op.doTimeStep();
			}

			result *= defaultGain * gain * getSampleValueReducer(tmpCarriers.size());

			synchronized (samplesStore) {
				samplesStore.removeLast();
				samplesStore.addFirst(result);
			}

			return result;
		}

		private double getSampleValueReducer(int sourceCount) {
			return sourceCount == 0 ? 1 : Math.pow(1.0 / sourceCount, 0.5);
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
