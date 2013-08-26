package com.skylerlipthay.thecount;

import java.awt.Color;
import java.awt.Font;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import javax.swing.KeyStroke;

public class Settings {
  public KeyStroke hotKeyIncrease = KeyStroke.getKeyStroke("1");
  public KeyStroke hotKeyDecrease = KeyStroke.getKeyStroke("2");
  public KeyStroke hotKeyReset = KeyStroke.getKeyStroke("3");
  public KeyStroke hotKeyManualSet = KeyStroke.getKeyStroke("4");
  public KeyStroke[] hotKeys;

  public Color backgroundColor = Color.LIGHT_GRAY;
  public Color textColor = Color.DARK_GRAY;

  public String fontName = Font.SANS_SERIF;
  public int fontSize = 24;
  public int fontStyle = Font.PLAIN;

  public Settings(String settingsPath) {
    Properties properties = new Properties();
    boolean propertiesValid = false;

    try {
      FileInputStream stream = new FileInputStream(settingsPath);
      properties.load(stream);
      propertiesValid = true;
    } catch (FileNotFoundException exception) {
      System.out.println("Properties file does not exist, continuing...");
    } catch (IOException exception) {
      System.out.println("Properties file invalid, continuing...");
    }

    if (propertiesValid) {
      hotKeyIncrease = loadKeyStroke(properties, "increase", hotKeyIncrease);
      hotKeyDecrease = loadKeyStroke(properties, "decrease", hotKeyDecrease);
      hotKeyReset = loadKeyStroke(properties, "reset", hotKeyReset);
      hotKeyManualSet = loadKeyStroke(properties, "set", hotKeyManualSet);

      backgroundColor = loadColor(properties, "bg_color", backgroundColor);
      textColor = loadColor(properties, "text_color", textColor);

      fontName = properties.getProperty("font_name", fontName);
      fontSize = loadInt(properties, "font_size", fontSize);
      fontStyle = loadFontStyle(properties, "font_style", fontStyle);
    }

    hotKeys = new KeyStroke[] { hotKeyIncrease, hotKeyDecrease, hotKeyReset,
        hotKeyManualSet };
  }

  private static KeyStroke loadKeyStroke(Properties properties, String keyName,
      KeyStroke fallback) {
    String setting = properties.getProperty(keyName);
    if (setting == null) {
      System.out.println("Using fallback for " + keyName + " hot key");
      return fallback;
    }

    KeyStroke keyStroke = KeyStroke.getKeyStroke(setting);
    if (keyStroke == null) {
      System.out.println("Hotkey setting for " + keyName + " is invalid");
      return fallback;
    }

    return keyStroke;
  }

  private static Color loadColor(Properties properties, String keyName,
      Color fallback) {
    String setting = properties.getProperty(keyName);
    if (setting == null) {
      System.out.println("Using fallback for " + keyName + " color");
      return fallback;
    }

    Color color;

    try {
      color = Color.decode(setting);
    } catch (NumberFormatException exception) {
      System.out.println("Color setting for " + keyName + " is invalid");
      return fallback;
    }

    return color;
  }

  private static int loadInt(Properties properties, String keyName, int fallback) {
    String setting = properties.getProperty(keyName);
    if (setting == null) {
      System.out.println("Using fallback for " + keyName + " int");
      return fallback;
    }

    int value;

    try {
      value = Integer.parseInt(setting);
    } catch (NumberFormatException exception) {
      System.out.println("Int setting for " + keyName + " is invalid");
      return fallback;
    }

    return value;
  }
  
  private static int loadFontStyle(Properties properties, String keyName, int fallback) {
    String setting = properties.getProperty(keyName);
    if (setting == null) {
      System.out.println("Using fallback for " + keyName + " font style");
      return fallback;
    }

    int value = fallback;
    
    if (setting.compareToIgnoreCase("plain") == 0) {
      value = Font.PLAIN;
    } else if (setting.compareToIgnoreCase("bold") == 0) {
      value = Font.BOLD;
    } else if (setting.compareToIgnoreCase("italic") == 0) {
      value = Font.ITALIC;
    } else if (setting.compareToIgnoreCase("bold_italic") == 0) {
      value = Font.BOLD | Font.ITALIC;
    } else {
      System.out.println("Font style setting for " + keyName + " is invalid");
    }

    return value;
  }
}
