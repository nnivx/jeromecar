/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package automatedcarparking;

import static automatedcarparking.Engine.TOOLBOX_SPEED;
import automatedcarparking.graphics.Text;
import automatedcarparking.graphics.Util;
import automatedcarparking.misc.Container;
import java.io.IOException;
import org.joml.Vector2f;
import org.lwjgl.opengl.GL11;

/**
 *
 * @author nikki
 */
class Toolbox {
    
    // Animation
    private float x = 800;
    private boolean animatingToolbox = false;
    private boolean toolboxIn = false;
    private float toolboxTimer = 0;
    
    // UI
    private static final float ALIGN = 200*0.20f;
    private static final float CHECKBOX_HEIGHT = 30;
    
    private final Checkbox[] checkboxes;
    private final Text header, instr;
    
    // [hands off]
    private final Engine e;
   
    public Toolbox(Engine e) throws IOException {
        this.e = e;
        
        checkboxes = new Checkbox[] {
            new Checkbox("Car Heading"),
            new Checkbox("Car Target"),
            new Checkbox("Car Path"),
            new Checkbox("Car Info"),
            new Checkbox("Paths"),
            new Checkbox("Points"),
            new Checkbox("Point Area"),
            new Checkbox("Debug Info")
        };
        for (int i = 0; i < 8; ++i) {
            checkboxes[i].boxY = (CHECKBOX_HEIGHT)*i + 50;
            checkboxes[i].checked = (e.displayFlags & (1<<i)) != 0;
        }
        
//        shader = Shader.load("automatedcarparking/toolbox_vert.glsl", "automatedcarparking/toolbox_frag.glsl");
        header = new Text(e.font, -12);
        header.setText("DISPLAY");
        header.setScale(0.7f);
        instr = new Text(e.font, -12, 5);
        instr.setText(
                "[r] Reset!\n" +
                "[~] Toggle pause\n" +
                "[1] 1x speed\n" +
                "[2] 2x speed\n" +
                "[3] 3x speed\n" +
                "[enter] Confirm");
        instr.setScale(0.45f);
    }
    
    private class Checkbox {
        float boxY;
        final Text text;
        boolean checked;
        
        Checkbox(String text) {
            this.text = new Text(e.font, -12);
            this.text.setText(text);
            this.text.setScale(0.45f);
        }
        
        boolean hovered(Container c) {
            Vector2f mpos = c.getMousePos();
            return (mpos.x > x) && (mpos.x < 800) && (mpos.y > boxY)
                    && (mpos.y < boxY+CHECKBOX_HEIGHT);
        }
        
        void render(Container c) {
            if (hovered(c)) {
                Util.pushColor(0.9f, 0.9f, 1.0f, 0.3f);
                Util.drawQuad(x, boxY, 200, CHECKBOX_HEIGHT);
                Util.popColor();
            }
            if (checked) {
                Util.pushColor(0.8f, 0.8f, 0.8f, 1);
                GL11.glBegin(GL11.GL_QUADS);
                GL11.glVertex2f(x+3, boxY+3);
                GL11.glVertex2f(x-3+CHECKBOX_HEIGHT, boxY+3);
                GL11.glVertex2f(x-3+CHECKBOX_HEIGHT, boxY+CHECKBOX_HEIGHT-3);
                GL11.glVertex2f(x+3, boxY+CHECKBOX_HEIGHT-3);
                GL11.glEnd();
                Util.popColor();
            }
            text.render();
        }
        
    }

    public void keyReleased(Container c, int key, int code, int mods) {
    }

    public void mouseButtonPressed(Container c, int button, int mods) {
    }

    public void mouseButtonReleased(Container c, int button, int mods) {
        // for all check box; if hovered, toggle
        for (int i = 0; i < 8; ++i) {
            if (checkboxes[i].hovered(c)) {
                checkboxes[i].checked = !checkboxes[i].checked;
                e.displayFlags ^= (1 << i);
            }
        }
    }
    
    public void mouseMoved(Container c, double x, double y) {
        if (e.dragCar || e.rotateCar) return;
            
        // check if mouse is in toolbox area
        final float minx =  toolboxIn? 600: (800-200*0.20f);
        boolean captured = x > minx && x < 800 && y > 0 && y < 600;
        
        // if it is captured, then check if we're updated already
        if (captured != toolboxIn) {
            // update necessary parameters for animation
            animatingToolbox = true;
            toolboxTimer = TOOLBOX_SPEED - toolboxTimer;
            toolboxIn = captured;
        }
    }
    
    public void update(Container c, float dt) {
        if (animatingToolbox) {
            // animate away~
            toolboxTimer -= dt;
            if (toolboxTimer <= 0) {
                toolboxTimer = 0;
                animatingToolbox = false;
            }

            // normalize time
            final float t = (TOOLBOX_SPEED - toolboxTimer)/TOOLBOX_SPEED;

            // cubic interp for entry, quadratic for exit
            final float factor = toolboxIn? t*t*t: (1-t)*(1-t);
            x = 800 + -200*factor;
        }
        for (int i = 0; i < 8; ++i) {
            checkboxes[i].text.setPos(CHECKBOX_HEIGHT + x + 5, checkboxes[i].boxY + 6);
        }
        header.setPos(x + CHECKBOX_HEIGHT/4, 20);
        instr.setPos(x + CHECKBOX_HEIGHT, checkboxes[7].boxY + CHECKBOX_HEIGHT*2);
    }
    
    public void render(Container c) {
        Util.pushColor(.24f, .24f, .24f, 1f);
        Util.drawQuad(x, 0, 200, 600);
        Util.popColor();
        
        Util.pushColor(0.3f, 0.3f, 0.3f, 1f);
        Util.drawQuad(x, 50, 200, CHECKBOX_HEIGHT*8);
        Util.drawQuad(x, 50+CHECKBOX_HEIGHT*8+25, 200, 130);
        Util.popColor();
            
        header.render();
        instr.render();
        for (Checkbox b: checkboxes) {
            b.render(c);
        }
    }
    
}
