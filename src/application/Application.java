package application;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Enumeration;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
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


		JComboBox<Oscillator> comboboxOscillator = new JComboBox<>(new Oscillator[] {
			Oscillators.Sine, Oscillators.Triangle, Oscillators.SawTooth,
			Oscillators.SawToothInverted, Oscillators.Flat, Oscillators.Noise
		});
		comboboxOscillator.setFocusable(false);
		panelOperatorValues.add(createLabel("Wave form"));
		panelOperatorValues.add(comboboxOscillator);
		panelOperatorValues.add(createLabel(""));

		JSliderScientific sliderLevel = new JSliderScientific(
			Operator.minLevel, Operator.maxLevel, 0.5, 1
		);
		panelOperatorValues.add(createLabel("Level"));
		panelOperatorValues.add(sliderLevel);
		panelOperatorValues.add(sliderLevel.getLabel());

		JSliderScientific sliderDetune = new JSliderScientific(
			-Operator.maxDetune, Operator.maxDetune, 0, 1
		);
		panelOperatorValues.add(createLabel("Detune, Hz"));
		panelOperatorValues.add(sliderDetune);
		panelOperatorValues.add(sliderDetune.getLabel());

		JSliderScientific sliderFrequencyProportionalLevel = new JSliderScientific(
			Operator.minProportionalFrequency, Operator.maxProportionalFrequency, 1, 1
		);
		sliderFrequencyProportionalLevel.setMinMax(Operator.minProportionalFrequency * 100, Operator.maxProportionalFrequency * 100);
		panelOperatorValues.add(createLabel("Frequency level"));
		panelOperatorValues.add(sliderFrequencyProportionalLevel);
		panelOperatorValues.add(sliderFrequencyProportionalLevel.getLabel());

		JCheckBox checkboxFixedFrequency = new JCheckBox("Fixed frequency");
		checkboxFixedFrequency.setHorizontalAlignment(SwingConstants.RIGHT);
		checkboxFixedFrequency.setHorizontalTextPosition(SwingConstants.LEFT);
		checkboxFixedFrequency.setFocusable(false);
		padRight(checkboxFixedFrequency);

		JSliderScientific sliderFrequencyFixedLevel = new JSliderScientific(
			Operator.minFixedFrequency, Operator.maxFixedFrequency, Operator.minFixedFrequency, 10
		);
		panelOperatorValues.add(checkboxFixedFrequency);
		panelOperatorValues.add(sliderFrequencyFixedLevel);
		panelOperatorValues.add(sliderFrequencyFixedLevel.getLabel());






		int gridRowHeight = 25;
		int rowCount = panelOperatorValues.getComponentCount() / 3 + 1;
		panelOperatorValues.setLayout(new GridLayout(0, 3));
		panelOperatorValues.setSize(panelOperatorValues.getWidth(), gridRowHeight * rowCount);

		btnAddOperator.addActionListener((actionEvent) ->
			treeOperators.addToCurrentNode(new Operator())
		);
		btnRemoveOperator.addActionListener((actionEvent) ->
			treeOperators.removeCurrentNode()
		);
		comboboxOscillator.addActionListener((ActionEvent e) ->
			selectedOperator.setOscillator((Oscillator) comboboxOscillator.getSelectedItem())
		);
		sliderLevel.addChangeListener((ChangeEvent e) ->
			selectedOperator.setLevel(sliderLevel.getValueSc())
		);
		sliderFrequencyProportionalLevel.addChangeListener((ChangeEvent e) ->
			selectedOperator.setFrequencyProportional(sliderFrequencyProportionalLevel.getValueSc())
		);
		sliderFrequencyFixedLevel.addChangeListener((ChangeEvent e) ->
			selectedOperator.setFrequencyFixed(sliderFrequencyFixedLevel.getValueSc())
		);
		sliderDetune.addChangeListener((ChangeEvent e) ->
			selectedOperator.setDetune(sliderDetune.getValueSc())
		);
		checkboxFixedFrequency.addChangeListener((ChangeEvent e) -> {
			boolean fixed = checkboxFixedFrequency.isSelected();

			sliderDetune.setVisible(!fixed);
			sliderDetune.getLabel().setVisible(!fixed);
			sliderFrequencyProportionalLevel.setVisible(!fixed);
			sliderFrequencyProportionalLevel.getLabel().setVisible(!fixed);
			sliderFrequencyFixedLevel.setVisible(fixed);
			sliderFrequencyFixedLevel.getLabel().setVisible(fixed);

			if (fixed)
				selectedOperator.setFrequencyFixed(sliderFrequencyFixedLevel.getValueSc());
			else
				selectedOperator.setFrequencyProportional(sliderFrequencyProportionalLevel.getValueSc());
		});
		checkboxFixedFrequency.setSelected(!checkboxFixedFrequency.isSelected());

		treeOperators.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				Object nodeObject = treeOperators.getSelectedNodeObject();
				if (!(nodeObject instanceof Operator)) {
					panelOperatorValues.setVisible(false);
					return;
				}
				panelOperatorValues.setVisible(true);

				Operator op = (Operator) nodeObject;
				selectedOperator = op;
				OperatorValues values = op.getOperatorValues();

				comboboxOscillator.setSelectedItem(values.oscillator);
				sliderLevel.setValueSc(values.level);
				checkboxFixedFrequency.setSelected(values.frequencyFixed);
				if (values.frequencyFixed)
					sliderFrequencyFixedLevel.setValueSc(values.frequencyLevel);
				else
					sliderFrequencyProportionalLevel.setValueSc(values.frequencyLevel);

				sliderDetune.setValueSc(values.detune);
				checkboxFixedFrequency.setSelected(values.frequencyFixed);
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

		treeOperators.setSelectionPath(new TreePath(
			treeOperators.addToCurrentNode(new Operator().setLevel(0.5)).getPath()
		));

		treeOperators.addToCurrentNode(new Operator().setFrequencyFixed(2));
	}

	private JLabel createLabel(String text) {
		JLabel result = new JLabel(text);
		result.setHorizontalAlignment(SwingConstants.RIGHT);
		padRight(result);
		return result;
	}

	private void padRight(JComponent component) {
		component.setBorder(new EmptyBorder(0, 0, 0, 20));
	}
}
