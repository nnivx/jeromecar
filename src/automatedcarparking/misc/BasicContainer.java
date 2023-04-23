/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package automatedcarparking.misc;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.glfw.GLFW;

/**
 * Implements basic functionalities.
 * 
 * @author nikki
 */
public class BasicContainer extends GLFWContainer {
    
    protected PrintStream err = System.err;
    private String phase;

    private final Game game;
    
    public BasicContainer(Game game) {
        this.game = game;
    }
    
    @Override
    public void run(String[] args) {
        try {
            init(args);
            loop();
        } catch (Exception ex) {
            err.println("Error in " + phase + ":");
            ex.printStackTrace(err);
        } finally {
            try {
                cleanup();
            } catch (Exception clex) {
                // no-op
            }
            GLFW.glfwTerminate();
        }
    }
    
    private void init(String[] args) {
        phase = "init";
       
        init(800, 600, 4, 2, 0);
        if (!game.init(this, args)) {
            throw new RuntimeException("Failed to initialize game");
        }
    }
    
    private void loop() {
        phase = "game loop";
        long last = System.nanoTime();
        while (!GLFW.glfwWindowShouldClose(getWindowHandle())) {
            GLFW.glfwPollEvents();
            long now = System.nanoTime();
            float dt = (now - last)/1e9f;
            last = now;
            game.update(this, dt);
            game.render(this);
            GLFW.glfwSwapBuffers(getWindowHandle());
        }
    }
    
    private void cleanup() {
        phase = "cleanup";
        game.cleanup(this);
    }
    
    @Override
    protected void keyPressed(int key, int code, int mods) {
        game.keyPressed(this, key, code, mods);
    }

    @Override
    protected void keyReleased(int key, int code, int mods) {
        game.keyReleased(this, key, code, mods);
    }

    @Override
    protected void mouseButtonPressed(int button, int mods) {
        game.mouseButtonPressed(this, button, mods);
    }

    @Override
    protected void mouseButtonReleased(int button, int mods) {
        game.mouseButtonReleased(this, button, mods);
    }

    @Override
    protected void mouseMoved(double x, double y) {
        game.mouseMoved(this, x, y);
    }
    
    @Override
    protected void error(int err, String desc) {
        this.err.println(desc);
    }

}
