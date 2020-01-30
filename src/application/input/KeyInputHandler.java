package application.input;

public interface KeyInputHandler {
	public interface OnKeyInput {
		void execute(int keyCode);
	}

	void setKeysToListen(Iterable<Integer> keyCodes);
	void setKeyInputHandlers(OnKeyInput onKeyDown, OnKeyInput onKeyUp);
}
