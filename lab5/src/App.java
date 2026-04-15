import com.sun.j3d.loaders.Scene;
import com.sun.j3d.loaders.objectfile.ObjectFile;
import com.sun.j3d.utils.image.TextureLoader;
import com.sun.j3d.utils.universe.SimpleUniverse;
import javax.media.j3d.*;
import javax.swing.*;
import javax.vecmath.*;
import java.awt.*;
import java.util.Map;

public class App extends JFrame {
    private Canvas3D canvas;
    private SimpleUniverse universe;
    private BranchGroup root;
    private TransformGroup wholeCar;

    public App() {
        configureWindow();
        configureCanvas();
        configureUniverse();
        addBackground();
        addLightToUniverse();
        loadCarModel();
        
        CarAnimation anim = new CarAnimation(wholeCar);
        this.addKeyListener(anim);
        canvas.addKeyListener(anim);
        canvas.setFocusable(true);
        canvas.requestFocus();

        setVisible(true);
    }

    private void configureWindow() {
        setTitle("Car Animation");
        setSize(1800, 1200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
    }

    private void configureCanvas() {
        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
        canvas = new Canvas3D(config);
        add(canvas, BorderLayout.CENTER);
    }

    private void configureUniverse() {
        root = new BranchGroup();
        universe = new SimpleUniverse(canvas);
        universe.getViewingPlatform().setNominalViewingTransform();
    }

    private void addLightToUniverse() {
        BoundingSphere bounds = new BoundingSphere(new Point3d(0, 0, 0), 100);
        DirectionalLight light = new DirectionalLight(new Color3f(1f, 1f, 1f), new Vector3f(-1f, -1f, -1f));
        light.setInfluencingBounds(bounds);
        root.addChild(light);
        
        AmbientLight ambient = new AmbientLight(new Color3f(0.5f, 0.5f, 0.5f));
        ambient.setInfluencingBounds(bounds);
        root.addChild(ambient);
    }

    private void addBackground() {
        TextureLoader loader = new TextureLoader("res/textures/road.jpg", canvas);
        Background bg = new Background(loader.getImage());
        bg.setImageScaleMode(Background.SCALE_FIT_ALL);
        bg.setApplicationBounds(new BoundingSphere(new Point3d(0, 0, 0), 100));
        root.addChild(bg);
    }

    private void loadCarModel() {
        wholeCar = new TransformGroup();
        wholeCar.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        
        ObjectFile loader = new ObjectFile(ObjectFile.RESIZE);
        Scene scene = null;
        try {
            scene = loader.load("res/objects/car.obj");
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        if (scene != null) {
            Map<String, Shape3D> nameMap = scene.getNamedObjects();

            applyTexture(nameMap.get("body"), "res/textures/body_diffuse.png");
            applyTexture(nameMap.get("tire1"), "res/textures/tire_diffuse.png");
            applyTexture(nameMap.get("tire2"), "res/textures/tire_diffuse.png");
            applyGlassMaterial(nameMap.get("glass"), "res/textures/glass_diffuse.png");

            wholeCar.addChild(scene.getSceneGroup());
            root.addChild(wholeCar);
            universe.addBranchGraph(root);
        }
    }

    private void applyTexture(Shape3D shape, String texPath) {
        if (shape == null) return;

        Appearance app = new Appearance();
        
        TextureLoader texLoader = new TextureLoader(texPath, canvas);
        Texture texture = texLoader.getTexture();
        app.setTexture(texture);

        Material mat = new Material();
        mat.setLightingEnable(true);
        app.setMaterial(mat);

        TextureAttributes texAttr = new TextureAttributes();
        texAttr.setTextureMode(TextureAttributes.MODULATE);
        app.setTextureAttributes(texAttr);

        shape.setAppearance(app);
    }

    private void applyGlassMaterial(Shape3D shape, String texPath) {
        if (shape == null) return;
        
        Appearance app = new Appearance();

        TextureLoader texLoader = new TextureLoader(texPath, canvas);
        Texture texture = texLoader.getTexture();
        app.setTexture(texture);

        Material mat = new Material();
        mat.setAmbientColor(new Color3f(0.1f, 0.1f, 0.1f));
        mat.setDiffuseColor(new Color3f(0.5f, 0.8f, 1.0f));
        mat.setSpecularColor(new Color3f(1.0f, 1.0f, 1.0f));
        mat.setShininess(128f);
        mat.setLightingEnable(true);
        app.setMaterial(mat);

        TextureAttributes texAttr = new TextureAttributes();
        texAttr.setTextureMode(TextureAttributes.MODULATE);
        app.setTextureAttributes(texAttr);

        shape.setAppearance(app);
    }

    public static void main(String[] args) {
        new App();
    }
}
