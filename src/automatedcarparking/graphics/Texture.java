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
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

/**
 * 2D Texture.
 * 
 * @author nikki
 */
public class Texture {
    
    /**
     * Returns maximum texture size.
     * @return maximum texture size
     */
    public static int getMaximumSize() {
        return GL11.glGetInteger(GL11.GL_MAX_TEXTURE_SIZE);
    }
    
    /**
     * Binds a texture.
     * @param texture texture to bind
     */
    public static void bind(Texture texture) {
        if (texture == null) {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        } else {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.id);
        }
    }

    /**
     * Create a texture.
     * @param width
     * @param height
     * @param buffer
     * @return 
     */
    public static Texture createTexture(int width, int height, ByteBuffer buffer) {
        Texture t = new Texture();
        t.create(width, height, buffer);
        return t;
    }
    
    private int id;
    private int width, height;
    private int actualWidth, actualHeight;
  
    /**
     * Create the texture.
     * @param width texture width
     * @param height texture height
     * @param buffer RGBA image
     */
    public void create(int width, int height, ByteBuffer buffer) {
        create(width, height, buffer, GL11.GL_LINEAR, GL11.GL_LINEAR, GL11.GL_REPEAT, GL11.GL_REPEAT);
    }
    
    /**
     * Create an empty texture.
     * @param width texture width
     * @param height texture height
     * @param buffer RGBA image
     * @param minFilter texture min filter
     * @param magFilter texture mag filter
     * @param wrapS texture horizontal wrap
     * @param wrapT texture vertical wrap
     */
    public void create(int width, int height, ByteBuffer buffer, int minFilter, int magFilter, int wrapS, int wrapT) {
        if (width < 1 || height < 1) 
            throw new IllegalArgumentException("texture width or height < 1");
        
        final int szX = getValidSize(width);
        final int szY = getValidSize(height);
        final int maxSize = getMaximumSize();

        if (szX > maxSize || szY > maxSize)
            throw new java.lang.RuntimeException("texture too large");

        this.width = width;
        this.height = height;
        this.actualWidth = szX;
        this.actualHeight = szY;

        if (id == 0) {
            id = GL11.glGenTextures();
        }

        int binding = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, wrapS);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, wrapT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, minFilter);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, magFilter);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL14.GL_GENERATE_MIPMAP, GL11.GL_TRUE);
        if (buffer != null)
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, binding);
    }
    
    /**
     * Load a texture from file.
     * @param source location
     * @throws IOException 
     */
    public void load(String source) throws IOException {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer comp = stack.mallocInt(1);
            // !! heap memory, stack may run out
            ByteBuffer imageBuffer = Util.resourceToByteBuffer(BufferUtils::createByteBuffer, source, 1024*8);
            
            if (STBImage.stbi_info_from_memory(imageBuffer, w, h, comp) == 0) {
                throw new IOException("Failed to read image information: " + STBImage.stbi_failure_reason());
            }
            // force 4 component per pixel
            ByteBuffer image = STBImage.stbi_load_from_memory(imageBuffer, w, h, comp, 4);
            if (image == null) {
                throw new IOException("Failed to load image: " + STBImage.stbi_failure_reason());
            }
            create(w.get(0), h.get(0), image);
        }
    }
    
    /**
     * Set texture filter.
     * @param minFilter min filter
     * @param magFilter mag filter
     */
    public void setFilter(int minFilter, int magFilter) {
        if (id == 0) return;
        int binding = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, magFilter);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, minFilter);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, binding);
    }
    
    /**
     * Set texture wrapping.
     * @param wrapS horizontal wrap
     * @param wrapT vertical wrap
     */
    public void setWrap(int wrapS, int wrapT) {
        if (id == 0) return;
        int binding = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, wrapS);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, wrapT);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, binding);
    }
    
    /** 
     * Returns the texture width.
     * @return the width
     */
    public int getWidth() { return width; }
    
    /**
     * Returns the texture height.
     * @return the height
     */
    public int getHeight() { return height; }
    
    /**
     * Return the actual width.
     * @return the actual width
     */
    public int getActualWidth() { return actualWidth; }
    
    /**
     * Returns the actual height.
     * @return the actual height
     */
    public int getActualHeight() { return actualHeight; }
    
    /** Binds this texture. */
    public void bind() {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
    }
    
    /** Unbinds this texture. */
    public void unbind() {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }
    
    /**
     * Returns the id.
     * @return the id
     */
    public final int id() {
        return id;
    }
    
    /** Dispose this texture. */
    public void dispose() {
        if (id != 0) {
            unbind();
            GL11.glDeleteTextures(id);
        }
    }
    
    private static int getValidSize(int size) {
        int powerOfTwo = 1;
        while (powerOfTwo < size)
            powerOfTwo *= 2;
        return powerOfTwo;
    }
    
}
