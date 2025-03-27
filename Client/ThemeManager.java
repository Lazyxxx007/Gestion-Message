package Client;

import java.awt.Color;

public class ThemeManager {
    private boolean darkMode;

    public ThemeManager() {
        this.darkMode = false; // Mode clair par défaut
    }

    public boolean isDarkMode() {
        return darkMode;
    }

    public void toggleTheme() {
        darkMode = !darkMode;
    }

    public Color getBackgroundColor() {
        return darkMode ? Color.BLACK : Color.WHITE;
    }

    public Color getForegroundColor() {
        return darkMode ? Color.WHITE : Color.BLACK;
    }

    public Color getButtonBackgroundColor() {
        return darkMode ? new Color(50, 50, 50) : new Color(200, 200, 200);
    }
}