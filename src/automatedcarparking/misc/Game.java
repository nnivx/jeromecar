/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package automatedcarparking.misc;

/**
 *
 * @author nikki
 */
public interface Game {
    
    public boolean init(Container c, String[] args);
    public void update(Container c, float dt);
    public void render(Container c);
    public void cleanup(Container c);
    
    public void keyPressed(Container c, int key, int code, int mods);
    public void keyReleased(Container c, int key, int code, int mods);
    public void mouseButtonPressed(Container c, int button, int mods);
    public void mouseButtonReleased(Container c, int button, int mods);
    public void mouseMoved(Container c, double x, double y);
    
}
