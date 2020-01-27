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

import syn.Note;
import syn.Synthesizer;
import syn.input.KeyInput;
import syn.operator.Operator;
import syn.operator.OperatorValues;
import syn.operator.Oscillator;
import syn.operator.Oscillators;
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

		JPanel panelOperatorValues = new JPanel();
		bounds.setBounds(bounds.width, 0, mainPane.getWidth() - bounds.width, mainPane.getHeight() / 3);
		panelOperatorValues.setBounds(bounds);
		mainPane.add(panelOperatorValues);

		JPanel panelCommonValues = new JPanel();
		bounds.y += bounds.height;
		panelCommonValues.setBounds(bounds);
		mainPane.add(panelCommonValues);

		JPanel waveViewer = new WaveViewer(synthesizer);
		bounds.y += bounds.height;
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

		JSliderScientific sliderAttack = new JSliderScientific(
			Operator.minAttack, Operator.maxAttack, Operator.minAttack, Math.E
		);
		panelOperatorValues.add(createLabel("Attack, seconds"));
		panelOperatorValues.add(sliderAttack);
		panelOperatorValues.add(sliderAttack.getLabel());

		JSliderScientific sliderDecay = new JSliderScientific(
			Operator.minDecay, Operator.maxDecay, Operator.minDecay, Math.E
		);
		panelOperatorValues.add(createLabel("Decay, seconds"));
		panelOperatorValues.add(sliderDecay);
		panelOperatorValues.add(sliderDecay.getLabel());

		JSliderScientific sliderSustain = new JSliderScientific(
			Operator.minSustain, Operator.maxSustain, Operator.minSustain, 1
		);
		panelOperatorValues.add(createLabel("Sustain, relative to level"));
		panelOperatorValues.add(sliderSustain);
		panelOperatorValues.add(sliderSustain.getLabel());

		JSliderScientific sliderRelease = new JSliderScientific(
			Operator.minRelease, Operator.maxRelease, Operator.minRelease, Math.E
		);
		panelOperatorValues.add(createLabel("Release, seconds"));
		panelOperatorValues.add(sliderRelease);
		panelOperatorValues.add(sliderRelease.getLabel());

		JSliderScientific sliderCommonGainLevel = new JSliderScientific(
			0, 4, 1, Math.E
		);
		sliderCommonGainLevel.setMinMax(0, 1000);
		sliderCommonGainLevel.setValueSc(synthesizer.gain);
		sliderCommonGainLevel.setRounding(3);
		panelCommonValues.add(createLabel("Gain"));
		panelCommonValues.add(sliderCommonGainLevel);
		panelCommonValues.add(sliderCommonGainLevel.getLabel());

		int gridRowHeight = 25;
		setGridLayout(panelOperatorValues, 3, gridRowHeight);

		panelCommonValues.setLocation(panelCommonValues.getX(), panelOperatorValues.getY() + panelOperatorValues.getHeight());
		setGridLayout(panelCommonValues, 3, gridRowHeight);

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

		sliderAttack.addChangeListener((ChangeEvent e) ->
			selectedOperator.setAttack(sliderAttack.getValueSc())
		);
		sliderDecay.addChangeListener((ChangeEvent e) ->
			selectedOperator.setDecay(sliderDecay.getValueSc())
		);
		sliderSustain.addChangeListener((ChangeEvent e) ->
			selectedOperator.setSustain(sliderSustain.getValueSc())
		);
		sliderRelease.addChangeListener((ChangeEvent e) ->
			selectedOperator.setRelease(sliderRelease.getValueSc())
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

		sliderCommonGainLevel.addChangeListener((ChangeEvent e) ->
			synthesizer.gain = sliderCommonGainLevel.getValueSc()
		);


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

				sliderAttack.setValueSc(values.attack);
				sliderDecay.setValueSc(values.decay);
				sliderSustain.setValueSc(values.sustain);
				sliderRelease.setValueSc(values.release);
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

	private JLabel createLabel(String text) {
		JLabel result = new JLabel(text);
		result.setHorizontalAlignment(SwingConstants.RIGHT);
		padRight(result);
		return result;
	}

	private void padRight(JComponent component) {
		component.setBorder(new EmptyBorder(0, 0, 0, 20));
	}

	private void setGridLayout(Container container, int colCount, int rowHeight) {
		int rowCount = container.getComponentCount() / colCount + 1;
		container.setLayout(new GridLayout(0, colCount));
		container.setSize(container.getWidth(), rowHeight * rowCount);
	}
}
