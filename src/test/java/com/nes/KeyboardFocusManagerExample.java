package com.nes;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by RoyChan on 2018/3/14.
 */
public class KeyboardFocusManagerExample {
    public HashSet<Component> comps = new HashSet<Component>();
    public HashMap<Integer, Character> swaps = new HashMap<Integer, Character>();

    private static volatile boolean wPressed = false;
    public static boolean isWPressed() {
        return wPressed;
    }

    public static void main(String[] args) {
        Frame frame = new Frame();
        TextField tf = new TextField();
        frame.add(tf, BorderLayout.NORTH);
        TextField tf2 = new TextField();
        frame.add(tf2, BorderLayout.SOUTH);
        KeyboardFocusManagerExample med = new KeyboardFocusManagerExample();
        med.swaps.put(new Integer(KeyEvent.VK_F4), new Character('\u00bd'));
        med.comps.add(tf);
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent ke) {
                // This example converts all typed keys to upper
                // case
                switch (ke.getID()) {
                    case KeyEvent.KEY_PRESSED:
                        if (ke.getKeyCode() == KeyEvent.VK_W) {
                            wPressed = true;
                        }
                        break;

                    case KeyEvent.KEY_RELEASED:
                        if (ke.getKeyCode() == KeyEvent.VK_W) {
                            wPressed = false;
                        }
                        break;
                }
//                if (ke.getKeyCode() == KeyEvent.KEY_TYPED) {
//                    System.out.println("typed");
//                    wPressed = true;
//                }
                // If the key should not be dispatched to the
                // focused component, set discardEvent to true
                boolean discardEvent = false;
                return discardEvent;
            }
        });
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        frame.pack();
        frame.setVisible(true);

        Thread thread = new Thread(() -> {
            while (true){
//            Thread.sleep(1000);
                if (KeyboardFocusManagerExample.isWPressed()){
                    System.out.println("w pressed");
                }
            }
        });
        thread.start();
//        EventQueue.invokeLater(r);
    }
}
