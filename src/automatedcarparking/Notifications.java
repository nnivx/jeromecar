/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package automatedcarparking;

import automatedcarparking.graphics.Text;
import automatedcarparking.graphics.BMFont;
import automatedcarparking.graphics.Util;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author nikki
 */
public class Notifications {
    
    private static final float FADE_TIME = 1.0f;
    private static final float MESSAGE_DURATION = 3.0f;
    private static final float TEXT_SCALE = 0.3f;
    private static final float BOX_HEIGHT = TEXT_SCALE*150;
    private static final float BOX_WIDTH = TEXT_SCALE*600;
            
    private static class Message {
        boolean alive = true;
        float timer;
        float intensity;
        Text text;
        
        Message(float basey, BMFont font, String message) {
            text = new Text(font, -12);
            text.setText(message);
            text.setScale(TEXT_SCALE);
            text.setPos(10, basey);
        }
        
        void setY(float y) {
            text.setPos(10, y);
        }
        
        void update(float dt) {
            if (!alive) return;
            timer -= dt;
            if (timer <= FADE_TIME) {
                if (timer < 0) timer = 0;
                float t = (timer-FADE_TIME)/FADE_TIME;
                intensity = t*(float)Math.sin(t);
                if (timer <= 0) {
                    alive = false;
                }
            }
        }
        
        void render() {
            if (!alive) return;
            Util.pushColor(0.3f, 0.3f, 0.3f, 0.6f);
            Util.drawQuad(0, text.getPosY(), BOX_WIDTH, BOX_HEIGHT);
            Util.popColor();
            text.render();
        }
        
    }
    
    private final List<Message> messages = new LinkedList<>();
    
    public void update(float dt) {
        for (Iterator<Message> i = messages.iterator(); ; ) {
            
        }
    }
    
    public void render() {
        for (Message m: messages)
            m.render();
    }
    
}
