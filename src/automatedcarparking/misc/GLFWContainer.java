/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package automatedcarparking.misc;

import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

/**
 * Simple GLFW container supporting single window.
 * 
 * @author nikki
 */
public abstract class GLFWContainer implements Container {
    
    private long window;
    private GLCapabilities caps;
    
    public final long getWindowHandle() {
        return window;
    }
    
    protected abstract void keyPressed(int key, int code, int mods);
    protected abstract void keyReleased(int key, int code, int mods);
    protected abstract void mouseButtonPressed(int button, int mods);
    protected abstract void mouseButtonReleased(int button, int mods);
    protected abstract void mouseMoved(double x, double y);
    protected abstract void error(int err, String desc);
    
    protected void init(int width, int height, int samples, int major, int minor) {
        if (width < 1) throw new IllegalArgumentException("width <= 0");
        if (height < 1) throw new IllegalArgumentException("height <= 0");
        
        // additional thunk
        GLFW.glfwSetErrorCallback((err, desc)->error(err, MemoryUtil.memASCII(desc)));
        
        // initialize glfw and window
        if (!GLFW.glfwInit()) {
            throw new RuntimeException("Failed to initialize GLFW");
        }
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, major);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, minor);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_SAMPLES, 4);
        
        if (samples > 0)
            GLFW.glfwWindowHint(GLFW.GLFW_SAMPLES, samples);
        
        // create window
        window = GLFW.glfwCreateWindow(width, height, "GLFW Window", 0L, 0L);
        if (window == 0) {
            throw new RuntimeException("Failed to create window");
        }
        
        // set callbacks
        GLFW.glfwSetKeyCallback(window, (w, k, s, a, m)-> {
            if (a == GLFW.GLFW_PRESS) {
                keyPressed(k, s, m);
            } else {
                keyReleased(k, s, m);
            }
        });
        GLFW.glfwSetCursorPosCallback(window, (w, x, y)-> {
            mouseMoved(x, y);
        });
        GLFW.glfwSetMouseButtonCallback(window, (w, b, a, m)-> {
            if (a == GLFW.GLFW_PRESS) {
                mouseButtonPressed(b, m);
            } else {
                mouseButtonReleased(b, m);
            }
        });
        
        // finishing touches
        GLFW.glfwMakeContextCurrent(window);
        caps = GL.createCapabilities();
        GLFW.glfwSwapInterval(1);
        GLFW.glfwShowWindow(window);
    }
    
    @Override
    public Vector2f getMousePos() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            final DoubleBuffer ptr = stack.mallocDouble(2);
            final long addr = MemoryUtil.memAddress(ptr);
            GLFW.nglfwGetCursorPos(window, addr, addr + Double.BYTES);
            return new Vector2f((float)ptr.get(0), (float)ptr.get(1));
        }
    }

    @Override
    public Vector2f getWindowPos() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            final IntBuffer ptr = stack.mallocInt(2);
            final long addr = MemoryUtil.memAddress(ptr);
            GLFW.nglfwGetWindowSize(window, addr, addr + Integer.BYTES);
            return new Vector2f(ptr.get(0), ptr.get(1));
        }
    }

    @Override
    public Vector2f getWindowSize() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            final IntBuffer ptr = stack.mallocInt(2);
            final long addr = MemoryUtil.memAddress(ptr);
            GLFW.nglfwGetWindowSize(window, addr, addr + Integer.BYTES);
            return new Vector2f(ptr.get(0), ptr.get(1));
        }
    }
    
    @Override
    public void setMousePos(float x, float y) {
        GLFW.glfwSetCursorPos(window, x, y);
    }

    @Override
    public void setWindowPos(int x, int y) {
        GLFW.glfwSetWindowPos(window, x, y);
    }

    @Override
    public void setWindowSize(int width, int height) {
        GLFW.glfwSetWindowSize(window, width, height);
    }

    @Override
    public void setWindowTitle(String title) {
        GLFW.glfwSetWindowTitle(window, title);
    }

    @Override
    public boolean isMouseButtonPressed(int button) {
        return GLFW.glfwGetMouseButton(window, button) == GLFW.GLFW_PRESS;
    }

    @Override
    public boolean isKeyPressed(int key) {
        final int state = GLFW.glfwGetKey(window, key);
        return state == GLFW.GLFW_PRESS || state == GLFW.GLFW_REPEAT;
    }

    @Override
    public void stop() {
        GLFW.glfwSetWindowShouldClose(window, true);
    }
    
}
