package application.swingUI;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import application.input.KeyInputHandler;
import application.input.KeysRepetitionFilter;
import utils.Utils;

public class SwingKeyListener implements KeyInputHandler {
	private KeysRepetitionFilter filter;
	private JComponent mainComponent;

	public SwingKeyListener() {
		filter = new KeysRepetitionFilter();
		setKeyInputHandlers((keyCode) -> {}, (keyCode) -> {});

		mainComponent = new JComponent() {
			private static final long serialVersionUID = 8501678333809167184L;
		};
	}

	@Override
	public void setKeysToListen(Iterable<Integer> keyCodes) {
		InputMap im = mainComponent.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW);
		ActionMap am = mainComponent.getActionMap();

		for (boolean release : new boolean[] {false, true})
			for (int keyCode : keyCodes) {
				Object actionKey = new Object();
				im.put(KeyStroke.getKeyStroke(keyCode, 0, release), actionKey);
				am.put(actionKey, createKeyAction(keyCode, release));
			}
	}

	@SuppressWarnings("serial")
	private AbstractAction createKeyAction(int keyCode, boolean keyRelease) {
		Utils.Callback filterAction = keyRelease ? () -> filter.keyUp(keyCode) : () -> filter.keyDown(keyCode);

		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				filterAction.execute();
			}
		};
	}

	@Override
	public void setKeyInputHandlers(OnKeyInput onKeyDown, OnKeyInput onKeyUp) {
		if (onKeyDown != null)
			filter.onKeyDown = onKeyDown;
		if (onKeyUp != null)
			filter.onKeyUp = onKeyUp;
	}

	public Component getMainComponent() {
		return mainComponent;
	}
}
