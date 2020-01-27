package syn;

import java.awt.event.KeyEvent;
import java.util.LinkedList;

public class Note {
	public final int keyCode;
	public final double frequency;
	public double timeSinceHit = 0;
	public double timeSinceReleased = 0;
	public boolean released = false;

	private final static double[] keysAndFrequencies = createKeysAndFrequencies();

	public Note(int keyCode) {
		this.keyCode = keyCode;

		if (keyCode >= 0 || keyCode < keysAndFrequencies.length)
			frequency = keysAndFrequencies[keyCode];
		else
			frequency = 0;
	}

	private static double[] createKeysAndFrequencies() {
		double noteStep = Math.pow(2, 1.0 / 12.0);
		double freq;

		double[] result = new double[255];

		freq = 220;
		result[KeyEvent.VK_A] = freq / noteStep;
		for (int keyCode : new int[] { KeyEvent.VK_Z, KeyEvent.VK_S, KeyEvent.VK_X, KeyEvent.VK_C, KeyEvent.VK_F,
				KeyEvent.VK_V, KeyEvent.VK_G, KeyEvent.VK_B, KeyEvent.VK_N, KeyEvent.VK_J, KeyEvent.VK_M, KeyEvent.VK_K,
				KeyEvent.VK_COMMA, KeyEvent.VK_L, KeyEvent.VK_PERIOD, KeyEvent.VK_SLASH, KeyEvent.VK_QUOTE }) {
			result[keyCode] = freq;
			freq *= noteStep;
		}

		freq = 440;
		result[KeyEvent.VK_1] = freq / noteStep;
		for (int keyCode : new int[] { KeyEvent.VK_Q, KeyEvent.VK_2, KeyEvent.VK_W, KeyEvent.VK_E, KeyEvent.VK_4,
				KeyEvent.VK_R, KeyEvent.VK_5, KeyEvent.VK_T, KeyEvent.VK_Y, KeyEvent.VK_7, KeyEvent.VK_U, KeyEvent.VK_8,
				KeyEvent.VK_I, KeyEvent.VK_9, KeyEvent.VK_O, KeyEvent.VK_P, KeyEvent.VK_MINUS, KeyEvent.VK_OPEN_BRACKET,
				KeyEvent.VK_EQUALS, KeyEvent.VK_CLOSE_BRACKET }) {
			result[keyCode] = freq;
			freq *= noteStep;
		}

		return result;
	}

	public static Iterable<Integer> getKeyCodes() {
		LinkedList<Integer> result = new LinkedList<>();

		for (int i = 0; i < keysAndFrequencies.length; i++)
			if (keysAndFrequencies[i] > 0)
				result.add(i);

		return result;
	}
}
