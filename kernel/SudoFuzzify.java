/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import automatedcarparking.FuzzifyKernel;

/**
 *
 * @author nikki
 */
public class SudoFuzzify implements FuzzifyKernel {
    
    private static float pos(float diff, float carHeading, float targetAngle) {
        if (Math.abs(diff) > 180) {
            return diff-360;
        } else {
            return diff;
        }
    }
    
    private static float neg(float diff, float carHeading, float targetAngle) {
        if (Math.abs(diff) > 180) {
            return 360+diff;
        } else {
            return diff;
        }
    }
    
    private static float zero(float diff, float carHeading, float targetAngle) {
        return 0;
    }
    
    @Override
    public float fuzzify(float carHeading, float targetAngle) {
        float diff = targetAngle - carHeading;
        float re;
        if (diff < -1.1f) {
            re = neg(diff, carHeading, targetAngle);
        } else if (diff > 1.1f) {
            re = pos(diff, carHeading, targetAngle);
        } else {
            re = zero(diff, carHeading, targetAngle);
        }
        return re;
    }
    
}
