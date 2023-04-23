/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package automatedcarparking;

import automatedcarparking.graphics.BMFont;
import automatedcarparking.graphics.Text;
import automatedcarparking.graphics.Texture;
import automatedcarparking.graphics.Util;
import automatedcarparking.misc.Container;
import automatedcarparking.misc.Game;
import java.io.IOException;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

/**
 * TODO: Stuff.
 * 
 * Notifications.
 * 
 * 
 * @author nikki
 */
public class Engine implements Game {
            
    // Display Flags
    public static final int CAR_HEADING = 1;
    public static final int CAR_TARGET  = 1 << 1;
    public static final int CAR_PATH    = 1 << 2;
    public static final int CAR_INFO    = 1 << 3;
    public static final int PATHS       = 1 << 4;
    public static final int POINTS      = 1 << 5;
    public static final int POINT_AREA  = 1 << 6;
    public static final int DEBUG_INFO  = 1 << 7;
    public static final int FLAGS_ALL   = 0xFFFFFFFF;
    public static final int DEFAULT_DISPLAY_FLAGS = CAR_PATH | PATHS | POINTS | CAR_INFO;
    
    // States
    public static final int STATE_SETUP   = 0;
    public static final int STATE_RUNNING = 1;
    public static final int STATE_DONE    = 2;
    
    // System/UI Parameters
    public static final int   BACKGROUND_COLOR = 0xEfEfF400;
    public static final float FLASH_DURATION = 0.6f;
    public static final float TOOLBOX_SPEED = 0.27f;
    public static final float GRAB_WIDGET_RADIUS = 150;
    public static final float POINT_RADIUS = 12;
    public static final float POINT_SIZE = 12;
    public static final float WIDGET_SCALE = 0.3f;
    public static final float TURN_RATE = 3.6f;
    
    // Other UI Parameters
    public static final int PARKING_LOT_WIDTH = (int)(WIDGET_SCALE*130);
    public static final int PARKING_LOT_HEIGHT = (int)(WIDGET_SCALE*210);
    private static final float ROTATE_WIDGET_INNER = GRAB_WIDGET_RADIUS + 10;
    private static final float ROTATE_WIDGET_OUTER = ROTATE_WIDGET_INNER + 24;
    
    // =========================================================================
    // PROJECT-RELATED
    
    // Simulation State
    final FuzzifyKernel kernel;
    float carX, carY, carHeading;
    float carSpeed = 100;
    PathSet pathSet;
    int targetPoint;
    int targetPath;
    float targetAngle;
    ParkingLot[] parkingLots;
    private final PointBuffer carPoints = new PointBuffer(16);
    
    // Simulation Control
    float simulationSpeed = 1;
    boolean paused = false;
    
    boolean dragCar = false;
    boolean rotateCar = false;
    
    // Preferences & Helpers
    int displayFlags = DEFAULT_DISPLAY_FLAGS; // @TODO reset
    
    // =========================================================================
    // SYSTEM-RELATED [hands off]
    
    // Flash animation (not Adobe Flash :D) 
    private boolean animatingFlash = false;
    private float flashTimer = 0;
    private float flashIntensity = 1;
    
    // Other necessary variables
    private int state = STATE_SETUP;
    private boolean initialized = false;
    private Toolbox toolbox;
            
    private boolean hoverCar = false, hoverRot = false;
    private float dragOffsetX, dragOffsetY, rotateOffsetAngle;
    
    // =========================================================================
    // GRAPHICS [hands off]
    
    private final Texture carTexture;       // car widget texture
    private final Texture widgetTexture;    // rotate widget texture
    private final Texture pointTexture;     // points texture
    private final Texture bannerTexture;    // banner texture
    
    private final Matrix4f gr_scrMatrix = new Matrix4f();
    
    BMFont font;
    private Text floatingHelperText;
    
    // =========================================================================
    // MAIN INTERFACE
    
    public Engine(FuzzifyKernel kernel) {
        this.kernel = kernel;
        carTexture = new Texture();
        widgetTexture = new Texture();
        pointTexture = new Texture();
        bannerTexture = new Texture();
    }
    
    public void reset() {
        flash();
        carX = 400;
        carY = 300;
        carHeading = 0;
        targetPoint = 0;
        simulationSpeed = 1;
        selectPath(0);
        carPoints.clear();
        if (state == STATE_DONE)
            state = STATE_SETUP;
        paused = true;
    }
    
