import com.sun.j3d.loaders.Scene;
import com.sun.j3d.loaders.objectfile.ObjectFile;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.image.TextureLoader;
import javax.media.j3d.*;
import javax.vecmath.*;
import java.awt.*;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.swing.JFrame;

public class App extends JFrame {
    private Canvas3D canvas;
    private SimpleUniverse universe;
    private Vector3f fwCenter = new Vector3f();
    private Vector3f rwCenter = new Vector3f();

    public App() {
        canvas = new Canvas3D(SimpleUniverse.getPreferredConfiguration());
        getContentPane().add(canvas, BorderLayout.CENTER);
        universe = new SimpleUniverse(canvas);
        
        BranchGroup scene = createSceneGraph();
        scene.compile();
        
        setupCamera();
        
        universe.addBranchGraph(scene);
        
        setSize(1800, 1200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void setupCamera() {
        TransformGroup viewTG = universe.getViewingPlatform().getViewPlatformTransform();
        Transform3D t3d = new Transform3D();
        t3d.lookAt(new Point3d(3.0, 2.5, 6.0), new Point3d(0, 0, 0), new Vector3d(0, 1, 0));
        t3d.invert();
        viewTG.setTransform(t3d);
    }

    public BranchGroup createSceneGraph() {
        BranchGroup objRoot = new BranchGroup();
        BoundingSphere bounds = new BoundingSphere(new Point3d(0,0,0), 1000.0);

        DirectionalLight light = new DirectionalLight(new Color3f(1.0f, 1.0f, 1.0f), new Vector3f(4.0f, -7.0f, -12.0f));
        light.setInfluencingBounds(bounds);
        objRoot.addChild(light);

        AmbientLight ambient = new AmbientLight(new Color3f(0.2f, 0.2f, 0.2f));
        ambient.setInfluencingBounds(bounds);
        objRoot.addChild(ambient);

        objRoot.addChild(createGround());

        TransformGroup bikeMoveTG = new TransformGroup();
        bikeMoveTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        
        TransformGroup fwTG = new TransformGroup();
        fwTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        
        TransformGroup rwTG = new TransformGroup();
        rwTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

        loadModel(bikeMoveTG, fwTG, rwTG);
        
        fwCenter = getGroupCenter(fwTG);
        rwCenter = getGroupCenter(rwTG);

        objRoot.addChild(bikeMoveTG);

        BicycleBehavior behavior = new BicycleBehavior(bikeMoveTG, fwTG, rwTG, fwCenter, rwCenter, canvas);
        behavior.setSchedulingBounds(bounds);
        objRoot.addChild(behavior);

        return objRoot;
    }

    private Node createGround() {
        QuadArray plane = new QuadArray(4, QuadArray.COORDINATES | QuadArray.TEXTURE_COORDINATE_2);
        
        float size = 100.0f;
        plane.setCoordinate(0, new Point3f(-size, -1f, size));
        plane.setCoordinate(1, new Point3f(size, -1f, size));
        plane.setCoordinate(2, new Point3f(size, -1f, -size));
        plane.setCoordinate(3, new Point3f(-size, -1f, -size));

        float texScale = 20.0f;
        plane.setTextureCoordinate(0, 0, new TexCoord2f(0f, 0f));
        plane.setTextureCoordinate(0, 1, new TexCoord2f(texScale, 0f));
        plane.setTextureCoordinate(0, 2, new TexCoord2f(texScale, texScale));
        plane.setTextureCoordinate(0, 3, new TexCoord2f(0f, texScale));

        Appearance app = new Appearance();
        TextureLoader texLoader = new TextureLoader("res/ground.jpg", "RGB", canvas);
        Texture texture = texLoader.getTexture();
        
        if (texture != null) {
            texture.setBoundaryModeS(Texture.WRAP);
            texture.setBoundaryModeT(Texture.WRAP);
            app.setTexture(texture);
        } else {
            ColoringAttributes ca = new ColoringAttributes(new Color3f(0.1f, 0.1f, 0.1f), ColoringAttributes.SHADE_FLAT);
            app.setColoringAttributes(ca);
        }

        return new Shape3D(plane, app);
    }

    private void loadModel(TransformGroup mainTG, TransformGroup fwTG, TransformGroup rwTG) {
        int flags = ObjectFile.RESIZE | ObjectFile.STRIPIFY | ObjectFile.TRIANGULATE;
        ObjectFile loader = new ObjectFile(flags);
        
        loader.setBasePath("res/");
        
        Scene scene = null;
        try {
            scene = loader.load("res/mountainbike.obj");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        Hashtable namedObjects = scene.getNamedObjects();
        Enumeration keys = namedObjects.keys();

        while (keys.hasMoreElements()) {
            String name = (String) keys.nextElement();
            Object obj = namedObjects.get(name);

            if (obj instanceof Shape3D) {
                Shape3D part = (Shape3D) obj;
                Group parent = (Group) part.getParent();
                if (parent != null) {
                    parent.removeChild(part);
                }

                if (name.contains("front_wheel")) {
                    fwTG.addChild(part);
                } else if (name.contains("rear_wheel")) {
                    rwTG.addChild(part);
                } else {
                    mainTG.addChild(part);
                }
            }
        }
        mainTG.addChild(fwTG);
        mainTG.addChild(rwTG);
    }

    private Vector3f getGroupCenter(TransformGroup tg) {
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, minZ = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE, maxZ = -Double.MAX_VALUE;
        boolean found = false;

        for (int i = 0; i < tg.numChildren(); i++) {
            Node child = tg.getChild(i);
            if (child instanceof Shape3D) {
                Bounds b = ((Shape3D) child).getBounds();
                if (b != null) {
                    BoundingBox bbox = new BoundingBox(b);
                    Point3d lower = new Point3d();
                    Point3d upper = new Point3d();
                    bbox.getLower(lower);
                    bbox.getUpper(upper);

                    minX = Math.min(minX, lower.x);
                    minY = Math.min(minY, lower.y);
                    minZ = Math.min(minZ, lower.z);
                    maxX = Math.max(maxX, upper.x);
                    maxY = Math.max(maxY, upper.y);
                    maxZ = Math.max(maxZ, upper.z);
                    found = true;
                }
            }
        }

        if (found) {
            return new Vector3f((float) ((minX + maxX) / 2), (float) ((minY + maxY) / 2), (float) ((minZ + maxZ) / 2));
        }
        return new Vector3f(0, 0, 0);
    }

    public static void main(String[] args) {
        new App();
    }
}
