package syn.operator;

import java.util.ArrayList;
import java.util.List;

public class OperatorValues {
	static final List<Operator> emptyOperatorList = new ArrayList<>(0);

	public Oscillator oscillator = Oscillators.Sine;
	public double level = 0.5;
	public double detune = 0;
	public double frequencyLevel = 1;
	public boolean frequencyFixed = false;
	public volatile List<Operator> modulators = emptyOperatorList;
	public double attack = 0.05;
	public double decay = 0.05;
	public double sustain = 1;
	public double release = 0.1;
}
