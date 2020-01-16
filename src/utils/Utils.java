package utils;

public class Utils {
	public final static double doublePi = 2.0 * Math.PI;

	public interface Callback {
		public void execute();
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
				Log.out(timerName + " error.");
				e.printStackTrace();
			}
		});
		result.setDaemon(true);
		result.setPriority(Thread.MIN_PRIORITY);
		result.start();
		return result;
	};

	public static int keepInRange(int val, int min, int max) {
		int range = max - min + 1;
		while (val < min)
			val += range;
		while (val > max)
			val -= range;
		return val;
	}
}
