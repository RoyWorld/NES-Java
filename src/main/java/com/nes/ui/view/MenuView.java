package com.nes.ui.view;

import com.nes.ui.Director;
import com.nes.ui.Texture;
import com.nes.ui.Window;
import com.nes.util.Constant;
import org.lwjgl.glfw.GLFWCharCallback;
import org.lwjgl.glfw.GLFWCharCallbackI;
import org.lwjgl.system.NativeType;

import static com.nes.ui.Util.combineButtons;
import static com.nes.ui.Util.readJoystick;
import static com.nes.ui.Util.readKeys;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * Created by RoyChan on 2018/3/15.
 */
public class MenuView implements View {

    int border       = 10;
    int margin       = 10;
    double initialDelay = 0.3;
    double repeatDelay  = 0.1;
    double typeDelay    = 0.5;
    
    Director director;
    String[] paths;
    Texture texture;
    int nx, ny, i, j;
    int scroll;
    float t;
    boolean[] buttons;
    float[] times;
    String typeBuffer;
    float typeTime;

    public MenuView(Director director, String[] paths) {
        this.director = director;
        this.paths = paths;
        this.texture = new Texture();
    }


    public void checkButtons(){
        Window window = this.director.window;
        boolean[] k1 = readKeys(window, false);
        boolean[] j1 = readJoystick(GLFW_JOYSTICK_1, false);
        boolean[] j2 = readJoystick(GLFW_JOYSTICK_2, false);
        boolean[] buttons1 = combineButtons(combineButtons(j1, j2), k1);
        double now = glfwGetTime();
        for (int k = 0; k < buttons1.length; k++) {
            if(buttons1[i] && !this.buttons[i]){
                this.times[i] = (float) (now + initialDelay);
                this.onPress(i);
            } else if(!buttons1[i] && this.buttons[i]){
                this.onRelease(i);
            } else if(buttons1[i] && now >= this.times[i]){
                this.times[i] = (float) (now + repeatDelay);
                this.onPress(i);
            }
        }
        this.buttons = buttons1;
    }

    public void onPress(int index) {
        switch (index){
            case Constant.ButtonUp:
                this.j--;
                this.t = (float) glfwGetTime();
            case Constant.ButtonDown:
                this.j++;
                this.t = (float) glfwGetTime();
            case Constant.ButtonLeft:
                this.i--;
                this.t = (float) glfwGetTime();
            case Constant.ButtonRight:
                this.i++;
                this.t = (float) glfwGetTime();
            default:
                return;
        }
    }

    public void onRelease(int index) {
        switch (index){
            case Constant.ButtonStart:
                this.onSelect();
        }
    }

    public void onSelect() {
        int index = this.nx*(this.j+this.scroll) + this.i;
        if(index >= this.paths.length){
            return;
        }
        this.director.PlayGame(this.paths[index]);
    }

    public GLFWCharCallback onChar() {
        return new GLFWCharCallback() {
            @Override
            public void invoke(@NativeType("GLFWwindow *") long window, @NativeType("unsigned int") int codepoint) {
                double now = glfwGetTime();
                if(now > typeTime){
                    typeBuffer = "";
                }
                typeTime = (float) (now + typeDelay);
                char someChar = (char)Integer.parseInt(String.valueOf(codepoint));
                typeBuffer = new String(typeBuffer + someChar);
                typeBuffer = typeBuffer.toLowerCase();
                for(int index = 0; index < paths.length; index++){
                    String[] p1 = paths[index].toLowerCase().split("\\\\");
                    if(p1[p1.length-1].compareTo(typeBuffer) >= 0){
                        highlight(index);
                        return;
                    }
                }
            }
        };

    }

    public void highlight(int index) {
        this.scroll = index/this.nx - (this.ny-1)/2;
        this.clampScroll(false);
        this.i = index % this.nx;
        this.j = (index-this.i)/this.nx - this.scroll;
    }

    public void Enter() {
        glClearColor((float)0.333, (float)0.333, (float)0.333, 1);
        this.director.SetTitle("Select Game");
        this.director.window.SetCharCallback(this.onChar());
    }

