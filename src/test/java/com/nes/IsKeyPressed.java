package com.nes;

import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Created by RoyChan on 2018/3/14.
 */
public class IsKeyPressed {
    private static volatile boolean wPressed = false;
    public static boolean isWPressed() {
        return wPressed;
    }

    public static void main(String[] args) throws InterruptedException {
        Runnable runnable = () -> {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {

                @Override
                public boolean dispatchKeyEvent(KeyEvent ke) {
                    synchronized (IsKeyPressed.class) {
                        if (ke.getKeyCode() == KeyEvent.KEY_TYPED) {
                            System.out.println("typed");
                            wPressed = true;
                        }
                        return false;
                    }
                }
            });


        };

        Thread thread = new Thread(() -> {
            while (true){
//            Thread.sleep(1000);
                if (IsKeyPressed.isWPressed()){
                    System.out.println("w pressed");
                }
            }
        });
        thread.start();


        EventQueue.invokeLater(runnable);
    }
}
