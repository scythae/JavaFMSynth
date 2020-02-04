package application.swingUI;

import java.awt.Container;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;

import syn.operator.OperatorConsts;
import syn.operator.OperatorValues;
import syn.operator.Oscillator;
import syn.operator.Oscillators;
import utils.LocalFactory;

public class UIControls {
	public interface OnValueChanged<T> {
		void execute(T value);
	}

	public OnValueChanged<OperatorValues> onOperatorValuesChanged;
	public OnValueChanged<Double> onGainChanged;

	private JComboBox<Oscillator> comboboxOscillator;
	private JCheckBox checkboxFixedFrequency;

	private JSliderScientific sliderLevel;
	private JSliderScientific sliderDetune;
	private JSliderScientific sliderFrequencyProportional;
	private JSliderScientific sliderFrequencyFixed;
	private JSliderScientific sliderAttack;
	private JSliderScientific sliderDecay;
	private JSliderScientific sliderSustain;
	private JSliderScientific sliderRelease;
	private JSliderScientific sliderGain;

	private JPanel mainPane, panelWaveForm, panelADSR, panelCommonValues;

	public UIControls() {
		comboboxOscillator = new JComboBox<>(new Oscillator[] {
			Oscillators.Sine, Oscillators.Triangle, Oscillators.SawTooth,
			Oscillators.SawToothInverted, Oscillators.Flat, Oscillators.Noise
		});
		comboboxOscillator.setFocusable(false);

		sliderLevel = new JSliderScientific(OperatorConsts.minLevel, OperatorConsts.maxLevel, 1);
		sliderDetune = new JSliderScientific(-OperatorConsts.maxDetune, OperatorConsts.maxDetune, 1);

		sliderFrequencyProportional = new JSliderScientific(OperatorConsts.minProportionalFrequency, OperatorConsts.maxProportionalFrequency, 1);
		sliderFrequencyProportional.setMinMax(OperatorConsts.minProportionalFrequency * 100, OperatorConsts.maxProportionalFrequency * 100);

		sliderFrequencyFixed = new JSliderScientific(OperatorConsts.minFixedFrequency, OperatorConsts.maxFixedFrequency, 10);

		checkboxFixedFrequency = new JCheckBox("Fixed, Hz");
		checkboxFixedFrequency.setHorizontalAlignment(SwingConstants.RIGHT);
		checkboxFixedFrequency.setHorizontalTextPosition(SwingConstants.LEFT);
		checkboxFixedFrequency.setFocusable(false);
		padRight(checkboxFixedFrequency);

		sliderAttack = new JSliderScientific(OperatorConsts.minAttack, OperatorConsts.maxAttack, Math.E);
		sliderDecay = new JSliderScientific(OperatorConsts.minDecay, OperatorConsts.maxDecay, Math.E);
		sliderSustain = new JSliderScientific(OperatorConsts.minSustain, OperatorConsts.maxSustain, 1);
		sliderRelease = new JSliderScientific(OperatorConsts.minRelease, OperatorConsts.maxRelease, Math.E);

		sliderGain = new JSliderScientific(0, 4, Math.E);
		sliderGain.setMinMax(0, 1000);
		sliderGain.setRounding(3);

		mainPane = new JPanel();

		LocalFactory<JPanel> panel = (args) -> {
			JPanel result = new JPanel();
			mainPane.add(result);
			return result;
		};

		panelWaveForm = panel.create();
		panelADSR = panel.create();
		panelCommonValues = panel.create();

		panelWaveForm.setBorder(BorderFactory.createTitledBorder("Wave form"));
		panelADSR.setBorder(BorderFactory.createTitledBorder("ADSR"));
		panelCommonValues.setBorder(BorderFactory.createTitledBorder("Global"));

		panelWaveForm.add(createLabel("Oscillator"));
		panelWaveForm.add(comboboxOscillator);
		panelWaveForm.add(createLabel(""));

		panelWaveForm.add(createLabel("Level, %"));
		panelWaveForm.add(sliderLevel);
		panelWaveForm.add(sliderLevel.getLabel());

		panelWaveForm.add(createLabel("Detune, Hz"));
		panelWaveForm.add(sliderDetune);
		panelWaveForm.add(sliderDetune.getLabel());

		panelWaveForm.add(createLabel("Frequency, %"));
		panelWaveForm.add(sliderFrequencyProportional);
		panelWaveForm.add(sliderFrequencyProportional.getLabel());

		panelWaveForm.add(checkboxFixedFrequency);
		panelWaveForm.add(sliderFrequencyFixed);
		panelWaveForm.add(sliderFrequencyFixed.getLabel());

		panelADSR.add(createLabel("Attack, s"));
		panelADSR.add(sliderAttack);
		panelADSR.add(sliderAttack.getLabel());

		panelADSR.add(createLabel("Decay, s"));
		panelADSR.add(sliderDecay);
		panelADSR.add(sliderDecay.getLabel());

		panelADSR.add(createLabel("Sustain, %"));
		panelADSR.add(sliderSustain);
		panelADSR.add(sliderSustain.getLabel());

		panelADSR.add(createLabel("Release, s"));
		panelADSR.add(sliderRelease);
		panelADSR.add(sliderRelease.getLabel());

		panelCommonValues.add(createLabel("Gain, %"));
		panelCommonValues.add(sliderGain);
		panelCommonValues.add(sliderGain.getLabel());

		mainPane.setLayout(null);
		setGridLayout(panelWaveForm, 3);
		setGridLayout(panelADSR, 3);
		setGridLayout(panelCommonValues, 3);

		realignUI();

		comboboxOscillator.addActionListener((ActionEvent e) -> doOnOperatorValuesChanged());

		for (JSlider slider : new JSlider[] {
			sliderLevel, sliderDetune, sliderFrequencyProportional, sliderFrequencyFixed,
			sliderAttack, sliderDecay, sliderSustain, sliderRelease
		})
			slider.addChangeListener((e) -> doOnOperatorValuesChanged());

		checkboxFixedFrequency.addChangeListener((ChangeEvent e) -> {
			boolean fixed = checkboxFixedFrequency.isSelected();

			sliderDetune.setVisible(!fixed);
			sliderDetune.getLabel().setVisible(!fixed);
			sliderFrequencyProportional.setVisible(!fixed);
			sliderFrequencyProportional.getLabel().setVisible(!fixed);
			sliderFrequencyFixed.setVisible(fixed);
			sliderFrequencyFixed.getLabel().setVisible(fixed);

			if (fixed)
				sliderFrequencyFixed.fireStateChangedExt();
			else
				sliderFrequencyProportional.fireStateChangedExt();
		});
		checkboxFixedFrequency.setSelected(!checkboxFixedFrequency.isSelected());

		sliderGain.addChangeListener((e) -> {
			if (onGainChanged != null)
				onGainChanged.execute(sliderGain.getValueSc());
		});
	}

