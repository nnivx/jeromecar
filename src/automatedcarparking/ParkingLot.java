/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package automatedcarparking;

import automatedcarparking.graphics.Text;
import automatedcarparking.graphics.Util;
import automatedcarparking.misc.Container;
import org.lwjgl.opengl.GL11;

/**
 *
 * @author nikki
 */
class ParkingLot {
    
    private final float x, y, angleDeg;
    private final int width, height;
    private final Engine e;
    private final int id;
    private boolean hovered, selected, pressed, visible;

    ParkingLot(Engine e, int id, float x, float y, float angle, int width, int height) {
        this.id = id;
        this.e = e;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        angleDeg = (float)Math.toDegrees(angle);
    }
    
    public boolean isVisible() {
        return visible;
    }
    
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    
    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    
    private boolean hovered(double mx, double my) {
        // inverse rotate x, y
        float dist = MathUtil.distance(x, y, (float)mx, (float)my);
        float ang = -(float)Math.toRadians(angleDeg);
        float x2 = MathUtil.offsetX(x, dist, ang);
        float y2 = MathUtil.offsetY(y, dist, ang);
        float x1 = x-width/2;
        float y1 = y-height/2;
        return MathUtil.in(x1, x1+width, x2) && MathUtil.in(y1, y1+height, y2);
    }
    
    public void mouseButtonPressed(Container c, int button, int mods) {
        if (hovered) {
            pressed = true;
        }
    }
    
    public void mouseButtonReleased(Container c, int button, int mods) {
        pressed = false;
        if (hovered) {
            selected = !selected;
            e.selectPath(id);
        }
    }
    
    public void mouseMoved(Container c, double x, double y) {
        hovered = hovered(x, y);
    }
    
    private void drawQuad(float scale, float border) {
        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 0);
        GL11.glRotatef(angleDeg-90, 0, 0, 1);
        GL11.glScalef(scale, scale, 1);
        GL11.glTranslatef(-(width+border)/2, -(height+border-Engine.WIDGET_SCALE*75)/2, 0);
        
        Util.drawQuad(0, 0, width+border, height+border);
        
        GL11.glPopMatrix();
    }
    
    public void render() {
        if (!visible) return;
        float r = 0.3f, g = 0.3f, b = 0.3f;
        float br = 0.6f, bg = 0.6f, bb = 0.6f;
        float scale = 1.0f;
        // hover: highlight
        if (hovered) {
            r = 0.8f;
            g = 0.8f;
            b = 0.9f;
        }
        // selected: 
        if (selected) {
            r = 0.75f;
            g = 0.75f;
            b = 0.95f;
        }
        // press: brighter (overrides selected)
        if (pressed) {
            r = 0.9f;
            g = 0.9f;
            b = 1.f;
            scale = 1.1f;
        }
        // border color
        if (pressed || selected || hovered) {
            br -= 0.2f;
            bg -= 0.2f;
            bb -= 0.2f;
        }
        
        Util.pushColor(br, bg, bb, 1);
        drawQuad(scale, scale*5f);
        
        Util.pushColor(r, g, b, 1);
        drawQuad(scale, 0f);
        
        Util.popColor();
        Util.popColor();
    }
    
}
