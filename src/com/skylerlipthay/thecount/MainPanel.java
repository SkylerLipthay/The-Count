package com.skylerlipthay.thecount;

import java.awt.Dimension;
import java.awt.Font;
import java.math.BigInteger;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;

public class MainPanel extends JPanel {
  private static final long serialVersionUID = 6924976121549861760L;

  private Settings settings;
  private Counter counter;
  private CountLabel countLabel;
  private volatile boolean ignoreHotKeys;

  public MainPanel() {
    counter = new Counter();
    settings = new Settings("./config.txt");
    countLabel = new CountLabel(counter);
    add(countLabel);
    ignoreHotKeys = false;

    initializeUI();
  }

  public void processKey(KeyStroke hotKey, boolean keyDown) {
    if (ignoreHotKeys) {
      return;
    }
    
    if (hotKey == settings.hotKeyIncrease && keyDown) {
      counterIncrease();
    } else if (hotKey == settings.hotKeyDecrease && keyDown) {
      counterDecrease();
    } else if (hotKey == settings.hotKeyReset && keyDown) {
      counterReset();
    } else if (hotKey == settings.hotKeyManualSet && !keyDown) {
      counterManualSet();
    }
  }

  public Settings getSettings() {
    return settings;
  }

  public void counterIncrease() {
    counter.increase();
    countLabel.updateCount();
  }

  public void counterDecrease() {
    counter.decrease();
    countLabel.updateCount();
  }

  public void counterReset() {
    counter.reset();
    countLabel.updateCount();
  }

  public void counterManualSet() {
    ignoreHotKeys = true;
    
    final MainPanel mainPanel = this;
    
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        final String prompt = "Enter new count";
        BigInteger count = counter.getCount();

        final String newCount = JOptionPane.showInputDialog(mainPanel, prompt, count);

        if (newCount == null) {
          // user pressed cancel
          ignoreHotKeys = false;
          return;
        }

        try {
          count = new BigInteger(newCount);
        } catch (NumberFormatException exception) {
          final String message = "Invalid number";
          JOptionPane.showMessageDialog(mainPanel, message);
          ignoreHotKeys = false;
          return;
        }

        counter.set(count);
        countLabel.updateCount();
        ignoreHotKeys = false;
      }
    });
  }

  private void initializeUI() {
    setBackground(settings.backgroundColor);
    countLabel.setFont(new Font(settings.fontName, settings.fontStyle,
        settings.fontSize));
    countLabel.setForeground(settings.textColor);

    Dimension windowSize = new Dimension(200, 150);
    setPreferredSize(windowSize);

    SpringLayout layout = new SpringLayout();
    setLayout(layout);

    layout.putConstraint(SpringLayout.NORTH, countLabel, 0, SpringLayout.NORTH,
        this);
    layout.putConstraint(SpringLayout.WEST, countLabel, 0, SpringLayout.WEST,
        this);
    layout.putConstraint(SpringLayout.SOUTH, countLabel, 0, SpringLayout.SOUTH,
        this);
    layout.putConstraint(SpringLayout.EAST, countLabel, 0, SpringLayout.EAST,
        this);
  }
}
