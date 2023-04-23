/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package automatedcarparking.misc;

import org.joml.Vector2f;


/**
 *
 * @author nikki
 */
public interface Container {
    
    public Vector2f getMousePos();
    public Vector2f getWindowPos();
    public Vector2f getWindowSize();
    
    public void setMousePos(float x, float y);
    public void setWindowPos(int x, int y);
    public void setWindowSize(int width, int height);
    public void setWindowTitle(String title);
    
    public boolean isMouseButtonPressed(int button);
    public boolean isKeyPressed(int key);
    
    public void run(String[] args);
    public void stop();
    
}