    public void restart() {
        reset();
        state = STATE_SETUP;
    }
    
    @Override
    public boolean init(Container container, String[] args) {
        container.setWindowTitle("AutomaticCarParker");
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        
        Vector2f size = container.getWindowSize();
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glOrtho(0, size.x, size.y, 0, 1, -1);
        GL11.glMatrixMode(GL11.GL_MATRIX_MODE);
        
        GL11.glClearColor(
                ((BACKGROUND_COLOR>>24) & 0xFF)/255.f, 
                ((BACKGROUND_COLOR>>16) & 0xFF)/255.f,
                ((BACKGROUND_COLOR>>8) & 0xFF)/255.f,
                ((BACKGROUND_COLOR) & 0xFF)/255.f);
            
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        
        if (!initialized) {
            // initialize external resources
            try {
                font = BMFont.load("ubuntu_mono");
                
                toolbox = new Toolbox(this);
                
                carTexture.load("car.png");
                widgetTexture.load("widget.png");
                pointTexture.load("point.png");
                bannerTexture.load("banner.png");
                        
                pathSet = PathSet.load("paths.txt");
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
                return false;
            }
            
            // rest of initialization 
            floatingHelperText = new Text(font, -12);
            floatingHelperText.setScale(0.40f);
            createParkingLots();
            
            initialized = true;
        }
        reset();
        return true;
    }
    
    @Override
    public void update(Container container, float dt) {
        // update flash effect
        if (animatingFlash) updateFlashEffect(dt);
        
        // update toolbox
        toolbox.update(container, dt);
        
        // update text
        if (state == STATE_SETUP) {
            floatingHelperText.setPos(carX - 135*WIDGET_SCALE, carY + 165*WIDGET_SCALE);
            if (dragCar) {
                floatingHelperText.setText("x = " + carX + '\n' + "y = " + carY);
            } else if (rotateCar) {
                // @TODO display is negative angle(!!)
                int ang = -(int)Math.toDegrees(carHeading);
                int dec = (int)(Math.abs(Math.toDegrees(carHeading))*100)%100;
                if (ang < 0) ang = 360 + ang;
                floatingHelperText.setText("angle = " + ang + "." + dec);
            } else {
                floatingHelperText.setText("");
            }
        }
        
        if (paused) return;
        
        if (state == STATE_RUNNING) {
            // @TODO
            Vector2f tgt = pathSet.getPoint(targetPath, targetPoint);
            targetAngle = MathUtil.angle(carX, carY, tgt.x, tgt.y);
            moveCar(dt);
        }
        
    }
   
    @Override
    public void render(Container container) {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
        
        // render path
        renderPath();
        
        // render car and widget
        renderCar();
        
        // render overlay graphics
        renderCarOverlay();
        
        // render overlay info
        renderMiscOverlay();
        
        // render state done banner
        if (state == STATE_DONE) {
            Util.pushColor(0, 0, 0, 0.3f);
            Util.drawQuad(0, 0, 800, 600);
            Util.popColor();
            Texture.bind(bannerTexture);
            Util.drawQuadTex(0, (600-150)/2, 800, 150);
            Texture.bind(null);
        }
        
        // render flash effect
        if (animatingFlash) {
            Util.pushColor(1,1,1,flashIntensity);
            Util.drawQuad(0, 0, 800, 600);
            Util.popColor();
        }
        
        // render toolbox
        toolbox.render(container);
        
    }
    
    @Override
    public void cleanup(Container container) {
        initialized = false;
        pointTexture.dispose();
        carTexture.dispose();
        widgetTexture.dispose();
        font.dispose();
    }

    public PointBuffer getPointBuffer() {
        return new PointBuffer(carPoints);
    }
    
