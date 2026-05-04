import javax.media.j3d.*;
import javax.vecmath.*;
import java.awt.event.*;
import java.util.Enumeration;

public class BicycleBehavior extends Behavior {
    private TransformGroup bikeTG, fwTG, rwTG;
    private Vector3f fwCenter, rwCenter;
    private Transform3D bikeT3D = new Transform3D();
    private WakeupOnElapsedFrames wakeUp = new WakeupOnElapsedFrames(0);
    
    private boolean w, s, a, d;
    private float posX = 0, posZ = 0, rotY = 0, wheelRot = 0;
    
    private final float SPEED = 0.04f;
    private final float TURN = 0.03f;

    public BicycleBehavior(TransformGroup main, TransformGroup fw, TransformGroup rw, Vector3f fwc, Vector3f rwc, Canvas3D canvas) {
        this.bikeTG = main;
        this.fwTG = fw;
        this.rwTG = rw;
        this.fwCenter = fwc;
        this.rwCenter = rwc;
        canvas.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) { handleKey(e.getKeyCode(), true); }
            public void keyReleased(KeyEvent e) { handleKey(e.getKeyCode(), false); }
        });
    }

    private void handleKey(int k, boolean p) {
        if (k == KeyEvent.VK_W) w = p;
        if (k == KeyEvent.VK_S) s = p;
        if (k == KeyEvent.VK_A) a = p;
        if (k == KeyEvent.VK_D) d = p;
    }

    public void initialize() {
        wakeupOn(wakeUp);
    }

    public void processStimulus(Enumeration criteria) {
        boolean isMoving = w || s;

        if (isMoving) {
            float direction = w ? -1.0f : 1.0f;

            if (d) rotY += TURN * direction;
            if (a) rotY -= TURN * direction;

            posX += SPEED * direction * Math.sin(rotY);
            posZ += SPEED * direction * Math.cos(rotY);
            
            wheelRot += 0.15f * direction;
        }

        bikeT3D.setIdentity();
        bikeT3D.setTranslation(new Vector3f(posX, 0, posZ));
        Transform3D rotation = new Transform3D();
        rotation.rotY(rotY);
        bikeT3D.mul(rotation);
        bikeTG.setTransform(bikeT3D);

        updateWheelTransform(fwTG, fwCenter);
        updateWheelTransform(rwTG, rwCenter);

        wakeupOn(wakeUp);
    }

    private void updateWheelTransform(TransformGroup tg, Vector3f center) {
        Transform3D finalTransform = new Transform3D();
        
        Transform3D toOrigin = new Transform3D();
        toOrigin.setTranslation(new Vector3f(-center.x, -center.y, -center.z));

        Transform3D rot = new Transform3D();
        rot.rotX(wheelRot);

        Transform3D toPos = new Transform3D();
        toPos.setTranslation(center);

        finalTransform.mul(toPos);
        finalTransform.mul(rot);
        finalTransform.mul(toOrigin);

        tg.setTransform(finalTransform);
    }
}
