package syn;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import syn.operator.Algorithm;
import utils.Utils;

public class Patch implements Externalizable {
	public String name = "";
	public volatile Algorithm algorithm = new Algorithm();
	public volatile double gain = 1;

	public Patch copyFrom(Patch source) {
		this.algorithm = Utils.clone(source.algorithm);
		this.gain = source.gain;
		return this;
	}

	@Override
	public String toString() {
		return name;
	}

	private static final long serialVersionUID = -2261980214684024089L;
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		name = in.readUTF();
		gain = in.readDouble();
		algorithm.readExternal(in);
	}
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(name);
		out.writeDouble(gain);
		algorithm.writeExternal(out);
	}
}
