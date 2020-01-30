package application;
import java.awt.Color;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import application.swingUI.JLabelExt;
import application.swingUI.JWaveViewer;
import application.swingUI.SwingKeyListener;
import application.swingUI.UIAlgorithm;
import application.swingUI.UIAlgorithmTreeView;
import application.swingUI.UIControls;
import resources.Resources;
import syn.Synthesizer;
import syn.operator.Note;
import syn.operator.Operator;

public class Application {
	private static volatile Application instance = null;

	private Container mainPane;
	private Synthesizer synthesizer;
	private Operator selectedOperator = new Operator();

	public Application() {
		if (isRunning())
			throw new RuntimeException("Application is already launched.");

		instance = this;
		javax.swing.SwingUtilities.invokeLater(() -> createAnShowGUI());
	}

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

		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				synthesizer.close();
				instance = null;
			};
		});

		SwingKeyListener keyListener = new SwingKeyListener();
		frame.add(keyListener.getMainComponent());

		keyListener.setKeysToListen(Note.getKeyCodes());
		keyListener.setKeyInputHandlers(
			(keyCode) -> synthesizer.algorithm.addNote(keyCode),
			(keyCode) -> synthesizer.algorithm.releaseNote(keyCode)
		);

		synthesizer = new Synthesizer();

		initUI();
	}

	public static boolean isRunning() {
		return instance != null;
	}

	private void initUI() {
		Rectangle bounds = new Rectangle();

		JPanel panelAlgorithm = new JPanel();
		panelAlgorithm.setLayout(null);
		bounds.setBounds(0, 0, 200, mainPane.getHeight());
		panelAlgorithm.setBounds(bounds);
		mainPane.add(panelAlgorithm);

		UIControls uiControls = new UIControls();
		bounds.x = panelAlgorithm.getWidth() + 1;
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

		JPanel waveViewer = new JWaveViewer(synthesizer);
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
		algorithmComponent.setBounds(0, 0, panelAlgorithm.getWidth(), panelAlgorithm.getHeight());
		panelAlgorithm.add(algorithmComponent);

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

		uiAlgorithm.setAlgorithm(synthesizer.algorithm);
	}
}
