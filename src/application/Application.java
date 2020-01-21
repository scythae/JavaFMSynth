package application;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Enumeration;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import syn.Synthesizer;
import syn.input.KeyInput;
import syn.operator.Operator;
import syn.operator.OperatorValues;
import syn.operator.Oscillator;
import syn.operator.Oscillators;
import utils.Utils;

public class Application {
	private static volatile Application instance = null;

	private Container mainPane;
	private Synthesizer synthesizer;
	private KeyInput keyInput;
	private JComponent keyListener;
	private Operator selectedOperator;

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
		frame.setLocationRelativeTo(null);
		frame.setSize(640, 480);
		frame.setResizable(false);
		frame.setVisible(true);

		mainPane = frame.getContentPane();
		mainPane.setLayout(null);

		keyListener = new JComponent() {
		};
		frame.add(keyListener);

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
	}

	public static boolean isRunning() {
		return instance != null;
	}

	@SuppressWarnings("serial")
	private void initKeyBindings() {
		InputMap im = keyListener.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW);
		ActionMap am = keyListener.getActionMap();

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
		int buttonHeight = 25;
		Rectangle bounds = new Rectangle();

		JPanel panelOperators = new JPanel();
		panelOperators.setLayout(null);
		bounds.setBounds(0, 0, 200, mainPane.getHeight());
		panelOperators.setBounds(bounds);
		mainPane.add(panelOperators);

		JPanel panelOperatorValues = new JPanel();
		panelOperatorValues.setLayout(null);
		bounds.setBounds(bounds.width, 0, mainPane.getWidth() - bounds.width, mainPane.getHeight() / 2);
		panelOperatorValues.setBounds(bounds);
		mainPane.add(panelOperatorValues);

		JPanel waveViewer = new WaveViewer(synthesizer);
		bounds.y = bounds.height;
		waveViewer.setBounds(bounds);
		mainPane.add(waveViewer);

		JTreeExt treeOperators = new JTreeExt("Synth");
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

		JComboBox<Oscillator> opOscillator = new JComboBox<>(new Oscillator[] {
				Oscillators.Sine, Oscillators.Triangle, Oscillators.SawTooth,
				Oscillators.SawToothInverted, Oscillators.Flat, Oscillators.Noise
		});
		bounds.setBounds(0, 0, opOscillator.getPreferredSize().width, opOscillator.getPreferredSize().height);
		opOscillator.setBounds(bounds);
		panelOperatorValues.add(opOscillator);

		JSliderLogarithmic opLevel = new JSliderLogarithmic();
		bounds.y += bounds.height;
		opLevel.setBounds(bounds);
		panelOperatorValues.add(opLevel);



		btnAddOperator.addActionListener((actionEvent) -> treeOperators.addToCurrentNode(new Operator()));
		btnRemoveOperator.addActionListener((actionEvent) -> treeOperators.removeCurrentNode());

		opOscillator.addActionListener((ActionEvent e) -> {
				selectedOperator.setOscillator((Oscillator) opOscillator.getSelectedItem());
		});
		opLevel.addChangeListener((ChangeEvent e) -> {
				selectedOperator.setLevel(opLevel.getValueLog());
		});

		treeOperators.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				Object nodeObject = treeOperators.getCurrentNodeObject();
				if (!(nodeObject instanceof Operator)) {
					panelOperatorValues.setVisible(false);
					return;
				}
				panelOperatorValues.setVisible(true);

				Operator op = (Operator) nodeObject;
				selectedOperator = op;
				OperatorValues values = op.getOperatorValues();

				opOscillator.setSelectedItem(values.oscillator);
				opLevel.setValueLog(values.level);
			}
		});

		treeOperators.onTreeChanged = new Utils.Callback() {
			@Override
			public void execute() {
				reloadOperatorsToSynth();
			}

			private void reloadOperatorsToSynth() {
				synthesizer.cleanCarriers();

				DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeOperators.getModel().getRoot();
				if (root == null)
					return;

				@SuppressWarnings("rawtypes")
				Enumeration nodes = root.depthFirstEnumeration();
				while (nodes.hasMoreElements())
					loadOperatorFromNodeToSynth((DefaultMutableTreeNode) nodes.nextElement());
			}

			private void loadOperatorFromNodeToSynth(DefaultMutableTreeNode node) {
				DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
				if (parent == null)
					return;

				Operator op = (Operator) node.getUserObject();

				if (parent.isRoot())
					synthesizer.addCarrier(op);
				else {
					Operator parentOp = (Operator) parent.getUserObject();
					parentOp.addModulator(op);
				}
			}
		};

		treeOperators.setSelectionPath(new TreePath(
				treeOperators.addToCurrentNode(new Operator()).getPath()
		));
	}
}
