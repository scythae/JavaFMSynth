package syn.operator;

import utils.LocalFactory;

public class AlgorithmBank {
	public static final Algorithm TubularBell;
	public static final Algorithm KickDrum;
	public static final Algorithm Guitar;
	public static final Algorithm GhostPad;

	static {
		LocalFactory<Operator> defaultOperator = (args) -> {
			return new Operator().setFrequencyProportional(1).setLevel(0.5).setAttack(0.1).
				setDecay(0.1).setSustain(1).setRelease(0.1);
		};

		LocalFactory<Operator> bellCarrier = (args) -> {
			return defaultOperator.create().setAttack(0.015).setDecay(2).setSustain(0).setRelease(2);
		};
		LocalFactory<Operator> bellModulator = (args) -> {
			return bellCarrier.create().setLevel(1).setFrequencyProportional(3.5);
		};

		TubularBell = new Algorithm(
		).addOperator((Operator)
			bellCarrier.create().setDetune(-5).addOperator(
				bellModulator.create().setDetune(-5)
			)
		).addOperator((Operator)
			bellCarrier.create().setDetune(5).addOperator(
				bellModulator.create().setDetune(5)
			)
		);

		KickDrum = new Algorithm(
		).addOperator((Operator)
			defaultOperator.create().setFrequencyProportional(0.3).setLevel(1).
			setAttack(0).setDecay(0.11).setSustain(0).setRelease(0.1)
			.addOperator(
				defaultOperator.create().setFrequencyProportional(0.05).setLevel(1).
				setAttack(0).setDecay(0.02).setSustain(0).setRelease(0.1)
			).addOperator(
				defaultOperator.create().setFrequencyProportional(8).setLevel(0.1).setOscillator(Oscillators.Noise).
				setAttack(0.015).setDecay(0.9).setSustain(0).setRelease(0.7)
			)
		).addOperator(defaultOperator.create().setFrequencyProportional(0.2).setLevel(0.4).setDetune(-12).
			setAttack(0.045).setDecay(0.6).setSustain(0).setRelease(0.6)
		);

		Guitar = new Algorithm(
		).addOperator((Operator)
			bellCarrier.create().setDetune(-5).addOperator(
				bellModulator.create().setDetune(-5)
			)
		).addOperator((Operator)
			bellCarrier.create().setDetune(5).addOperator(
				bellModulator.create().setDetune(5)
			)
		);

		LocalFactory<Operator> padCarrier = (args) -> {
			return defaultOperator.create().setAttack(2).setSustain(0.7).setRelease(1.5);
		};
		LocalFactory<Operator> padModulator = (args) -> {
			return defaultOperator.create().setSustain(0.5).setRelease(5);
		};

		GhostPad = new Algorithm(
		).addOperator((Operator)
			padCarrier.create().setDetune(-11).addOperator(
				padModulator.create().setDetune(3).setFrequencyProportional(2)
			)
		).addOperator((Operator)
			padCarrier.create().setDetune(12).addOperator(
				padModulator.create().setDetune(5).setFrequencyProportional(2)
			)
		).addOperator((Operator)
			padCarrier.create().setDetune(-9).addOperator(
				padModulator.create().setDetune(3)
			)
		).addOperator((Operator)
			padCarrier.create().setDetune(10).addOperator(
				padModulator.create().setDetune(5)
			)
		).addOperator((Operator)
			padCarrier.create().setDetune(-4).setOscillator(Oscillators.Triangle).addOperator(
				padModulator.create().setDetune(3)
			)
		).addOperator((Operator)
			padCarrier.create().setDetune(6).setOscillator(Oscillators.Triangle).addOperator(
				padModulator.create().setDetune(5)
			)
		);
	}
}
