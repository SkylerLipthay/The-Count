package com.skylerlipthay.thecount;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import com.sun.jna.Platform;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HMODULE;
import com.sun.jna.platform.win32.WinDef.LRESULT;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinUser;

public class MainWindow extends JFrame {
  private static final long serialVersionUID = -1579583724424467247L;

  private User32.HHOOK hook;
  private int modShift;
  private int modControl;
  private int modAlt;
  private volatile boolean quit;

  public MainWindow() {
    super("The Count");

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setResizable(true);

    final MainPanel mainPanel = new MainPanel();
    setContentPane(mainPanel);
    initializeMenuBar(mainPanel);
    initializeIcon();
    pack();
    setLocationRelativeTo(null);

    modShift = 0;
    modControl = 0;
    modAlt = 0;
    quit = false;

    if (Platform.isWindows()) {
      new Thread(new Runnable() {
        public void run() {
          startWindowsKeyListener(mainPanel);
        }
      }).start();
    } else {
      initializeSwingKeyListener(mainPanel);
    }

    this.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        quit = true;
      }
    });
  }

  private void startWindowsKeyListener(final MainPanel mainPanel) {
    User32.LowLevelKeyboardProc lpfn = new User32.LowLevelKeyboardProc() {
      public LRESULT callback(int nCode, WPARAM wParam,
          User32.KBDLLHOOKSTRUCT lParam) {
        final boolean isKeyDown = (wParam.intValue() == WinUser.WM_KEYDOWN)
            || (wParam.intValue() == WinUser.WM_SYSKEYDOWN);
        switch (lParam.vkCode) {
        case WinUser.VK_CONTROL:
          modControl = (modControl & 6) | (isKeyDown ? 1 : 0);
          break;

        case WinUser.VK_LCONTROL:
          modControl = (modControl & 5) | (isKeyDown ? 2 : 0);
          break;

        case WinUser.VK_RCONTROL:
          modControl = (modControl & 3) | (isKeyDown ? 4 : 0);
          break;

        case WinUser.VK_SHIFT:
          modShift = (modShift & 6) | (isKeyDown ? 1 : 0);
          break;

        case WinUser.VK_LSHIFT:
          modShift = (modShift & 5) | (isKeyDown ? 2 : 0);
          break;

        case WinUser.VK_RSHIFT:
          modShift = (modShift & 3) | (isKeyDown ? 4 : 0);
          break;

        case WinUser.VK_MENU:
          modAlt = (modAlt & 6) | (isKeyDown ? 1 : 0);
          break;

        case WinUser.VK_LMENU:
          modAlt = (modAlt & 5) | (isKeyDown ? 2 : 0);
          break;

        case WinUser.VK_RMENU:
          modAlt = (modAlt & 3) | (isKeyDown ? 4 : 0);
          break;

        default:
          for (KeyStroke hotKey : mainPanel.getSettings().hotKeys) {
            final boolean keysMatch = lParam.vkCode == WindowsKeyMap
                .getCode(hotKey);

            if (!keysMatch) {
              continue;
            }

            final boolean modifiersSatisfactory = WindowsKeyMap
                .modifiersSatisfactory(hotKey, modControl > 0, modShift > 0,
                    modAlt > 0);

            if (!isKeyDown || modifiersSatisfactory) {
              mainPanel.processKey(hotKey, isKeyDown);
            }
          }
        }

        return User32.INSTANCE.CallNextHookEx(hook, nCode, wParam,
            lParam.getPointer());
      }
    };

    HMODULE module = Kernel32.INSTANCE.GetModuleHandle(null);
    hook = User32.INSTANCE.SetWindowsHookEx(User32.WH_KEYBOARD_LL, lpfn,
        module, 0);

    if (hook == null) {
      return;
    }

    User32.MSG msg = new User32.MSG();

    while (!quit) {
      // hex arguments: WM_KEYFIRST, WM_KEYLAST
      int result = User32.INSTANCE.GetMessage(msg, null, 0x100, 0x109);
      if (result == -1) {
        break;
      } else {
        User32.INSTANCE.TranslateMessage(msg);
        User32.INSTANCE.DispatchMessage(msg);
      }
    }
    
    User32.INSTANCE.UnhookWindowsHookEx(hook);
  }

  private void initializeSwingKeyListener(final MainPanel mainPanel) {
    addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent event) {
        processKeyEvent(event, true);
      }

      @Override
      public void keyReleased(KeyEvent event) {
        processKeyEvent(event, false);
      }

      private void processKeyEvent(KeyEvent event, boolean keyDown) {
        final Settings settings = mainPanel.getSettings();

        for (KeyStroke hotKey : settings.hotKeys) {
          if (event.getKeyCode() != hotKey.getKeyCode()) {
            continue;
          }

          final int hotKeyModifiers = hotKey.getModifiers();
          final int eventModifiers = event.getModifiers();
          final boolean control = (eventModifiers & KeyEvent.CTRL_DOWN_MASK | eventModifiers
              & KeyEvent.CTRL_MASK) > 0;
          final boolean shift = (eventModifiers & KeyEvent.SHIFT_DOWN_MASK | eventModifiers
              & KeyEvent.SHIFT_MASK) > 0;
          final boolean alt = (eventModifiers & KeyEvent.ALT_DOWN_MASK | eventModifiers
              & KeyEvent.ALT_MASK) > 0;

          final boolean ignoreModifiers = !keyDown || hotKeyModifiers == 0;
          final boolean modifiersMatch = WindowsKeyMap.modifiersSatisfactory(
              hotKey, control, shift, alt);

          if (ignoreModifiers || modifiersMatch) {
            mainPanel.processKey(hotKey, keyDown);
          }
        }
      }
    });
  }

  private void initializeMenuBar(final MainPanel mainPanel) {
    JMenuBar menuBar = new JMenuBar();
    JMenu menu = new JMenu("Actions");
    menuBar.add(menu);

    JMenuItem menuItem = new JMenuItem("Increase");
    menuItem.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent event) {
        mainPanel.counterIncrease();
      }
    });
    menu.add(menuItem);

    menuItem = new JMenuItem("Decrease");
    menuItem.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent event) {
        mainPanel.counterDecrease();
      }
    });
    menu.add(menuItem);

    menuItem = new JMenuItem("Reset");
    menuItem.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent event) {
        mainPanel.counterReset();
      }
    });
    menu.add(menuItem);

    menuItem = new JMenuItem("Manual set");
    menuItem.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent event) {
        mainPanel.counterManualSet();
      }
    });
    menu.add(menuItem);

    setJMenuBar(menuBar);
  }
  
  
  private void initializeIcon() {
    InputStream stream = MainWindow.class.getResourceAsStream("icon.png");

    if (stream == null) {
      System.out.println("Unable to load icon");
      return;
    }
    
    Image iconImage;

    try {
      iconImage = ImageIO.read(stream);
    } catch (IOException exception) {
      System.out.println("Unable to load icon");
      return;
    }

    try {
      stream.close();
    } catch (IOException exception) {
      // ignore, might just be packaged resource
    }
    
    this.setIconImage(iconImage);
  }
}
