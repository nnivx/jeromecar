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
public final class MathUtil {
    
    public static float offsetX(float x1, float dist, float angle) {
        return (float)(Math.cos(angle)*dist + x1);
    }
    
    public static float offsetY(float y1, float dist, float angle) {
        return (float)(Math.sin(angle)*dist + y1);
    }
   
    public static boolean in(float min, float max, float value) {
        return !(value < min || value > max);
    }
    
    public static boolean pointInAABB(float rectL, float rectB, float rectW, float rectH, float pointX, float pointY) {
        final float rectT = rectB + rectH;
        final float rectR = rectL + rectW;
        return in(rectL, rectW, pointX) && in(rectB, rectT, pointY);
    }
    
    public static boolean pointInCircle(float circleX, float circleY, float radius, float pointX, float pointY) {
        return distanceSq(circleX, circleY, pointX, pointY) < (radius*radius);
    }
           
    public static float distance(float x1, float y1, float x2, float y2) {
        return (float)Math.sqrt(distanceSq(x1, y1, x2, y2));
    }
    
    public static float distanceSq(float x1, float y1, float x2, float y2) {
        return (x2-x1)*(x2-x1) + (y2-y1)*(y2-y1);
    }
            
    public static float angle(float x1, float y1, float x2, float y2) {
        return (float)Math.atan2(y2-y1, x2-x1);
    }
    
    private MathUtil() {
        
    }
    
}
