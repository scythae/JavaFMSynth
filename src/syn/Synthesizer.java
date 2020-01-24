package syn;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import syn.input.TimeProvider;
import syn.operator.Operator;

public class Synthesizer implements TimeProvider{
	private MainWaveGenerator waveGenerator;
	private NotesProvider notesProvider;
	private double timeSeconds = 0.0;
	private final List<Operator> emptyOperatorList = new ArrayList<>(0);

	public volatile double gain = 1.0;
	private final double defaultGain = 0.25;

	public Synthesizer(NotesProvider notesProvider) {
		this.notesProvider = notesProvider;
		if (notesProvider == null)
			notesProvider = () -> {return new LinkedList<>();};

		waveGenerator = new MainWaveGenerator();
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

	@Override
	public double getTime() {
		return timeSeconds;
	}

	public List<Double> getLastSamples() {
		return waveGenerator.getLastSamples();
	}

	private class MainWaveGenerator implements WaveGenerator {
		private static final int sampleStoreSize = 2000;
		private LinkedList<Double> samplesStore = new LinkedList<Double>();

		private volatile List<Operator> carriers = emptyOperatorList;

		MainWaveGenerator() {
			for (int i = 0; i < sampleStoreSize; i++)
				samplesStore.add(0.0);
		}

		@Override
		public double getSampleValue() {
			double result = 0;

			List<Note> tmpNotes = notesProvider.getNotes();
			for (Note note : tmpNotes)
				result += getSampleValue(note);

			result *= defaultGain * gain * getSampleValueReducer(tmpNotes.size());

			synchronized (samplesStore) {
				samplesStore.removeLast();
				samplesStore.addFirst(result);
			}

			timeSeconds += SoundPlayer.sampleStepSeconds;

			return result;
		}

		private double getSampleValue(Note note) {
			double result = 0;

			List<Operator> tmpCarriers = carriers;
			for (Operator op: tmpCarriers)
				result += op.getSampleValue(note);

			result *= getSampleValueReducer(tmpCarriers.size());

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
