import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GraphicsConfiguration;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.Timer;

import javax.media.j3d.*;
import javax.swing.JFrame;
import javax.vecmath.*;

import com.sun.j3d.utils.geometry.Box;
import com.sun.j3d.utils.geometry.Primitive;
import com.sun.j3d.utils.image.TextureLoader;
import com.sun.j3d.utils.universe.SimpleUniverse;

public class App extends JFrame implements KeyListener {

    private TransformGroup bookRootTG;
    private Transform3D bookRootTransform = new Transform3D();
    
    private TransformGroup frontCoverHingeTG;
    private float frontCoverAngle = 0.0f;

    private static final int NUM_PAGES = 9;
    private TransformGroup[] pageHingeTGs = new TransformGroup[NUM_PAGES];
    private float[] pageAngles = new float[NUM_PAGES];
    
    private float bookRotY = -0.4f;
    private float bookRotX = 0.5f;
    
    private boolean isAnimating = false; 
    private final float FULL_FLIP = (float) Math.PI - 0.1f;

    public App() {
        super("Book");
        setLayout(new BorderLayout());
        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
        Canvas3D canvas = new Canvas3D(config);
        add("Center", canvas);
        canvas.addKeyListener(this);

        SimpleUniverse universe = new SimpleUniverse(canvas);
        universe.getViewingPlatform().setNominalViewingTransform();
        
        BranchGroup scene = createSceneGraph();
        scene.compile();
        universe.addBranchGraph(scene);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1800, 1200);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private BranchGroup createSceneGraph() {
        BranchGroup root = new BranchGroup();

        bookRootTG = new TransformGroup();
        bookRootTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        bookRootTG.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        root.addChild(bookRootTG);
        updateBookRotation();

        float bookWidth = 0.3f;
        float bookHeight = 0.4f;
        float coverThickness = 0.01f;
        float pageThickness = 0.002f;
        float blockThickness = 0.05f;

        Appearance coverApp = createTextureAppearance("res/cover.png", new Color3f(0.5f, 0.1f, 0.1f));
        Appearance pageApp = createTextureAppearance("res/page.png", new Color3f(0.95f, 0.95f, 0.9f));

        addBox(bookRootTG, new Vector3f(bookWidth, 0.0f, -blockThickness - coverThickness), 
               bookWidth, bookHeight, coverThickness, coverApp);

        addBox(bookRootTG, new Vector3f(0.0f, 0.0f, -blockThickness / 2.0f), 
               coverThickness, bookHeight, blockThickness / 2.0f + coverThickness, coverApp);

        frontCoverHingeTG = createHinge(bookRootTG, new Vector3f(bookWidth, 0.0f, coverThickness), 
                                       bookWidth, bookHeight, coverThickness, coverApp);

        for (int i = 0; i < NUM_PAGES; i++) {
            float zOffset = - (i * 0.006f); 
            pageHingeTGs[i] = createHinge(bookRootTG, new Vector3f(bookWidth * 0.95f, 0.0f, zOffset), 
                                         bookWidth * 0.95f, bookHeight * 0.95f, pageThickness, pageApp);
            pageAngles[i] = 0.0f;
        }

        BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 100.0);
        DirectionalLight light = new DirectionalLight(new Color3f(1.0f, 1.0f, 1.0f), new Vector3f(4.0f, -7.0f, -12.0f));
        light.setInfluencingBounds(bounds);
        root.addChild(light);
        root.addChild(new AmbientLight(new Color3f(0.5f, 0.5f, 0.5f)));

