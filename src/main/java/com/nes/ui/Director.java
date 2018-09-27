package com.nes.ui;


import com.nes.core.Console;
import com.nes.ui.view.GameView;
import com.nes.ui.view.MenuView;
import com.nes.ui.view.View;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.opengl.GL11.glClear;

/**
 * Created by RoyChan on 2018/3/14.
 */
public class Director{
    public Window window;
    public Audio audio;
    public View view;
    public View menuView;
    public float timestamp;


    public Director(Window window, Audio audio){
        this.window = window;
        this.audio = audio;
    }


    public void SetTitle(String title){
        this.window.SetTitle(title);
    }

    public void SetView(View view){
        if(this.view != null){
            this.view.Exit();
        }
        this.view = view;
        if(this.view != null){
            this.view.Enter();
        }
        this.timestamp = (float) glfwGetTime();
    }

    public void Step(){
        glClear(GL11.GL_COLOR_BUFFER_BIT);
        float timestamp1 = (float) glfwGetTime();
        float dt = timestamp1 - this.timestamp;
        this.timestamp = timestamp;
        if(this.view != null){
            this.view.Update(timestamp, dt);
        }
    }

    public void Start(String[] paths){
        this.menuView = new MenuView(this, paths);
        if(paths.length == 1){
            this.PlayGame(paths[0]);
        } else{
            this.ShowMenu();
        }
        this.Run();
    }

    public void Run(){
        while (!this.window.ShouldClose()){
            this.Step();
            this.window.SwapBuffers();
            glfwPollEvents();
        }
        this.SetView(null);
    }

    public void PlayGame(String path){
        String hash = Util.hashFile(path);
        Console console = new Console(path);
        this.SetView(new GameView(this, console, path, hash));
    }

    public void ShowMenu(){
        this.SetView(this.menuView);
    }

}
