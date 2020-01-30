package application.swingUI;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import syn.Synthesizer;
import utils.Utils;

public class JWaveViewer extends JPanel {
	private static final long serialVersionUID = -8481038351597037986L;
	private static final int repaintIntervalMs = 10;
	private List<Double> lastSamples = new ArrayList<>(0);

	public JWaveViewer (Synthesizer synthCore) {
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

		boolean gotOverload = false;
		for (double sample : lastSamples) {
			lineBegin.setLocation(lineEnd);
			lineEnd.setLocation(x++, halfHeight * (1.0 - sample));
			g.drawLine(lineBegin.x, lineBegin.y, lineEnd.x, lineEnd.y);

			if (sample > 1)
				gotOverload = true;

			if (x >= width)
				break;
		}

		if (gotOverload) {
			Color transparentRed = new Color(1.0f, 0.8f, 0.8f, 0.2f);
			g.setColor(transparentRed);
			g.fillRect(0, 0, getWidth(), getHeight());
		}
	}
}