package application;
import java.awt.Color;
import java.awt.Container;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Enumeration;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import application.swingUI.JTreeExt;
import application.swingUI.SwingUI;
import resources.Resources;
import syn.Synthesizer;
import syn.operator.Note;
import syn.operator.Operator;
import syn.operator.OperatorValues;
import utils.LocalFactory;
import utils.Utils;

public class Application {
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
				instance = null;
			};
		});

		synthesizer = new Synthesizer();

		keyInput = new KeyInput();
		keyInput.onKeyDown = (keyCode) -> synthesizer.keyDown(keyCode);
		keyInput.onKeyUp = (keyCode) -> synthesizer.keyUp(keyCode);

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
		int buttonHeight = 25;
		Rectangle bounds = new Rectangle();

		JPanel panelOperators = new JPanel();
		panelOperators.setLayout(null);
		bounds.setBounds(0, 0, 200, mainPane.getHeight());
		panelOperators.setBounds(bounds);
		mainPane.add(panelOperators);

		SwingUI swingUI = new SwingUI();
		bounds.x = panelOperators.getWidth() + 1;
		bounds.y = 0;
		bounds.width = (mainPane.getWidth() - bounds.x);
		swingUI.setBounds(bounds);
		mainPane.add(swingUI.getMainContainer());

		swingUI.onOperatorValuesChanged = (opValues) -> {
			selectedOperator.setOscillator(opValues.oscillator);
			selectedOperator.setLevel(opValues.level);
			selectedOperator.setDetune(opValues.detune);
			if (opValues.frequencyFixed)
				selectedOperator.setFrequencyFixed(opValues.frequencyLevel);
			else
				selectedOperator.setFrequencyProportional(opValues.frequencyLevel);

			selectedOperator.setAttack(opValues.attack);
			selectedOperator.setDecay(opValues.decay);
			selectedOperator.setSustain(opValues.sustain);
			selectedOperator.setRelease(opValues.release);
		};
		swingUI.onGainChanged = (gain) -> {
			synthesizer.gain = gain;
		};
		swingUI.setGain(synthesizer.gain);

		JPanel waveViewer = new WaveViewer(synthesizer);
		bounds.height = mainPane.getHeight() / 3;
		bounds.y = (int) swingUI.getMainContainer().getBounds().getMaxY();
		waveViewer.setBounds(bounds);
		mainPane.add(waveViewer);

		if (Resources.keyboardPicture != null) {
			bounds.y = (int) waveViewer.getBounds().getMaxY();
			bounds.height = mainPane.getHeight() - bounds.y;

			ImageIcon icon = new ImageIcon(new ImageIcon(Resources.keyboardPicture).getImage().getScaledInstance(bounds.width, bounds.height, Image.SCALE_DEFAULT));
			JLabel keyboardPic = new JLabel(icon);
			keyboardPic.setBounds(bounds);
			mainPane.add(keyboardPic);
		} else {
			Utils.complain("Cannot load keyboard pic.");
		}

		JTreeExt treeOperators = new JTreeExt("Synth");
		treeOperators.setFocusable(false);
		bounds.setBounds(0, 0, panelOperators.getWidth(), panelOperators.getHeight() - buttonHeight);
		treeOperators.setBounds(bounds);
		panelOperators.add(treeOperators);

		JButton btnAddOperator = new JButton("Add");
		bounds.setBounds(0, panelOperators.getHeight() - buttonHeight, panelOperators.getWidth() / 2, buttonHeight);
		btnAddOperator.setBounds(bounds);
		panelOperators.add(btnAddOperator);

		JButton btnRemoveOperator = new JButton("Remove");
		bounds.x += bounds.width;
		btnRemoveOperator.setBounds(bounds);
		panelOperators.add(btnRemoveOperator);

		btnAddOperator.addActionListener((actionEvent) ->
			treeOperators.addToCurrentNode(new Operator())
		);
		btnRemoveOperator.addActionListener((actionEvent) ->
			treeOperators.removeCurrentNode()
		);

		treeOperators.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				Object nodeObject = treeOperators.getSelectedNodeObject();
				if (!(nodeObject instanceof Operator)) {
					swingUI.setOperatorVisible(false);
					return;
				}
				swingUI.setOperatorVisible(true);

				Operator op = (Operator) nodeObject;
				selectedOperator = op;
				OperatorValues values = op.getOperatorValues();

				swingUI.setOperatorValues(values);
			}
		});

		treeOperators.onTreeChanged = new Utils.Callback() {
			@Override
			public void execute() {
				reloadOperatorsToSynth();
			}

			@SuppressWarnings("rawtypes")
			private void reloadOperatorsToSynth() {
				synthesizer.cleanCarriers();

				DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeOperators.getModel().getRoot();
				if (root == null)
					return;

				Enumeration nodes = root.breadthFirstEnumeration();
				while (nodes.hasMoreElements())
					loadOperatorFromNodeToSynth((DefaultMutableTreeNode) nodes.nextElement());
			}

			private void loadOperatorFromNodeToSynth(DefaultMutableTreeNode node) {
				DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
				if (parent == null)
					return;

				Operator op = (Operator) node.getUserObject();
				op.cleanModulators();

				if (parent.isRoot())
					synthesizer.addCarrier(op);
				else {
					Operator parentOp = (Operator) parent.getUserObject();
					parentOp.addModulator(op);
				}
			}
		};

		LocalFactory<Operator> bellCarrier = () -> {
			return new Operator().setAttack(0.015).setDecay(2).setSustain(0).setRelease(2);
		};

		LocalFactory<Operator> bellModulator = () -> {
			return bellCarrier.create().setLevel(1).setFrequencyProportional(3.5);
		};

		TreePath rootPath = treeOperators.getSelectionPath();

		treeOperators.setSelectionPath(new TreePath(
			treeOperators.addToCurrentNode(
				bellCarrier.create().setDetune(-5)
			).getPath()
		));

		treeOperators.addToCurrentNode(
			bellModulator.create().setDetune(-5)
		);

		treeOperators.setSelectionPath(rootPath);

		treeOperators.setSelectionPath(new TreePath(
			treeOperators.addToCurrentNode(
				bellCarrier.create().setDetune(5)
			).getPath()
		));

		treeOperators.addToCurrentNode(
			bellModulator.create().setDetune(5)
		);
	}
}
