package com.skylerlipthay.thecount;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class TheCount {
  public static void main(String[] args) {
    // Mac OS X particulars
    System.setProperty("apple.laf.useScreenMenuBar", "true");
    System.setProperty("com.apple.mrj.application.apple.menu.about.name",
        "The Count");

    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Throwable exception) {
      // fail silently
    }

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        final MainWindow window = new MainWindow();
        window.setVisible(true);
      }
    });
  }
}
