package com.nes.ui.view;

import com.nes.core.Cartridge;
import com.nes.core.Console;
import com.nes.ui.*;
import com.nes.ui.Window;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.NativeType;

import java.awt.*;
import java.util.Arrays;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * Created by RoyChan on 2018/3/14.
 */
public class GameView implements View {

    public static final int padding = 0;

    Director director;
    Console console;
    String title;
    String hash;
    int texture;
    boolean record;
    Image[] frames;

    public GameView(Director director, Console console, String title, String hash){
        this.director = director;
        this.console = console;
        this.title = title;
        this.hash = hash;
        this.texture = Util.createTexture();
    }

    @Override
    public void Enter() {
        GL11.glClearColor(0, 0, 0, 1);
        this.director.SetTitle(this.title);
        this.console.setAudioChannel(this.director.audio.channel);
        this.director.window.SetKeyCallback(this.onKey());
        // load sram
        Cartridge cartridge = this.console.Cartridge;
        if(cartridge.Battery != 0){
            byte[] sram = Util.readSRAM(Util.sramPath(this.hash));
            cartridge.SRAM = sram;
        }
    }

    @Override
    public void Exit() {
        this.director.window.SetKeyCallback(null);
        this.console.setAudioChannel(null);
        // save sram
        Cartridge cartridge = this.console.Cartridge;
        if(cartridge.Battery != 0){
            Util.writeSRAM(Util.sramPath(this.hash), cartridge.SRAM);
        }
    }

    @Override
    public void Update(float t, float dt) {
        if(dt > 1){
            dt = 0;
        }
        com.nes.ui.Window window = this.director.window;
        Console console = this.console;
        if (Util.joystickReset(GLFW_JOYSTICK_1)) {
            this.director.ShowMenu();
        }
        if (Util.joystickReset(GLFW_JOYSTICK_2)) {
            this.director.ShowMenu();
        }
        if (Util.readKey(window, GLFW_KEY_ESCAPE)) {
            this.director.ShowMenu();
        }
        updateControllers(window, console);
        console.stepSeconds(dt);
        glBindTexture(GL_TEXTURE_2D, this.texture);
        Util.setTexture(console.buffer());
        drawBuffer(this.director.window);
        glBindTexture(GL_TEXTURE_2D, 0);
        if(this.record){
            int n = this.frames.length;
            this.frames = Arrays.copyOf(this.frames, n + 1);
            this.frames[n+1] = Util.copyImage(console.buffer());
        }
    }


    public GLFWKeyCallback onKey() {
        return new GLFWKeyCallback() {
            @Override
            public void invoke(@NativeType("GLFWwindow *") long window, int key, int scancode, int action, int mods) {
                if(action == GLFW_PRESS){
                    switch(key) {
                        case GLFW_KEY_SPACE:
//                    screenshot(this.console.buffer());
                        case GLFW_KEY_R:
                            console.reset();
                        case GLFW_KEY_TAB:
                            if (record){
                                record = false;
//                        animation(this.frames);
//                        this.frames = nil;
                            } else {
                                record = true;
                            }
                    }
                }
            }
        };

    }

    public void drawBuffer(com.nes.ui.Window window) {
        int w = window.GetFramebufferWidth();
        int h = window.GetFramebufferHeight();
        float s1 = w / 256;
        float s2 = h/ 240;
        float f = 1 - padding;
        float x, y;
        if(s1 >= s2){
            x = f * s2 / s1;
            y = f;
        } else {
            x = f;
            y = f * s1 / s2;
        }
        glBegin(GL_QUADS);
        glTexCoord2f(0, 1);
        glVertex2f(-x, -y);
        glTexCoord2f(1, 1);
        glVertex2f(x, 0-y);
        glTexCoord2f(1, 0);
        glVertex2f(x, y);
        glTexCoord2f(0, 0);
        glVertex2f(-x, y);
        glEnd();
    }

    public void updateControllers(com.nes.ui.Window window, Console console) {
        boolean turbo = console.PPU.Frame%6 < 3;
        boolean[] k1 = Util.readKeys(window, turbo);
        boolean[] j1 = Util.readJoystick(GLFW_JOYSTICK_1, turbo);
        boolean[] j2 = Util.readJoystick(GLFW_JOYSTICK_2, turbo);
        console.setButtons1(Util.combineButtons(k1, j1));
        console.setButtons2(j2);
    }

}
