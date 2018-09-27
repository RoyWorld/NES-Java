package com.nes.ui;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWCharCallbackI;
import org.lwjgl.glfw.GLFWKeyCallbackI;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Created by RoyChan on 2018/3/15.
 */
public class Window {

    long window;

    String title;

    IntBuffer framebufferWidth;
    IntBuffer framebufferHeight;


    public void SetTitle(String title){
        this.title = title;
    }

    public boolean ShouldClose(){
        return glfwWindowShouldClose(window);
    }

    public void SwapBuffers(){
        glfwSwapBuffers(window);
    }

    public void SetKeyCallback(GLFWKeyCallbackI keyCallback){
        glfwSetKeyCallback(window, keyCallback);
    }

    public int GetFramebufferWidth(){
        framebufferWidth = BufferUtils.createIntBuffer(1);
        framebufferHeight = BufferUtils.createIntBuffer(1);
        glfwGetFramebufferSize(window, framebufferWidth, framebufferHeight);
        return framebufferWidth.get();
    }

    public int GetFramebufferHeight(){
        framebufferWidth = BufferUtils.createIntBuffer(1);
        framebufferHeight = BufferUtils.createIntBuffer(1);
        glfwGetFramebufferSize(window, framebufferWidth, framebufferHeight);
        return framebufferWidth.get();
    }

    public int GetKey(int key){
        return glfwGetKey(window, key);
    }

    public void SetCharCallback(GLFWCharCallbackI glfwCharCallbackI){
        glfwSetCharCallback(window, glfwCharCallbackI);
    }
}
