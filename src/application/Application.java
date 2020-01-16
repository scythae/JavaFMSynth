package application;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import syn.Synthesizer;
import syn.input.KeyInput;

public class Application {
	private static volatile boolean running = false;

	private JFrame frame;
	private Synthesizer synthesizer;
	private KeyInput keyInput;

	public Application() {
		if (running)
			throw new RuntimeException("Application is already launched.");

		running = true;

		frame = new JFrame("Synth");
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		frame.setSize(640, 480);
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.setLayout(null);
		frame.addKeyListener(createKeyListener());
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				running = false;
			};
		});
		frame.setVisible(true);

		keyInput = new KeyInput();
		synthesizer = new Synthesizer(keyInput);
		keyInput.setTimeProvider(synthesizer);

		initUI();
	}

	public static boolean isRunning() {
		return running;
	}

	private KeyListener createKeyListener() {
		return new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				keyInput.keyDown(e.getKeyCode());
			}

			@Override
			public void keyReleased(KeyEvent e) {
				keyInput.keyUp(e.getKeyCode());
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}
		};
	}

	private void initUI() {
		JPanel waveViewer = new WaveViewer(synthesizer);
		Rectangle bounds = (Rectangle) frame.getContentPane().getBounds().clone();
		bounds.grow(-10, -10);
		waveViewer.setBounds(bounds);
		frame.add(waveViewer);
	}
}
