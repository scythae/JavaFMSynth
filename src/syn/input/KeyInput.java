package syn.input;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import syn.Note;
import syn.NotesProvider;
import utils.Utils;

public class KeyInput implements NotesProvider {
	private HashSet<Integer> keyStrokesLightWeight = new HashSet<>();
	private Map<Integer, KeyStroke> keyStrokes = new HashMap<>();
	private double[] keysAndFrequencies = new double[255];

	private TimeProvider timeProvider = () -> {return 0.0;};

	public KeyInput() {
		runKeyStrokesCleaner();
		fillKeysAndFrequencies();
	}

	public void setTimeProvider(TimeProvider timeProvider) {
		if (timeProvider != null)
			this.timeProvider = timeProvider;
	};

	private class KeyStroke {
		public double whenDown = 0.0;
		public double whenUp = 0.0;
		public double frequency;
		public boolean finished = false;
	}

	public void keyDown(int keyCode) {
		synchronized (keyStrokesLightWeight) {
			if (!keyStrokesLightWeight.add(keyCode))
				return;
		}

		if (getKeyFrequency(keyCode) == 0)
			return;

		KeyStroke keyStroke = new KeyStroke();
		keyStroke.whenDown = timeProvider.getTime();
		keyStroke.frequency = getKeyFrequency(keyCode);

		synchronized (keyStrokes) {
			keyStrokes.put(keyCode, keyStroke);
		}
	}

	public void keyUp(int keyCode) {
		synchronized (keyStrokesLightWeight) {
			if (!keyStrokesLightWeight.remove(keyCode))
				return;
		}

		if (getKeyFrequency(keyCode) == 0)
			return;

		synchronized (keyStrokes) {
			KeyStroke keyStroke = keyStrokes.get(keyCode);
			if (keyStroke != null) {
				keyStroke.finished = true;
				keyStroke.whenUp = timeProvider.getTime();
			}
		}
	}

	private double getKeyFrequency(int keyCode) {
		if (keyCode >= 0 && keyCode <= 255)
			return keysAndFrequencies[keyCode];
		else
			return 0;
	}

	@Override
	public List<Note> getNotes() {
		double globalTime = timeProvider.getTime();
		ArrayList<Note> result;

		synchronized (keyStrokes) {
			result = new ArrayList<>(keyStrokes.size());

			for (KeyStroke keyStroke : keyStrokes.values()) {
				Note note = new Note();
				note.frequency = keyStroke.frequency;
				note.timeSinceHit = globalTime - keyStroke.whenDown;
				note.timeSinceReleased = globalTime - keyStroke.whenUp;
				result.add(note);
			}
		}

		return result;
	}

	private void runKeyStrokesCleaner() {
		Utils.startTimer("KeyStrokesCleaner", 20, () -> {
			synchronized (keyStrokes) {
				List<Integer> finishedKeyCodes = new LinkedList<>();

				for (int keyCode : keyStrokes.keySet())
					if (keyStrokes.get(keyCode).finished)
						finishedKeyCodes.add(keyCode);

				for (int keyCode : finishedKeyCodes)
					keyStrokes.remove(keyCode);
			}
		});
	}

	private void fillKeysAndFrequencies() {
		double noteStep = Math.pow(2, 1.0 / 12.0);
		double freq;

		freq = 220;
		keysAndFrequencies[KeyEvent.VK_A] = freq / noteStep;
		for (int keyCode : new int[] { KeyEvent.VK_Z, KeyEvent.VK_S, KeyEvent.VK_X, KeyEvent.VK_C, KeyEvent.VK_F,
				KeyEvent.VK_V, KeyEvent.VK_G, KeyEvent.VK_B, KeyEvent.VK_N, KeyEvent.VK_J, KeyEvent.VK_M, KeyEvent.VK_K,
				KeyEvent.VK_COMMA, KeyEvent.VK_L, KeyEvent.VK_PERIOD, KeyEvent.VK_SLASH, KeyEvent.VK_QUOTE }) {
			keysAndFrequencies[keyCode] = freq;
			freq *= noteStep;
		}

		freq = 440;
		keysAndFrequencies[KeyEvent.VK_1] = freq / noteStep;
		for (int keyCode : new int[] { KeyEvent.VK_Q, KeyEvent.VK_2, KeyEvent.VK_W, KeyEvent.VK_E, KeyEvent.VK_4,
				KeyEvent.VK_R, KeyEvent.VK_5, KeyEvent.VK_T, KeyEvent.VK_Y, KeyEvent.VK_7, KeyEvent.VK_U, KeyEvent.VK_8,
				KeyEvent.VK_I, KeyEvent.VK_9, KeyEvent.VK_O, KeyEvent.VK_P, KeyEvent.VK_MINUS, KeyEvent.VK_OPEN_BRACKET,
				KeyEvent.VK_EQUALS, KeyEvent.VK_CLOSE_BRACKET }) {
			keysAndFrequencies[keyCode] = freq;
			freq *= noteStep;
		}
	}
}
