package application;
import java.awt.Color;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

import application.swingUI.JLabelExt;
import application.swingUI.UIAlgorithm;
import application.swingUI.UIAlgorithmTreeView;
import application.swingUI.UIControls;
import resources.Resources;
import syn.Synthesizer;
import syn.operator.Algorithm;
import syn.operator.DefaultAlgorithms;
import syn.operator.Note;
import syn.operator.Operator;

public class Application {
	private static final String userDataPath = "./.synthUserData/Algorithm.synth";
	private static volatile Application instance = null;

	private Container mainPane;
	private Synthesizer synthesizer;
	private KeyInput keyInput;
	private JComponent keyListener;
	private Operator selectedOperator = new Operator();

	public Application() {
		if (isRunning())
			throw new RuntimeException("Application is already launched.");

		instance = this;
		javax.swing.SwingUtilities.invokeLater(() -> createAnShowGUI());
	}

	@SuppressWarnings("serial")
	public void createAnShowGUI() {
		JFrame frame = new JFrame("Synth");
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.setSize(800, 600);
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

		mainPane = frame.getContentPane();
		mainPane.setLayout(null);
		mainPane.setBackground(Color.darkGray);

		keyListener = new JComponent() {
		};
		frame.add(keyListener);

		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				synthesizer.algorithm.saveToFile(userDataPath);

				instance = null;
			};
		});

		synthesizer = new Synthesizer();

		keyInput = new KeyInput();
		keyInput.onKeyDown = (keyCode) -> synthesizer.algorithm.addNote(keyCode);
		keyInput.onKeyUp = (keyCode) -> synthesizer.algorithm.releaseNote(keyCode);

		initUI();
		initKeyBindings();
	}

	public static boolean isRunning() {
		return instance != null;
	}

	@SuppressWarnings("serial")
	private void initKeyBindings() {
		InputMap im = keyListener.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW);
		ActionMap am = keyListener.getActionMap();

		Iterable<Integer> activeKeyCodes = Note.getKeyCodes();

		for (boolean release : new boolean[] {false, true})
			for (int keyCode : activeKeyCodes) {
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
		Rectangle bounds = new Rectangle();

		JPanel panelOperators = new JPanel();
		panelOperators.setLayout(null);
		bounds.setBounds(0, 0, 200, mainPane.getHeight());
		panelOperators.setBounds(bounds);
		mainPane.add(panelOperators);

		UIControls uiControls = new UIControls();
		bounds.x = panelOperators.getWidth() + 1;
		bounds.y = 0;
		bounds.width = (mainPane.getWidth() - bounds.x);
		uiControls.setBounds(bounds);
		mainPane.add(uiControls.getMainContainer());

		uiControls.onOperatorValuesChanged = (opValues) -> {
			if (selectedOperator != null)
				selectedOperator.setValues(opValues);
		};
		uiControls.onGainChanged = (gain) -> {
			synthesizer.gain = gain;
		};
		uiControls.setGain(synthesizer.gain);

		JPanel waveViewer = new WaveViewer(synthesizer);
		bounds.height = mainPane.getHeight() / 3;
		bounds.y = (int) uiControls.getMainContainer().getBounds().getMaxY();
		waveViewer.setBounds(bounds);
		mainPane.add(waveViewer);

		JLabelExt keyboardPic = new JLabelExt();
		bounds.y = (int) waveViewer.getBounds().getMaxY();
		bounds.height = mainPane.getHeight() - bounds.y;
		keyboardPic.setBounds(bounds);
		mainPane.add(keyboardPic);

		keyboardPic.setScaledIcon(Resources.keyboardPicture);

		UIAlgorithm uiAlgorithm = new UIAlgorithmTreeView();
		JComponent algorithmComponent = uiAlgorithm.getMainComponent();
		algorithmComponent.setBounds(0, 0, panelOperators.getWidth(), panelOperators.getHeight());
		panelOperators.add(algorithmComponent);

		uiAlgorithm.onOperatorSelected = (operator) -> {
			selectedOperator = operator;
			if (operator == null) {
				uiControls.setOperatorVisible(false);
				return;
			}
			uiControls.setOperatorVisible(true);
			uiControls.setOperatorValues(operator.getOperatorValues());
		};
		uiAlgorithm.onAlgorithmOperatorChange = (algorithm, operator, connecting) -> {
			if (algorithm == null || operator == null)
				return;

			if (connecting)
				algorithm.addOperator(operator);
			else
				algorithm.removeOperator(operator);
		};

		synthesizer.algorithm = Algorithm.loadFromFile(userDataPath);
		if (synthesizer.algorithm == null)
			synthesizer.algorithm = DefaultAlgorithms.TubularBell;

		uiAlgorithm.setAlgorithm(synthesizer.algorithm);
	}
}
