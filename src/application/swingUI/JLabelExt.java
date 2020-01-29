package application.swingUI;

import java.awt.Image;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import utils.Utils;

public class JLabelExt extends JLabel{
	private static final long serialVersionUID = 338138015749714459L;

	public void setScaledIcon(URL url) {
		if (url != null) {
			ImageIcon icon = new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(getWidth(), getHeight(), Image.SCALE_DEFAULT));
			setIcon(icon);
		} else {
			Utils.complain("Cannot load keyboard pic.");
		}
	}
}
