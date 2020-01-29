package syn.operator;

import utils.LocalFactory;

public class DefaultAlgorithms {
	public static final Algorithm TubularBell;

	static {
		LocalFactory<Operator> bellCarrier = () -> {
			return new Operator().setAttack(0.015).setDecay(2).setSustain(0).setRelease(2);
		};

		LocalFactory<Operator> bellModulator = () -> {
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
	}
}
