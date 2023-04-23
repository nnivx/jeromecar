/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package automatedcarparking.graphics;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Loads noix bitmap font.
 * 
 * @author nikki
 */
public class BMFont {
    
    public static class Glyph {
        public int charWidth, charHeight;
        public float top, left, bottom, right;
    }
    
    private final Glyph[] glyphs;
    private final Texture texture;
    
    public static BMFont load(String path) throws IOException {
        String pngPath = path+"/font-image.png";
        String txtPath = path+"/font-info.txt";
        Glyph[] glf = new Glyph[127-32 + 1];
        Texture tex = new Texture();
        tex.load(pngPath);
        try (BufferedReader reader = new BufferedReader(new FileReader(Util.findResource(txtPath)))) {
            for (int i = 0; ; ++i) {
                String line = reader.readLine();
                if (line == null) break;
                
                // code:left:top:width:height
                String[] vals = line.split(":");
                int code = Integer.parseInt(vals[0]);
                if (code < 32 || code > 127) continue;
                
                // assign
                Glyph g  = new Glyph();
                float tw = 1.0f/tex.getWidth();
                float th = 1.0f/tex.getHeight();
                int left = Integer.parseInt(vals[1]);
                int top  = Integer.parseInt(vals[2]);
                g.charWidth  = Integer.parseInt(vals[3]);
                g.charHeight = Integer.parseInt(vals[4]);
                g.left   = left*tw;
                g.top    = top*th;
                g.right  = (left + g.charWidth)*tw;
                g.bottom = (top + g.charHeight)*th;
                glf[code-32] = g;
            }
        }
        return new BMFont(tex, glf);
    }
    
    private BMFont(Texture tex, Glyph[] gs) {
        texture = tex;
        glyphs = gs;
    }
    
    public Glyph getCharGlyph(char c, Glyph dest) {
        if (c < 32 || c > 127) c = 32;
        final Glyph g = glyphs[c - 32];
        dest.left   = g.left;
        dest.top    = g.top;
        dest.right  = g.right;
        dest.bottom = g.bottom;
        dest.charWidth = g.charWidth;
        dest.charHeight = g.charHeight;
        return dest;
    }
    
    public void bind() {
        texture.bind();
    }
   
    public int getTextureWidth() {
        return texture.getWidth();
    }
    
    public int getTextureHeight() {
        return texture.getHeight();
    }
    
    public void dispose() {
        texture.dispose();
    }
    
}
