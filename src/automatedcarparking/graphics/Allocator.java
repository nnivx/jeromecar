/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package automatedcarparking.graphics;

/**
 *
 * @author nikki
 */
@FunctionalInterface
public interface Allocator {
    
    public java.nio.ByteBuffer allocate(int size);
    
}

