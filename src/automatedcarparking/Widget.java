/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package automatedcarparking;

import automatedcarparking.graphics.Texture;

/**
 *
 * @author nikki
 */
class Widget {
    
    private final Texture texture;    // default texture
    
    boolean hover;
    boolean selected;
    
    Widget(Texture texture) {
        this.texture = texture;
    }
    
   
            
}
