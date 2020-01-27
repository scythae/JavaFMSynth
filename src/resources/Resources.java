package resources;

import java.net.URL;

public class Resources {
	private static Resources r = new Resources();

	private static URL getURL(String name) {
		return r.getClass().getResource(name);
	}

	public static final URL keyboardPicture = getURL("Keyboard.png");
}
