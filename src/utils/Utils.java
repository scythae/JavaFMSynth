package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Utils {
	public final static double doublePi = 2.0 * Math.PI;
	public final static double halfPi = 0.5 * Math.PI;

	public interface Callback {
		public void execute();
	}

	public interface CallbackArg<T> {
		public void execute(T arg);
	}

	public static Thread startTimer(String timerName, int intervalMs, Callback callback) {
		if (callback == null)
			return null;

		Thread result = new Thread(() -> {
			try {
				while (true) {
					callback.execute();
					Thread.sleep(intervalMs);
				}
			} catch (InterruptedException e) {
				Utils.complain(timerName + " error.");
				e.printStackTrace();
			}
		});
		result.setDaemon(true);
		result.setPriority(Thread.MIN_PRIORITY);
		result.start();
		return result;
	};

	public static void complain(String text) {
		Log.out(text);
	};

	public static boolean doubleEquals(double a, double b) {
		return Double.compare(a, b) == 0;
	}

	public static void saveObjectToFile(String fileName, Object obj) {
		try {
			File file = new File(fileName);
			new File(file.getParent()).mkdirs();

			file.delete();
			file.createNewFile();
			FileOutputStream fos = new FileOutputStream(file);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(obj);
			oos.close();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
			complain("Cannot save object to file " + fileName);
		}
	};

	public static Object loadObjectFromFile(String fileName) {
		Object result = null;
		try {
			FileInputStream fis = new FileInputStream(fileName);
			ObjectInputStream ois = new ObjectInputStream(fis);
			result = ois.readObject();
			ois.close();
			fis.close();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			complain("Cannot load object from file " + fileName);
		}

		return result;
	};

	public static <T> T cast(Object obj, Class<T> targetClass) {
		if (targetClass.isInstance(obj))
			return targetClass.cast(obj);

		return null;
	}
}