    public void Exit() {
        this.director.window.SetCharCallback(null);
    }

    public void Update(float t, float dt) {
        this.checkButtons();
        this.texture.Purge();
        Window window = this.director.window;
        int w = window.GetFramebufferWidth();
        int h = window.GetFramebufferHeight();
        int sx = 256 + margin*2;
        int sy = 240 + margin*2;
        int nx = (w - border*2) / sx;
        int ny = (h - border*2) / sy;
        int ox = (w-nx*sx)/2 + margin;
        int oy = (h-ny*sy)/2 + margin;
        if(nx < 1){
            nx = 1;
        }
        if(ny < 1){
            ny = 1;
        }
        this.nx = nx;
        this.ny = ny;
        this.clampSelection();
        glPushMatrix();
        glOrtho(0, w, h, 0, -1, 1);
        this.texture.Bind();
        for(int j = 0; j < ny; j++){
            for(int i = 0; i < nx; i++){
                float x = ox + i*sx;
                float y = oy + j*sy;
                int index = nx*(j+this.scroll) + i;
                if(index >= this.paths.length){
                    continue;
                }
                String path = this.paths[index];
                float[] floats = this.texture.Lookup(path);
                drawThumbnail(x, y, floats[0], floats[1], floats[2], floats[3]);
            }
        }
        this.texture.Unbind();
        if(((t-this.t)*4)%2 == 0){
            float x = (ox + this.i*sx);
            float y = (oy + this.j*sy);
            drawSelection(x, y, 8, 4);
        }
        glPopMatrix();
    }

    public void clampSelection() {
        if(this.i < 0){
            this.i = this.nx - 1;
        }
        if(this.i >= this.nx){
            this.i = 0;
        }
        if(this.j < 0){
            this.j = 0;
            this.scroll--;
        }
        if(this.j >= this.ny){
            this.j = this.ny - 1;
            this.scroll++;
        }
        this.clampScroll(true);
    }

    public void clampScroll(boolean wrap) {
        int n = this.paths.length;
        int rows = n / this.nx;
        if(n%this.nx > 0){
            rows++;
        }
        int maxScroll = rows - this.ny;
        if(this.scroll < 0){
            if(wrap){
                this.scroll = maxScroll;
                this.j = this.ny - 1;
            } else {
                this.scroll = 0;
                this.j = 0;
            }
        }
        if(this.scroll > maxScroll){
            if(wrap){
                this.scroll = 0;
                this.j = 0;
            } else {
                this.scroll = maxScroll;
                this.j = this.ny - 1;
            }
        }
    }

    public void drawThumbnail(float x, float y, float tx, float ty, float tw, float th) {
        float sx = x + 4;
        float sy = y + 4;
        glDisable(GL_TEXTURE_2D);
        glColor3f((float) 0.2,(float) 0.2,(float) 0.2);
        glBegin(GL_QUADS);
        glVertex2f(sx, sy);
        glVertex2f(sx+256, sy);
        glVertex2f(sx+256, sy+240);
        glVertex2f(sx, sy+240);
        glEnd();
        glEnable(GL_TEXTURE_2D);
        glColor3f(1, 1, 1);
        glBegin(GL_QUADS);
        glTexCoord2f(tx, ty);
        glVertex2f(x, y);
        glTexCoord2f(tx+tw, ty);
        glVertex2f(x+256, y);
        glTexCoord2f(tx+tw, ty+th);
        glVertex2f(x+256, y+240);
        glTexCoord2f(tx, ty+th);
        glVertex2f(x, y+240);
        glEnd();
    }

    public void drawSelection(float x, float y, float p, float w) {
        glLineWidth(w);
        glBegin(GL_LINE_STRIP);
        glVertex2f(x-p, y-p);
        glVertex2f(x+256+p, y-p);
        glVertex2f(x+256+p, y+240+p);
        glVertex2f(x-p, y+240+p);
        glVertex2f(x-p, y-p);
        glEnd();
    }

}
