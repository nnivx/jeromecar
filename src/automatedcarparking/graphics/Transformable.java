/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package automatedcarparking.graphics;

import org.joml.Matrix4f;


/**
 *
 * @author nikki
 */
public class Transformable {
    
    private Matrix4f matrix = new Matrix4f();
    private Matrix4f inverse = new Matrix4f();
    
    private float Tx = 0, Ty = 0, Sx = 1, Sy = 1, Rz = 0;
    private boolean needsUpdate;
    
    public void clear() { matrix.identity(); }
    
    protected Matrix4f matrix() {
        matrix.identity().translate(Tx, Ty, 0).rotate(Rz, 0, 0, 1).scale(Sx, Sy, 1);
        needsUpdate = true;
        return matrix;
    }
            
    public void setX(float x) { setPos(x, Ty); }
    public void setY(float y) { setPos(Tx, y); }
    public void setPos(float x, float y) {
        Tx = x;
        Ty = y;
        matrix();
    }
    
    public void setRot(float angle) {
        Rz = angle;
        matrix();
    }
    
    public void setScale(float scale) { setScale(scale, scale); }
    public void setScale(float scaleX, float scaleY) {
        Sx = scaleX;
        Sy = scaleY;
        matrix();
    }
    
    public void translateX(float tx) { setPos(Tx+tx, Ty); }
    public void translateY(float ty) { setPos(Ty, Ty+ty); }
    public void translate(float tx, float ty) { setPos(Tx+tx, Ty+ty); }
    
    public void rotate(float angle) { setRot(Rz+angle); }
    
    public void scale(float s) { setScale(Sx*s, Sy*s); }
    public void scale(float sx, float sy) { setScale(Sx*sx, Sy*sy); }
    
    public Matrix4f matrix(Matrix4f dest) {
        return dest.set(matrix);
    }
    
    public Matrix4f inverse(Matrix4f dest) {
        if (needsUpdate) {
            inverse = matrix.invert(inverse);
            needsUpdate = false;
        }
        return dest.set(inverse);
    }
    
    public float getPosX() { return Tx; }
    public float getPosY() { return Ty; }
    public float getRot() { return Rz; }
    public float getScaleX() { return Sx; }
    public float getScaleY() { return Sy; }
    
}
