/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package automatedcarparking.graphics;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

/**
 * TrueType font. Handles ASCII characters only.
 * @author nikki
 */
public class MonoBitmapFont {
    
    public static MonoBitmapFont create(String path, int charWidth, int charHeight) throws IOException {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer comp = stack.mallocInt(1);
            // !! heap memory, stack may run out
            ByteBuffer imageBuffer = Util.resourceToByteBuffer(BufferUtils::createByteBuffer, path, 1024*8);
            
            if (STBImage.stbi_info_from_memory(imageBuffer, w, h, comp) == 0) {
                throw new IOException("Failed to read image information: " + STBImage.stbi_failure_reason());
            }
            // force 4 component per pixel
            ByteBuffer image = STBImage.stbi_load_from_memory(imageBuffer, w, h, comp, 4);
            if (image == null) {
                throw new IOException("Failed to load image: " + STBImage.stbi_failure_reason());
            }
            return create(image, w.get(0), h.get(0), charWidth, charHeight);
        }
    }
    
    public static MonoBitmapFont create(ByteBuffer buf, int width, int height, int charWidth, int charHeight) {
        return new MonoBitmapFont(Texture.createTexture(width, height, buf), charWidth, charHeight); 
    }
    
    final int charWidth, charHeight, scansize;
    private final Texture texture;
    
    private MonoBitmapFont(Texture texture, int charWidth, int charHeight) {
        this.texture = texture;
        this.charWidth = charWidth;
        this.charHeight = charHeight;
        scansize = texture.getWidth()/charWidth;
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
