package syn;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import application.Application;
import utils.Log;

class SoundPlayer {
	private static final int samplesPerSecond = 44100;
	private static final int bitsPerSample = 16;
	private static final int channelsNumber = 1;
	private static final int sampleAmplitude = 31000;
	private static final double secondsPerBuffer = 0.025;

	private static final int samplesPerBuffer = (int) (samplesPerSecond * secondsPerBuffer) / bitsPerSample * bitsPerSample;
	static final double sampleStepSeconds = 1.0 / samplesPerSecond;

	private SoundPlayerThread thread;

	public SoundPlayer(WaveGenerator waveGenerator) {
		if (waveGenerator == null) {
			Log.out("SoundPlayer requires non-null waveGenerator.");
			return;
		}

		thread = new SoundPlayerThread();
		thread.waveGenerator = waveGenerator;
		thread.start();
	}

	private class SoundPlayerThread extends Thread {
		private WaveGenerator waveGenerator;

		@Override
		public void run() {
			try {
				playWhileAlive();
			} catch (LineUnavailableException e) {
				e.printStackTrace();
			}
		}

		private void playWhileAlive() throws LineUnavailableException {
			AudioFormat audioFormat = new AudioFormat(samplesPerSecond, bitsPerSample, channelsNumber, true, false);
			SourceDataLine line = AudioSystem.getSourceDataLine(audioFormat);
			line.open(audioFormat);
			line.start();

			byte[] buf = new byte[samplesPerBuffer * 2];

			int fillingThreshold = line.available() - samplesPerBuffer;

			try {
				while (Application.isRunning()) {
					boolean lineIsFull = line.available() < fillingThreshold;
					if (lineIsFull) {
						Thread.sleep(1);
					} else {
						int byteIndex = 0;
						for (int i = 0; i < samplesPerBuffer; i++) {
							short sampleVal =  (short) (waveGenerator.getSampleValue() * sampleAmplitude);
							buf[byteIndex++] = (byte) sampleVal;
							buf[byteIndex++] = (byte) (sampleVal >> 8);
						}
						line.write(buf, 0, buf.length);
					}
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}

			line.stop();
			line.close();
		}
	}
}
