/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package automatedcarparking.graphics;

import java.nio.FloatBuffer;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

/**
 *
 * @author nikki
 */
public class Text extends Transformable {
    
    private BMFont font;
    private final BMFont.Glyph glyph = new BMFont.Glyph();
    private String text;
    private float spacingH, spacingV;
    private boolean empty;
    
    private final FloatBuffer buf = BufferUtils.createFloatBuffer(16);
   
    public Text(BMFont font, float spacingH) {
        this(font, spacingH, 2);
    }
    
    public Text(BMFont font, float spacingH, float spacingV) {
        this.font = font;
        this.spacingH = spacingH;
        this.spacingV = spacingV;
        text = "";
        empty = true;
    }
    
    public void setText(CharSequence text) {
        // invoke equals of charsequence
        if (!text.equals(this.text)) {
            this.text = text.toString();
            updateText(this.text);
        }
    }
    
    public void render() {
        if (empty) return;
        font.bind();
        
        float x = 0, y = 0;
        matrix().get(buf);
        
        GL11.glPushMatrix();
        GL11.glMultMatrixf(buf);
        GL11.glBegin(GL11.GL_QUADS);
        for (int i= 0; i < text.length(); ++i) {
            char c = text.charAt(i);
            if (c == '\n') {
                font.getCharGlyph(' ', glyph);
                y += glyph.charHeight + spacingV;
                x = 0;
                continue;
            }
            font.getCharGlyph(c, glyph);
            
            GL11.glTexCoord2f(glyph.left, glyph.top);
            GL11.glVertex2f(x, y);
            
            GL11.glTexCoord2f(glyph.right, glyph.top);
            GL11.glVertex2f(x+glyph.charWidth, y);
            
            GL11.glTexCoord2f(glyph.right, glyph.bottom);
            GL11.glVertex2f(x+glyph.charWidth, y+glyph.charHeight);
            
            GL11.glTexCoord2f(glyph.left, glyph.bottom);
            GL11.glVertex2f(x, y+glyph.charHeight);
            
            x += glyph.charWidth + spacingH;
        }
        GL11.glEnd();
        GL11.glPopMatrix();
        Texture.bind(null);
    }
    
    public void dispose() {
        font.dispose();
    }
    
    private void updateText(String newText) {
        empty = newText.trim().equals("");
    }

    public float getHorizontalSpacing() {
        return spacingH;
    }

    public void setHorizontalSpacing(float spacing) {
        this.spacingH = spacing;
    }
    
    public float getVerticalSpacing() {
        return spacingV;
    }

    public void setVerticalSpacing(float spacing) {
        this.spacingV = spacing;
    }
    
}
