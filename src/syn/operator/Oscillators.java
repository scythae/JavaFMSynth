package syn.operator;

import utils.Utils;

public class Oscillators {
	public final static Oscillator Sine;
	public final static Oscillator Triangle;
	public final static Oscillator SawTooth;
	public final static Oscillator SawToothInverted;
	public final static Oscillator Flat;
	public final static Oscillator Noise;

	static {
		Sine = new Oscillator() {
			@Override
			public String toString() {
				return "Sine";
			}

			@Override
			public double getSampleValue(double phase) {
				return Math.sin(phase);
			}
		};

		Triangle = new Oscillator() {
			@Override
			public String toString() {
				return "Triangle";
			}

			@Override
			public double getSampleValue(double phase) {
				phase = getNormalizedPhase(phase + Utils.halfPi);
				double result = - 1 + phase / Utils.halfPi;

				if (phase < Math.PI)
					return result;
				else
					return 2 - result;
			}
		};

		SawTooth = new Oscillator() {
			@Override
			public String toString() {
				return "SawTooth";
			}

			@Override
			public double getSampleValue(double phase) {
				phase = getNormalizedPhase(phase + Utils.halfPi);
				return - 1 + phase / Math.PI;
			}
		};

		SawToothInverted = new Oscillator() {
			@Override
			public String toString() {
				return "SawToothInverted";
			}

			@Override
			public double getSampleValue(double phase) {
				return -SawTooth.getSampleValue(phase);
			}
		};

		Flat = new Oscillator() {
			@Override
			public String toString() {
				return "Flat";
			}

			@Override
			public double getSampleValue(double phase) {
				if (getNormalizedPhase(phase) < Math.PI)
					return 1;
				else
					return -1;
			}
		};

		Noise = new Oscillator() {
			@Override
			public String toString() {
				return "Noise";
			}

			@Override
			public double getSampleValue(double phase) {
				return Math.random() * 2 - 1;
			}
		};
	}

	private static double getNormalizedPhase(double phase) {
		return phase - (Utils.doublePi * (int) (phase / Utils.doublePi));
	}

	public static Oscillator getByName(String oscillatorName) {
		Oscillator result = null;
		try {
			result = (Oscillator) Oscillators.class.getField(oscillatorName).get(null);
		} catch (Exception e) {
			e.printStackTrace();
			Utils.complain("Cannot find oscillator with this name: " + oscillatorName);
		}

		return result;
	}
}