    // main main function
    private void moveCar(float dt) {
        // apply speed
        dt = dt*simulationSpeed;
        
        // fuzzify
        float ang;
        {
            float carHeadingDeg = (float)Math.toDegrees(carHeading);
            float targetAngleDeg = (float)Math.toDegrees(targetAngle);
            float angDeg = kernel.fuzzify(carHeadingDeg, targetAngleDeg);
            ang = (float)Math.toRadians(angDeg);
        }
        
        // move and orient car
        carHeading += dt*TURN_RATE*ang;
        carX = MathUtil.offsetX(carX, dt*carSpeed, carHeading);
        carY = MathUtil.offsetY(carY, dt*carSpeed, carHeading);
        
        // add point
//        if (carPoints.length < (carPathPoints+1)*2) {
//            Vector2f[] nbuf = new Vector2f[carPoints.length*2];
//            System.arraycopy(carPoints, 0, nbuf, 0, carPoints.length);
//            carPoints = nbuf;
//        }
//        carPoints[carPathPoints] = new Vector2f(carX, carY);
//        ++carPathPoints;
        carPoints.push(carX, carY);
        
        // check if car arrived at point
        Vector2f tgt = pathSet.getPoint(targetPath, targetPoint);
        if (MathUtil.pointInCircle(tgt.x, tgt.y, POINT_RADIUS, carX, carY)) {
            ++targetPoint;
            // check if we arrived at the goal
            if (targetPoint >= pathSet.numPoints(targetPath)) {
                state = STATE_DONE;
            }
        }
    }
    
    // =========================================================================
    // EVENT HANDLING
    
    @Override
    public void keyPressed(Container c, int key, int code, int mods) {
        
    }

    @Override
    public void keyReleased(Container c, int key, int code, int mods) {
        switch (key) {
            case GLFW.GLFW_KEY_R:
                restart(); // @UPDATE
                break;
            case GLFW.GLFW_KEY_GRAVE_ACCENT:
                paused = !paused;
                break;
            case GLFW.GLFW_KEY_1:
                simulationSpeed = 1;
                break;
            case GLFW.GLFW_KEY_2:
                simulationSpeed = 2;
                break;
            case GLFW.GLFW_KEY_3:
                simulationSpeed = 3;
                break;
            case GLFW.GLFW_KEY_ENTER:
                if (state == STATE_SETUP) {
                    state = STATE_RUNNING;
                    paused = false;
                } else if (state == STATE_DONE) {
                    restart();
                }
                break;
        }
        toolbox.keyReleased(c, key, code, mods);
    }

    @Override
    public void mouseButtonPressed(Container c, int button, int mods) {
        if (state == STATE_SETUP) {
            // check all lots
            for (int i = 0; i < parkingLots.length; ++i)
                parkingLots[i].mouseButtonPressed(c, button, mods);
            
            // check if hovering car
            if (hoverCar) {
                dragCar = true;
                final Vector2f mpos = c.getMousePos();
                dragOffsetX = carX - mpos.x ;
                dragOffsetY = carY - mpos.y;
            }

            // check if hovering rot
            if (hoverRot) {
                rotateCar = true;
                final Vector2f mpos = c.getMousePos();
                rotateOffsetAngle = carHeading - MathUtil.angle(carX, carY, mpos.x, mpos.y);
            }
        }
        
        toolbox.mouseButtonPressed(c, button, mods);
    }

    @Override
    public void mouseButtonReleased(Container c, int button, int mods) {
        dragCar = false;
        rotateCar = false;
        
        if (state == STATE_SETUP) {
            // check all lots
            for (int i = 0; i < parkingLots.length; ++i)
                parkingLots[i].mouseButtonReleased(c, button, mods);
        }
        
        toolbox.mouseButtonReleased(c, button, mods);
    }

    @Override
    public void mouseMoved(Container c, double xd, double yd) {
        final float x = (float)xd, y = (float)yd;
        if (state == STATE_SETUP) {
            // check all lots
            for (int i = 0; i < parkingLots.length; ++i)
                parkingLots[i].mouseMoved(c, xd, yd);
        
            // check if mouse is hovering car
            hoverCar = MathUtil.pointInCircle(carX, carY, 150*WIDGET_SCALE/2.f, x, y);

            // check if mouse is hovering rot
            hoverRot = !hoverCar && MathUtil.pointInCircle(carX, carY, 300*WIDGET_SCALE/2.f, x, y);

            // if car is being rotated, update
            if (rotateCar) {
                carHeading = rotateOffsetAngle + MathUtil.angle(carX, carY, x, y);
            }

            // if car is being dragged, update
            if (dragCar) {
                carX = x + dragOffsetX;
                carY = y + dragOffsetY;
                final float size = (150*WIDGET_SCALE);
                carX = Math.min(800-size-200*0.20f, Math.max(carX, size));
                carY = Math.min(600-size, Math.max(carY, size));
            }
        }
            
        toolbox.mouseMoved(c, x, y);
    }
    
