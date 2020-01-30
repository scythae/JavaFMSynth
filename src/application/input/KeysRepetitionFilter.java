package application.input;

import java.util.HashSet;

import application.input.KeyInputHandler.OnKeyInput;

public class KeysRepetitionFilter {
	private HashSet<Integer> pressedKeys = new HashSet<>();

	public OnKeyInput onKeyDown;
	public OnKeyInput onKeyUp;

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
