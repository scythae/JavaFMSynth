package syn.operator;

public class Oscillators {
	public final static Oscillator Sine;
	public final static Oscillator Triangle;
	public final static Oscillator Saw;
	public final static Oscillator Flat;
	public final static Oscillator Noise;

	static {
		Sine = (phase) -> {
			return Math.sin(phase);
		};

		Triangle = Sine;
		Saw = Sine;

		Flat = (phase) -> {
			return Math.signum(Sine.getSampleValue(phase));
		};

		Noise = (phase) -> {
			return Math.random() * 2 - 1;
		};
	}


}
