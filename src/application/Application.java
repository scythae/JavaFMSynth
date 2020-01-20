package application;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

import syn.Synthesizer;
import syn.input.KeyInput;

public class Application {
	private static volatile Application instance = null;

	private JPanel mainContainer;
	private Synthesizer synthesizer;
	private KeyInput keyInput;

	public Application() {
		if (isRunning())
			throw new RuntimeException("Application is already launched.");

		instance = this;

		JFrame frame = new JFrame("Synth");
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		frame.setSize(640, 480);
		frame.setLocationRelativeTo(null);
		mainContainer = new JPanel();
		frame.add(mainContainer, BorderLayout.CENTER);

		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				instance = null;
			};
		});

		keyInput = new KeyInput();
		synthesizer = new Synthesizer(keyInput);
		keyInput.setTimeProvider(synthesizer);

		initUI();
		initKeyBindings();

		frame.setVisible(true);
	}

	public static boolean isRunning() {
		return instance != null;
	}

	@SuppressWarnings("serial")
	private void initKeyBindings() {
		InputMap im = mainContainer.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW);
		ActionMap am = mainContainer.getActionMap();

		for (boolean release : new boolean[] {false, true})
			for (int keyCode : keyInput.getKeyCodes()) {
				Object key = new Object();
				im.put(KeyStroke.getKeyStroke(keyCode, 0, release), key);
				am.put(
						key,
						new AbstractAction() {
							@Override
							public void actionPerformed(ActionEvent arg0) {
								if (release)
									keyInput.keyUp(keyCode);
								else
									keyInput.keyDown(keyCode);
							}
						}
				);
			}
	}

	private void initUI() {
		JPanel waveViewer = new WaveViewer(synthesizer);
		mainContainer.setLayout(new BorderLayout());
		mainContainer.add(waveViewer);
	}
}