    // =========================================================================
    // INTERNAL MODULE FUNCTIONS
    
    void selectPath(int i) {
        if (state == STATE_SETUP) {
            parkingLots[targetPath].setSelected(false);
            targetPath = i;
            parkingLots[targetPath].setSelected(true);
        }
    }
    
    // =========================================================================
    // HELPER FUNCTIONS
    
    private void createParkingLots() {
        parkingLots = new ParkingLot[pathSet.numPaths()];
        for (int i = 0; i < parkingLots.length; ++i) {
            final int lastPoint = pathSet.numPoints(i) - 1;
            Vector2f a = pathSet.getPoint(i, lastPoint);
            Vector2f b = pathSet.getPoint(i, lastPoint-1);
            float ang = MathUtil.angle(a.x, a.y, b.x, b.y);
            parkingLots[i] = new ParkingLot(this, i, a.x, a.y, ang, PARKING_LOT_WIDTH, PARKING_LOT_HEIGHT);
            parkingLots[i].setVisible(true);
        }
    }
    
    private void flash() {
        flashTimer = FLASH_DURATION;
        animatingFlash = true;
    }
    
    private boolean inPoint(int path, int point, float x, float y) {
        Vector2f p = pathSet.getPoint(path, point);
        return MathUtil.pointInCircle(p.x, p.y, POINT_RADIUS, x, y);
    }
    
    private void updateFlashEffect(float dt) {
        // animate away~
        flashTimer -= dt;
        if (flashTimer <= 0) {
            flashTimer = 0;
            animatingFlash = false;
        }
        
        // normalize time
        final float t = (FLASH_DURATION - flashTimer)/FLASH_DURATION;
        
        // smooth interpoation
        flashIntensity = 1*(float)Math.sin(1 - t);
    }
    
    private void renderPath() {
        // draw lot
        if (state == STATE_SETUP) {
            for (int i = 0; i < parkingLots.length; ++i)
                parkingLots[i].render();
        } else {
            parkingLots[targetPath].render();
        }
        
        // draw area
        if ((displayFlags&Engine.POINTS) != 0) {
            // iterate paths
            Util.pushColor(0.2f, 0.8f, 0.2f, 0.7f);
            if (state == STATE_SETUP) {
                for (int i = 0; i < pathSet.numPaths(); ++i) {
                    render0PathArea(i);
                }
            } else {
                render0PathArea(targetPath);
            }
            Util.popColor();
        }
        
        // draw paths
        if ((displayFlags&Engine.PATHS) != 0) {
            if (state == STATE_SETUP) {
                // render paths
                GL11.glLineWidth(3);
                Util.pushColor(0.8f, 0.8f, 0.8f, 1);
                for (int i = 0; i < pathSet.numPaths(); ++i) {
                    if (i != targetPath) render0PathLines(i);
                }
                Util.popColor();
            } 
            // render selected over
            GL11.glLineWidth(7);
            Util.pushColor(0.7f, 0.7f, 0.7f, 1);
            render0PathLines(targetPath);
            Util.popColor();
            
            GL11.glLineWidth(1);
        }
        
        // draw points
        if ((displayFlags&Engine.POINTS) != 0) {
            // iterate paths
            if (state == STATE_SETUP) {
                Util.pushColor(0.2f, 1f, 0.2f, 1);
                for (int i = 0; i < pathSet.numPaths(); ++i) {
                    if (i != targetPath) render0PathPoints(i);
                }
                Util.popColor();
            }
            Util.pushColor(0, 1, 0, 1);
            render0PathPoints(targetPath);
            Util.popColor();
        }
    }
    
    private void render0PathLines(int path) {
        // iterate points in path
        GL11.glBegin(GL11.GL_LINE_STRIP);
        for (int j = 0; j < pathSet.numPoints(path); ++j) {
            Vector2f p = pathSet.getPoint(path, j);
            GL11.glVertex2f(p.x, p.y);
        }
        GL11.glEnd();
    }
    
    private void render0PathPoints(int path) {
        // iterate points in path
        Texture.bind(pointTexture);
        for (int j = 0; j < pathSet.numPoints(path); ++j) {
            Vector2f p = pathSet.getPoint(path, j);
            // draw point
            Util.drawQuadTex(p.x-POINT_SIZE/2, p.y-POINT_SIZE/2, POINT_SIZE, POINT_SIZE);
        }
        Texture.bind(null);
    }
    
