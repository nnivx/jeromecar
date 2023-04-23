/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package automatedcarparking;

/**
 *
 * @author nikki
 */
public class PointBuffer {
    
    private float[] buf;
    private int count;
    private int cur;

    public PointBuffer(int initialCapacity) {
        if (initialCapacity < 0) initialCapacity = 16;
        buf = new float[initialCapacity*2];
        cur = -2;
        count = 0;
    }
    
    public PointBuffer(PointBuffer other) {
        buf = new float[other.count];
        System.arraycopy(other.buf, 0, buf, 0, other.count);
        count = other.count;
        cur = other.cur;
    }
     
    public void clear() {
        count = 0;
        cur = -2;
    }
   
    public int size() {
        return count;
    }
    
    public boolean isEmpty() {
        return count == 0;
    }
    
    public float getX(int i) {
        return buf[i*2];
    }
    
    public float getY(int i) {
        return buf[i*2+1];
    }
    
    public void push(float x, float y) {
        ensureCapacity(2);
        cur += 2;
        buf[cur] = x;
        buf[cur+1] = y;
        ++count;
    }

    public void pop() {
        if (!(cur > 0))
            throw new RuntimeException("underflow");
        cur -= 2;
        --count;
    }

    private void ensureCapacity(int size) {
        if ((buf.length-cur) > size) return;
        if ((buf.length+size) > 1000000)
            throw new RuntimeException("overflow");
        float[] nbuf = new float[buf.length*2 + 16];
        System.arraycopy(buf, 0, nbuf, 0, buf.length);
        buf = nbuf;
    }
    
}