        return root;
    }

    private TransformGroup createHinge(Group parent, Vector3f offset, float w, float h, float d, Appearance app) {
        TransformGroup hinge = new TransformGroup();
        hinge.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        TransformGroup offsetTG = new TransformGroup();
        Transform3D t3d = new Transform3D();
        t3d.setTranslation(offset);
        offsetTG.setTransform(t3d);
        offsetTG.addChild(new Box(w, h, d, Primitive.GENERATE_NORMALS | Primitive.GENERATE_TEXTURE_COORDS, app));
        hinge.addChild(offsetTG);
        parent.addChild(hinge);
        return hinge;
    }

    private void addBox(Group parent, Vector3f pos, float w, float h, float d, Appearance app) {
        TransformGroup tg = new TransformGroup();
        Transform3D t3d = new Transform3D();
        t3d.setTranslation(pos);
        tg.setTransform(t3d);
        tg.addChild(new Box(w, h, d, Primitive.GENERATE_NORMALS | Primitive.GENERATE_TEXTURE_COORDS, app));
        parent.addChild(tg);
    }

    private Appearance createTextureAppearance(String filePath, Color3f fallbackColor) {
        Appearance app = new Appearance();
        Material mat = new Material(fallbackColor, new Color3f(0,0,0), fallbackColor, new Color3f(1,1,1), 64f);
        app.setMaterial(mat);
        try {
            Texture tex = new TextureLoader(filePath, "RGB", new Container()).getTexture();
            if (tex != null) app.setTexture(tex);
        } catch (Exception e) {}
        return app;
    }

    private void updateBookRotation() {
        Transform3D rX = new Transform3D();
        Transform3D rY = new Transform3D();
        rX.rotX(bookRotX);
        rY.rotY(bookRotY);
        bookRootTransform.mul(rX, rY);
        bookRootTG.setTransform(bookRootTransform);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (isAnimating) return;

        int key = e.getKeyCode();

        if (key == KeyEvent.VK_W) bookRotX -= 0.1f;
        if (key == KeyEvent.VK_S) bookRotX += 0.1f;
        if (key == KeyEvent.VK_A) bookRotY -= 0.1f;
        if (key == KeyEvent.VK_D) bookRotY += 0.1f;
        updateBookRotation();

        // Open cover
        if (key == KeyEvent.VK_1 && frontCoverAngle < 0.1f) {
            animateCover(0, FULL_FLIP);
        }
        
        // Close cover
        if (key == KeyEvent.VK_2 && frontCoverAngle > 1.0f) {
            boolean pagesClosed = true;
            for(float angle : pageAngles) if (angle > 0.1f) pagesClosed = false;
            if (pagesClosed) animateCover(FULL_FLIP, 0);
        }

        // Turn page forward
        if (key == KeyEvent.VK_3 && frontCoverAngle > 2.0f) {
            for (int i = 0; i < NUM_PAGES; i++) {
                if (pageAngles[i] < 0.1f) { 
                    animatePage(i, 0, FULL_FLIP);
                    break;
                }
            }
        }

        // Turn page back
        if (key == KeyEvent.VK_4 && frontCoverAngle > 2.0f) {
            for (int i = NUM_PAGES - 1; i >= 0; i--) {
                if (pageAngles[i] > 2.0f) { 
                    animatePage(i, FULL_FLIP, 0);
                    break;
                }
            }
        }
    }

    private void animatePage(final int index, float start, float end) {
        isAnimating = true;
        final boolean forward = (end > start);
        Timer timer = new Timer(15, null);
        timer.addActionListener(evt -> {
            if (forward) {
                pageAngles[index] += 0.1f;
                if (pageAngles[index] >= end) {
                    pageAngles[index] = end;
                    stopTimer(timer);
                }
            } else {
                pageAngles[index] -= 0.1f;
                if (pageAngles[index] <= end) {
                    pageAngles[index] = end;
                    stopTimer(timer);
                }
            }
            updateHinge(pageHingeTGs[index], pageAngles[index]);
        });
        timer.start();
    }

    private void animateCover(float start, float end) {
        isAnimating = true;
        final boolean opening = (end > start);
        Timer timer = new Timer(15, null);
        timer.addActionListener(evt -> {
            if (opening) {
                frontCoverAngle += 0.1f;
                if (frontCoverAngle >= end) {
                    frontCoverAngle = end;
                    stopTimer(timer);
                }
            } else {
                frontCoverAngle -= 0.1f;
                if (frontCoverAngle <= end) {
                    frontCoverAngle = end;
                    stopTimer(timer);
                }
            }
            updateHinge(frontCoverHingeTG, frontCoverAngle);
        });
        timer.start();
    }

    private void stopTimer(Timer t) {
        t.stop();
        isAnimating = false;
    }

    private void updateHinge(TransformGroup tg, float angle) {
        Transform3D t3d = new Transform3D();
        t3d.rotY(-angle);
        tg.setTransform(t3d);
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) { new App(); }
}
