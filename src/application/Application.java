package application;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import application.swingUI.JLabelExt;
import application.swingUI.JWaveViewer;
import application.swingUI.SwingKeyListener;
import application.swingUI.UIAlgorithm;
import application.swingUI.UIAlgorithmTreeView;
import application.swingUI.UIControls;
import application.swingUI.UIPatchSelector;
import resources.Resources;
import syn.Synthesizer;
import syn.operator.Note;
import syn.operator.Operator;

public class Application {
	private static volatile Application instance = null;

	private Container mainPane;
	private Synthesizer synthesizer;
	private Operator selectedOperator = new Operator();
	private PatchSelector patchSelector;

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
				patchSelector.save();
				synthesizer.close();
				instance = null;
			};
		});

		SwingKeyListener keyListener = new SwingKeyListener();
		frame.add(keyListener.getMainComponent());

		keyListener.setKeysToListen(Note.getKeyCodes());
		keyListener.setKeyInputHandlers(
			(keyCode) -> synthesizer.pressNote(keyCode),
			(keyCode) -> synthesizer.releaseNote(keyCode)
		);

		synthesizer = new Synthesizer();

		initUI();
	}

	public static boolean isRunning() {
		return instance != null;
	}

	private void initUI() {
		Rectangle bounds = new Rectangle();

		JPanel panelPatch = new JPanel();
		panelPatch.setLayout(new BorderLayout());
		bounds.setBounds(0, 0, 200, mainPane.getHeight());
		panelPatch.setBounds(bounds);
		mainPane.add(panelPatch);

		UIControls uiControls = new UIControls();
		bounds.x = panelPatch.getWidth() + 1;
		bounds.y = 0;
		bounds.width = (mainPane.getWidth() - bounds.x);
		uiControls.setBounds(bounds);
		mainPane.add(uiControls.getMainContainer());

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

		UIPatchSelector patchSelector = new UIPatchSelector();
		this.patchSelector = patchSelector;
		panelPatch.add(patchSelector.getMainContainer(), BorderLayout.PAGE_END);

		UIAlgorithm uiAlgorithm = new UIAlgorithmTreeView();
		panelPatch.add(uiAlgorithm.getMainComponent(), BorderLayout.CENTER);


		uiControls.onOperatorValuesChanged = (opValues) -> {
			if (selectedOperator != null)
				selectedOperator.setValues(opValues);
		};
		uiControls.onGainChanged = (gain) -> {
			synthesizer.getPatch().gain = gain;
		};

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

		patchSelector.onPatchSelected = (patch) -> {
			synthesizer.setPatch(patch);
			uiControls.setGain(patch.gain);
			uiAlgorithm.setAlgorithm(patch.algorithm);
		};
		patchSelector.load();

		keyboardPic.setScaledIcon(Resources.keyboardPicture);

	}
}
