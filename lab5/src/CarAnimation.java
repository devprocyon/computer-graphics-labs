import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.swing.Timer;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

public class CarAnimation implements ActionListener, KeyListener {
    private TransformGroup carGroup;
    
    private float xloc = 0.0f;
    private float zloc = 0.0f;
    private float speed = 0.0f;
    private float angle = 0.0f;
    
    private final float ACCELERATION = 0.0005f;
    private final float MAX_SPEED = 0.15f;
    private final float TURN_SPEED = 0.03f;
    private final float FRICTION = 0.001f;

    private boolean keyW = false;
    private boolean keyS = false;
    private boolean keyA = false;
    private boolean keyD = false;

    private Timer timer;

    public CarAnimation(TransformGroup carGroup) {
        this.carGroup = carGroup;
        timer = new Timer(20, this);
        timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (keyW) {
            speed = Math.max(speed - ACCELERATION, -MAX_SPEED / 2);
        } else if (keyS) {
            speed = Math.min(speed + ACCELERATION, MAX_SPEED);
        } else {
            if (speed > 0) speed = Math.max(0, speed - FRICTION);
            if (speed < 0) speed = Math.min(0, speed + FRICTION);
        }

        if (Math.abs(speed) > 0.005) {
            float direction = (speed > 0) ? 1 : -1;
            if (keyA) angle -= TURN_SPEED * direction;
            if (keyD) angle += TURN_SPEED * direction;
        }

        xloc += speed * Math.sin(angle);
        zloc += speed * Math.cos(angle);

        Transform3D transform = new Transform3D();
        
        transform.setTranslation(new Vector3f(xloc, 0.0f, zloc));
        
        Transform3D rotate = new Transform3D();
        rotate.rotY(angle);
        
        transform.mul(rotate);
        
        Transform3D scale = new Transform3D();
        scale.setScale(new Vector3d(0.2, 0.2, 0.2));
        transform.mul(scale);

        carGroup.setTransform(transform);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_W) keyW = true;
        if (key == KeyEvent.VK_S) keyS = true;
        if (key == KeyEvent.VK_A) keyA = true;
        if (key == KeyEvent.VK_D) keyD = true;
        if (key == KeyEvent.VK_SPACE) speed = 0;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_W) keyW = false;
        if (key == KeyEvent.VK_S) keyS = false;
        if (key == KeyEvent.VK_A) keyA = false;
        if (key == KeyEvent.VK_D) keyD = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {}
}
