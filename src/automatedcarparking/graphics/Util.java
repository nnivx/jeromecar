/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package automatedcarparking.graphics;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

/**
 *
 * @author nikki
 */
public final class Util {
    
    private static File getClassLoaderResource(String resource) {
        URL url = Thread.currentThread().getContextClassLoader().getResource(resource);
        try {
            return (url != null)? new File(url.toURI()): null;
        } catch (URISyntaxException ex) {
            return null;
        }
    }
    
    private static File getExecDirResource(String resource) {
        URI uri = new File(System.getProperty("user.dir")).toURI();
        File file = new File(uri.resolve(resource));
        return file.exists()? file: null;
    }
    
    /**
     * Finds a resource.
     * @param resource
     * @return 
     */
    public static File findResource(String resource) {
        File file = getClassLoaderResource(resource);
        if (file == null)
            file = getExecDirResource(resource);
        return file;
    }
    
    /**
     * Resizes a {@code ByteBuffer}.
     * 
     * @param alloc an allocator
     * @param buffer a byte buffer
     * @param capacity new capacity
     * @return {@code buffer} resized to {@code capacity}
     *  
     * @see org.lwjgl.demo.opengl.util.DemoUtils
     */
    public static ByteBuffer resizeBuffer(Allocator alloc, ByteBuffer buffer, int capacity) {
        ByteBuffer newBuffer = alloc.allocate(capacity);
        buffer.flip();
        // DirectByteBuffer should optimize this
        newBuffer.put(buffer);
        return newBuffer;
    }
    
    /**
     * Opens a resource and loads it contents into a {@code ByteBuffer}.
     * 
     * @param resource resource location
     * @return the buffer
     * @throws IOException 
     */
    public static ByteBuffer resourceToByteBuffer(String resource)
            throws IOException {
        return resourceToByteBuffer(resource, 1024);
    }
    
    /**
     * Opens a resource and loads it contents into a {@code ByteBuffer}.
     * 
     * @param resource resource location
     * @param initialCapacity initial capacity
     * @return the buffer
     * @throws IOException 
     */
    public static ByteBuffer resourceToByteBuffer(String resource, int initialCapacity)
            throws IOException {
        return resourceToByteBuffer(BufferUtils::createByteBuffer, resource, initialCapacity);
    }
    
    /**
     * Opens a resource and loads it contents into a {@code ByteBuffer}.
     * 
     * @param alloc an allocator
     * @param resource resource location
     * @param initialCapacity initial capacity
     * @return the buffer
     * @throws IOException 
     */
    public static ByteBuffer resourceToByteBuffer(Allocator alloc, String resource, int initialCapacity)
            throws IOException {
        if (initialCapacity < 1)
            throw new IllegalArgumentException("invalid initial capacity");
        
        File file = findResource(resource);
        if (file == null)
            throw new FileNotFoundException("File not found: " + resource);
        
        ByteBuffer buffer = alloc.allocate(initialCapacity);
        try (InputStream source = new FileInputStream(file)) {
            if (source == null)
                throw new FileNotFoundException(resource);
            while (true) {
                // read byte into buffer
                int value = source.read();
                if (value != -1)
                    buffer.put((byte)value);
                else 
                    break;
                // reize if needed
                if (buffer.remaining() == 0)
                    buffer = resizeBuffer(alloc, buffer, buffer.capacity()*2);
            }
            buffer.flip();
        }
        
        return buffer;
    }
    
    private static class ColorStack {
        float[] buf = new float[16*4];
        int cur;
        
        ColorStack() {
            cur = 0;
            buf[cur + 0] = 1;
            buf[cur + 1] = 1;
            buf[cur + 2] = 1;
            buf[cur + 3] = 1;
        }
        
        void push() {
            ensureCapacity(4);
            System.arraycopy(buf, cur, buf, cur+4, 4);
            cur += 4;
        }
        
        void push(float r, float g, float b, float a) {
            ensureCapacity(4);
            cur += 4;
            buf[cur] = r;
            buf[cur+1] = g;
            buf[cur+2] = b;
            buf[cur+3] = a;
            GL11.glColor4f(r, g, b, a);
        }
        
        void pop() {
            if (cur < 4)
                throw new RuntimeException("stack underflow");
            cur -= 4;
            GL11.glColor4f(buf[cur], buf[cur+1], buf[cur+2], buf[cur+3]);
        }
        
        void ensureCapacity(int size) {
            if ((buf.length - cur) >= size) return;
            if (buf.length+size > 120*4)
                throw new RuntimeException("stack overflow");
            float[] nbuf = new float[buf.length*2];
            System.arraycopy(buf, 0, nbuf, 0, buf.length);
            buf = nbuf;
        }
        
    }
    private static final ColorStack COLORSTACK = new ColorStack();
    
    public static void pushColor(float r, float g, float b, float a) {
        COLORSTACK.push(r, g, b, a);
    }
    
    public static void popColor() {
        COLORSTACK.pop();
    }
    
    public static void drawQuad(float x, float y, float w, float h) {
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x+w, y);
        GL11.glVertex2f(x+w, y+h);
        GL11.glVertex2f(x, y+h);
        GL11.glEnd();
    }
    
    public static void drawQuadTex(float x, float y, float w, float h) {
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(0, 0);
        GL11.glVertex2f(x, y);
        GL11.glTexCoord2f(1, 0);
        GL11.glVertex2f(x+w, y);
        GL11.glTexCoord2f(1, 1);
        GL11.glVertex2f(x+w, y+h);
        GL11.glTexCoord2f(0, 1);
        GL11.glVertex2f(x, y+h);
        GL11.glEnd();
    }
    
    public static void drawLine(float x1, float y1, float x2, float y2) {
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2f(x1, y1);
        GL11.glVertex2f(x2, y2);
        GL11.glEnd();
    }
    
    public static void drawCircle(float cx, float cy, float r, int segments) {
	double theta = 2.0*3.1415926/segments; 
	double c = Math.cos(theta);
	double s = Math.sin(theta);

	double x = r; //we start at angle = 0 
	double y = 0; 
    
	GL11.glBegin(GL11.GL_LINE_LOOP); 
	for(int i = 0; i < segments; ++i) { 
            GL11.glVertex2d(x + cx, y + cy); 

            //apply the rotation matrix
            double t = x;
            x = c * x - s * y;
            y = s * t + c * y;
	} 
	GL11.glEnd(); 
    }
    
    private Util() { }
    
}
