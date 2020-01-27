package application;

import java.util.HashSet;

public class KeyInput {
	private HashSet<Integer> pressedKeys = new HashSet<>();

	public interface KeyInputHandler {
		void execute(int keyCode);
	}
	public KeyInputHandler onKeyDown;
	public KeyInputHandler onKeyUp;

	public void keyDown(int keyCode) {
		synchronized (pressedKeys) {
			if (!pressedKeys.add(keyCode))
				return;
		}

		if (onKeyDown != null)
			onKeyDown.execute(keyCode);
	}

	public void keyUp(int keyCode) {
		synchronized (pressedKeys) {
			if (!pressedKeys.remove(keyCode))
				return;
		}

		if (onKeyUp != null)
			onKeyUp.execute(keyCode);
	}
}