	private void doOnOperatorValuesChanged() {
		if (onOperatorValuesChanged != null)
			onOperatorValuesChanged.execute(getOperatorValues());
	}

	private JLabel createLabel(String text) {
		JLabel result = new JLabel(text);
		result.setHorizontalAlignment(SwingConstants.RIGHT);
		padRight(result);
		return result;
	}

	private void padRight(JComponent component) {
		component.setBorder(new EmptyBorder(0, 0, 0, 10));
	}

	public JComponent getMainContainer() {
		return mainPane;
	}

	public void setBounds(Rectangle r) {
		mainPane.setBounds(r);
		realignUI();
	}

	private void realignUI() {
		if (mainPane == null)
			return;

		Rectangle bounds = new Rectangle();

		bounds.x = 0;
		bounds.y = 0;
		bounds.width = mainPane.getWidth() / 2;
		bounds.height = panelWaveForm.getHeight();
		panelWaveForm.setBounds(bounds);

		bounds.x += bounds.width;
		bounds.height = panelADSR.getHeight();
		panelADSR.setBounds(bounds);

		bounds.x = 0;
		bounds.width = mainPane.getWidth();
		bounds.y = (int) panelWaveForm.getBounds().getMaxY();
		bounds.height = panelCommonValues.getHeight();
		panelCommonValues.setBounds(bounds);

		mainPane.setSize(mainPane.getWidth(), (int) panelCommonValues.getBounds().getMaxY());
	}

	private void setGridLayout(Container container, int colCount) {
		int rowHeight = 25;
		int rowCount = container.getComponentCount() / colCount + 1;
		container.setLayout(new GridLayout(0, colCount));
		container.setSize(container.getWidth(), rowHeight * rowCount);
	}

	public void setOperatorValues(OperatorValues values) {
		comboboxOscillator.setSelectedItem(values.oscillator);
		sliderLevel.setValueSc(values.level);
		checkboxFixedFrequency.setSelected(values.frequencyFixed);
		if (values.frequencyFixed)
			sliderFrequencyFixed.setValueSc(values.frequencyLevel);
		else
			sliderFrequencyProportional.setValueSc(values.frequencyLevel);

		sliderDetune.setValueSc(values.detune);

		sliderAttack.setValueSc(values.attack);
		sliderDecay.setValueSc(values.decay);
		sliderSustain.setValueSc(values.sustain);
		sliderRelease.setValueSc(values.release);
	}

	public void setGain(double gain) {
		sliderGain.setValueSc(gain);
	}

	private OperatorValues getOperatorValuesResult = new OperatorValues();
	private OperatorValues getOperatorValues() {
		OperatorValues result = getOperatorValuesResult;
		result.oscillator = (Oscillator) comboboxOscillator.getSelectedItem();
		result.level = sliderLevel.getValueSc();
		result.detune = sliderDetune.getValueSc();
		result.frequencyFixed = checkboxFixedFrequency.isSelected();
		if (result.frequencyFixed)
			result.frequencyLevel = sliderFrequencyFixed.getValueSc();
		else
			result.frequencyLevel = sliderFrequencyProportional.getValueSc();

		result.attack = sliderAttack.getValueSc();
		result.decay = sliderDecay.getValueSc();
		result.sustain = sliderSustain.getValueSc();
		result.release = sliderRelease.getValueSc();

		return result;
	}

	public void setOperatorVisible(boolean visible) {
		panelWaveForm.setVisible(visible);
		panelADSR.setVisible(visible);
	}
}