    private void render0PathArea(int path) {
        // iterate points in path
        for (int j = 0; j < pathSet.numPoints(path); ++j) {
            Vector2f p = pathSet.getPoint(path, j);
            // draw area
            if ((displayFlags&Engine.POINT_AREA) != 0) {
                Util.pushColor(0.8f, 0.8f, 0.8f, 1);
                Util.drawCircle(p.x, p.y, POINT_RADIUS, 16);
                Util.popColor();
            }
        }
    }
    
    private void renderCarOverlay() {
        // @TODO
        if (state == STATE_RUNNING) {
            if ((displayFlags&Engine.CAR_HEADING) != 0) {
                Util.pushColor(1, 0, 0, 0.4f);
                float x2 = MathUtil.offsetX(carX, 75, carHeading);
                float y2 = MathUtil.offsetY(carY, 75, carHeading);
                Util.drawLine(carX, carY, x2, y2);
                Util.popColor();
            }
            if ((displayFlags&Engine.CAR_TARGET) != 0) {
                float x2 = MathUtil.offsetX(carX, 75, targetAngle);
                float y2 = MathUtil.offsetY(carY, 75, targetAngle);
                Util.pushColor(0, 0, 1, 0.4f);
                Util.drawLine(carX, carY, x2, y2);
                Util.popColor();
            }
        }
        if (state == STATE_RUNNING || state == STATE_DONE) {
            if ((displayFlags&Engine.CAR_PATH) != 0) {
                Util.pushColor(1.0f, 0.2f, 0.2f, 0.4f);
                // @TODO render to texture
                GL11.glBegin(GL11.GL_LINE_STRIP);
                for (int i = 0; i < carPoints.size(); ++i)
                    GL11.glVertex2f(carPoints.getX(i), carPoints.getY(i));
                GL11.glEnd();
                Util.popColor();
            }
        }
        if (state == STATE_SETUP) {
            if ((displayFlags&Engine.CAR_INFO) != 0) {
                Util.pushColor(0, 0, 0, 0.7f);
                floatingHelperText.render();
                Util.popColor();
            }
        }
    }
    
    private void renderMiscOverlay() {
        if ((displayFlags&Engine.DEBUG_INFO) != 0) {
            // @TODO
        }
    }
    
    private void renderCar() {
        GL11.glPushMatrix();
        GL11.glTranslatef(carX, carY, 0);
        GL11.glScalef(WIDGET_SCALE, WIDGET_SCALE, 1);
        GL11.glRotated(Math.toDegrees(carHeading), 0, 0, 1);
        
        final boolean scale = hoverCar || hoverRot || dragCar || rotateCar;
        if (scale) {
            GL11.glPushMatrix();
            GL11.glScalef(1.075f, 1.075f, 1);
        }
        Texture.bind(carTexture);
        final float carx = 150/2;
        final float cary = 90/2;
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(0, 0);
        GL11.glVertex2f(-carx, -cary);
        GL11.glTexCoord2f(1, 0);
        GL11.glVertex2f(carx, -cary);
        GL11.glTexCoord2f(1, 1);
        GL11.glVertex2f(carx, cary);
        GL11.glTexCoord2f(0, 1);
        GL11.glVertex2f(-carx, cary);
        GL11.glEnd();
        
        if (state == STATE_SETUP) {
            final boolean fade = !(hoverRot || rotateCar);
            if (fade) {
                Util.pushColor(1, 1, 1, 0.2f);
            }
            Texture.bind(widgetTexture);
            final float widgx = 300/2;
            final float widgy = 300/2;
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glTexCoord2f(0, 0);
            GL11.glVertex2f(-widgx, -widgy);
            GL11.glTexCoord2f(1, 0);
            GL11.glVertex2f(widgx, -widgy);
            GL11.glTexCoord2f(1, 1);
            GL11.glVertex2f(widgx, widgy);
            GL11.glTexCoord2f(0, 1);
            GL11.glVertex2f(-widgx, widgy);
            GL11.glEnd();
            Texture.bind(null);
            if (fade) {
                Util.popColor();
            }
        }
        Texture.bind(null);
        if (scale) {
            GL11.glPopMatrix();
        }
        
        GL11.glPopMatrix();
    }
    
}
