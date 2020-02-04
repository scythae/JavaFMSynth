package utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

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

	public static void saveObjectToFile(Object obj, String fileName) {
		File file;

		try {
			file = new File(fileName);
			new File(file.getParent()).mkdirs();
			file.delete();
			file.createNewFile();
		} catch (Exception e) {
			e.printStackTrace();
			complain("Cannot create file: " + fileName);
			return;
		}

		try {
			FileOutputStream fileOut = new FileOutputStream(file);
			ObjectOutputStream objOut = new ObjectOutputStream(fileOut);
			objOut.writeObject(obj);
			objOut.close();
			fileOut.close();
		} catch (IOException e) {
			e.printStackTrace();
			complain("Cannot save object to file: " + fileName);
		}
	};

	public static Object loadObjectFromFile(String fileName) {
		File file;

		String complainMessage = "Cannot find file: " + fileName;
		try {
			file = new File(fileName);
			if (!file.exists())
				throw new Exception("File doesn't exist.");
		} catch (Exception e) {
			complain(complainMessage + System.lineSeparator() + e.getMessage());
			return null;
		}

		Object result = null;
		try {
			FileInputStream fileIn = new FileInputStream(file);
			ObjectInputStream objIn = new ObjectInputStream(fileIn);
			result = objIn.readObject();
			objIn.close();
			fileIn.close();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			complain("Cannot load object from file: " + fileName);
		}

		return result;
	};

	@SuppressWarnings("unchecked")
	public static <T extends Serializable> T clone(T source) {
		T result = null;

		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();

		try {
			ObjectOutputStream objOut = new ObjectOutputStream(byteOut);
			objOut.writeObject(source);
			objOut.close();

			ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());

			ObjectInputStream objIn = new ObjectInputStream(byteIn);
			result = (T) objIn.readObject();
			objIn.close();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			complain("Cannot clone the object.");
			return null;
		}

		return result;
	};

	public static <T> T cast(Object obj, Class<T> targetClass) {
		if (targetClass.isInstance(obj))
			return targetClass.cast(obj);

		return null;
	}

	public static String removeFileExtension (String fileName) {
		return fileName.substring(0, fileName.lastIndexOf("."));
	}
}
