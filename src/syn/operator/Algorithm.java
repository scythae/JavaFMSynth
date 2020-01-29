package syn.operator;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;

import utils.Utils;

public class Algorithm implements Externalizable {
	private final static ArrayList<Operator> emptyOperatorList = new ArrayList<>(0);
	private Object operatorsLock;

	private volatile ArrayList<Operator> operators = emptyOperatorList;

	public Algorithm() {
		operatorsLock = new Object();
	}

	public List<Operator> getOperators() {
		return operators;
	}

	public Algorithm addOperator(Operator operator) {
		if (operator == null)
			return this;

		ArrayList<Operator> tmpOperators;
		synchronized (operatorsLock) {
			tmpOperators = new ArrayList<>(operators.size() + 1);
			tmpOperators.addAll(operators);
			tmpOperators.add(operator);
		}

		operators = tmpOperators;

		return this;
	};

	public void removeOperator(Operator operator) {
		if (operator == null)
			return;

		ArrayList<Operator> tmpOperators;
		synchronized (operatorsLock) {
			tmpOperators = new ArrayList<>(operators);
			tmpOperators.remove(operator);
		}

		operators = tmpOperators;
	};

	public void cleanOperators() {
		synchronized (operatorsLock) {
			operators = emptyOperatorList;
		}
	};

	public void addNote(int keyCode) {
		for (Operator op: operators)
			op.addNote(keyCode);
	}

	public void releaseNote(int keyCode) {
		for (Operator op: operators)
			op.releaseNote(keyCode);
	}

	public void saveToFile(String fileName) {
		Utils.saveObjectToFile(fileName, this);
	};

	public static Algorithm loadFromFile(String fileName) {
		return (Algorithm) Utils.loadObjectFromFile(fileName);
	}

	@Override
	public String toString() {
		return "Algorithm";
	}

	private static final long serialVersionUID = 5647407837514451078L;
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		int operatorCount = in.readInt();
		ArrayList<Operator> tmpOperators = new ArrayList<>(operatorCount);

		for (int i = 0; i < operatorCount; i++)
			tmpOperators.add((Operator) in.readObject());

		operators = tmpOperators;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		ArrayList<Operator> tmpOperators = operators;
		out.writeInt(tmpOperators.size());

		for (Operator op : tmpOperators)
			out.writeObject(op);
	};
}
