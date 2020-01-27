package application;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.List;

import javax.swing.JPanel;

import syn.Synthesizer;
import utils.Utils;

public class WaveViewer extends JPanel {
	private static final long serialVersionUID = -8481038351597037986L;
	private static final int repaintIntervalMs = 10;
	private List<Double> lastSamples;

	WaveViewer (Synthesizer synthCore) {
		lastSamples = synthCore.getLastSamples();
		Utils.startTimer(
				"WaveViewer repaint",
				repaintIntervalMs,
				() -> {
					lastSamples = synthCore.getLastSamples();
					repaint();
				}
		);
	}

	@Override
	public void paintComponent(Graphics graphics) {
		super.paintComponent(graphics);

		Graphics2D g = (Graphics2D) graphics;

		int halfHeight = getHeight() / 2;
		int width = getWidth();
		g.drawLine(0, halfHeight, width, halfHeight);

		Point lineBegin = new Point();
		Point lineEnd = new Point();

		int x = 0;
		lineEnd.setLocation(0, halfHeight * (1.0 - lastSamples.get(0)));

		for (double sample : lastSamples) {
			lineBegin.setLocation(lineEnd);
			lineEnd.setLocation(x++, halfHeight * (1.0 - sample));
			g.drawLine(lineBegin.x, lineBegin.y, lineEnd.x, lineEnd.y);

			if (x >= width);
		}
	}
}