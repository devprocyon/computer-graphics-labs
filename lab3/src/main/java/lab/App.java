package lab;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;

public class App extends Application {

    private Group fishGroup;
    private ImageView mapView;
    private PixelReader mapReader;
    
    private double x = 400, y = 300;
    private double dx = -2.0, dy = 1.5;
    
    private double angle = 0;
    private double scale = 1.0;
    private double dScale = 0.005;

    @Override
    public void start(Stage primaryStage) throws Exception {
        fishGroup = createFish();
    
        fishGroup.setScaleX(1.0); 
        fishGroup.setScaleY(1.0);

        Image mapImage = new Image(getClass().getResource("/lab/map.bmp").toExternalForm());
        mapView = new ImageView(mapImage);
        mapReader = mapImage.getPixelReader();
        
        int mapWidth = (int) mapImage.getWidth();
        int mapHeight = (int) mapImage.getHeight();

        Group root = new Group(mapView, fishGroup);
        Scene scene = new Scene(root, mapWidth, mapHeight, Color.AZURE);

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                double w = fishGroup.getBoundsInLocal().getWidth() * Math.abs(fishGroup.getScaleX());
                double h = fishGroup.getBoundsInLocal().getHeight() * Math.abs(fishGroup.getScaleY());

                double nextX = x + dx;
                double nextY = y + dy;

                int[][] corners = {
                    {(int)(nextX - w/2), (int)(nextY - h/2)},
                    {(int)(nextX + w/2), (int)(nextY - h/2)},
                    {(int)(nextX - w/2), (int)(nextY + h/2)},
                    {(int)(nextX + w/2), (int)(nextY + h/2)}
                };

                boolean collisionDetected = false;
                int colX = 0, colY = 0;

                for (int[] corner : corners) {
                    int cx = corner[0]; int cy = corner[1];
                    if (cx >= 1 && cx < mapWidth - 1 && cy >= 1 && cy < mapHeight - 1) {
                        if (mapReader.getColor(cx, cy).getBrightness() < 0.1) {
                            collisionDetected = true; colX = cx; colY = cy;
                            break;
                        }
                    } else {
                        collisionDetected = true; colX = cx; colY = cy;
                        break;
                    }
                }

                if (collisionDetected) {
                    if (colX >= 1 && colX < mapWidth - 1 && colY >= 1 && colY < mapHeight - 1) {
                        double gradX = mapReader.getColor(colX + 1, colY).getBrightness() 
                                    - mapReader.getColor(colX - 1, colY).getBrightness();
                        double gradY = mapReader.getColor(colX, colY + 1).getBrightness() 
                                    - mapReader.getColor(colX, colY - 1).getBrightness();
                        double len = Math.sqrt(gradX * gradX + gradY * gradY);
                        
                        if (len > 0) {
                            double nx = gradX / len; double ny = gradY / len;
                            double dot = dx * nx + dy * ny;
                            dx = dx - 2 * dot * nx;
                            dy = dy - 2 * dot * ny;
                        } else { dx = -dx; dy = -dy; }
                    } else { dx = -dx; dy = -dy; }

                    fishGroup.setScaleX(dx > 0 ? -1 : 1); 
                    
                } else {
                    x = nextX;
                    y = nextY;
                }

                fishGroup.setTranslateX(x);
                fishGroup.setTranslateY(y);

                angle += 0.05;
                fishGroup.setRotate(Math.sin(angle) * 15);
                
                scale += dScale;
                if (scale > 1.1 || scale < 0.9) dScale = -dScale;
                fishGroup.setScaleY(scale);
                fishGroup.setScaleX((dx > 0 ? -1 : 1) * (1.0 + Math.abs(1.0 - scale)));
            }
        }.start();

        primaryStage.setTitle("Fish Animation");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Group createFish() {
        Group fishGroup = new Group();

        Color bodyColor = Color.web("#F8E33F");
        Color finColor = Color.web("#D1652C");
        Color outlineColor = Color.BLACK;
        
        Ellipse mainBody = new Ellipse(-10, 0, 100, 80);
        mainBody.setFill(bodyColor);
        mainBody.setStroke(outlineColor);
        mainBody.setStrokeWidth(2.5);

        Arc topLip = new Arc(-100, 10, 20, 15, 125, 120);
        topLip.setType(ArcType.CHORD);
        topLip.setFill(bodyColor);
        topLip.setStroke(outlineColor);
        topLip.setStrokeWidth(2.5);

        Arc bottomLip = new Arc(-95, 28, 20, 15, 140, 120);
        bottomLip.setType(ArcType.CHORD);
        bottomLip.setFill(bodyColor);
        bottomLip.setStroke(outlineColor);
        bottomLip.setStrokeWidth(2.5);

        Polygon tail = new Polygon();
        tail.getPoints().addAll(new Double[]{
            85.0, 0.0,
            140.0, -65.0,
            115.0, 0.0,
            140.0, 65.0
        });
        tail.setFill(finColor);
        tail.setStroke(outlineColor);
        tail.setStrokeWidth(2);

        Polygon dorsalFin = new Polygon();
        dorsalFin.getPoints().addAll(new Double[]{
            -40.0, -74.0, 
            40.0, -78.0, 
            25.0, -115.0, 
            -20.0, -110.0
        });
        dorsalFin.setFill(finColor);
        dorsalFin.setStroke(outlineColor);
        dorsalFin.setStrokeWidth(2);

        Polygon lowerFin = new Polygon();
        lowerFin.getPoints().addAll(new Double[]{
            -30.0, 75.0, 
            30.0, 80.0, 
            20.0, 120.0, 
            -15.0, 118.0
        });
        lowerFin.setFill(finColor);
        lowerFin.setStroke(outlineColor);
        lowerFin.setStrokeWidth(2);
        
        Polygon pectoralFin = new Polygon();
        pectoralFin.getPoints().addAll(new Double[]{
            -20.0, 40.0, 
            25.0, 45.0, 
            15.0, 75.0, 
            -10.0, 70.0
        });
        pectoralFin.setFill(finColor);
        pectoralFin.setStroke(outlineColor);
        pectoralFin.setStrokeWidth(2);

        Circle eyeBg = new Circle(-60, -20, 18, Color.WHITE);
        eyeBg.setStroke(Color.BLACK);
        eyeBg.setStrokeWidth(1.5);
        
        Circle pupil = new Circle(-63, -19, 9, Color.BLACK);
        
        Circle highlight = new Circle(-66, -22, 3, Color.WHITE);

        Group eyelashes = new Group();
        for (int i = 0; i < 5; i++) {
            double startAngle = 60 + i * 20;
            Arc lash = new Arc(-60, -20, 22, 22, startAngle, 10);
            lash.setType(ArcType.OPEN);
            lash.setStroke(Color.BLACK);
            lash.setStrokeWidth(2);
            lash.setFill(null);
            eyelashes.getChildren().add(lash);
        }

        Group scalesGroup = new Group();
        
        Group headDivider = new Group();
        for (int i = 0; i < 10; i++) {
            Circle dot = new Circle(-25, -60 + i * 13, 2, Color.web("#E0E0E0", 0.8));
            headDivider.getChildren().add(dot);
        }

        Color scaleViolet = Color.web("#9A82C7");
        Color scaleGreen = Color.web("#75CE9F");
        Color scalePink = Color.web("#E685A9");

        scalesGroup.getChildren().add(createScaleShape(-5, -45, scaleViolet));
        scalesGroup.getChildren().add(createScaleShape(15, -45, scaleViolet));

        scalesGroup.getChildren().add(createScaleShape(-10, 0, scaleGreen));
        scalesGroup.getChildren().add(createScaleShape(10, 0, scaleGreen));
        scalesGroup.getChildren().add(createScaleShape(30, 0, scaleGreen));

        scalesGroup.getChildren().add(createScaleShape(-5, 45, scalePink));
        scalesGroup.getChildren().add(createScaleShape(15, 45, scalePink));

        fishGroup.getChildren().addAll(
            dorsalFin, lowerFin, tail,
            mainBody, bottomLip, topLip,
            headDivider, scalesGroup,
            eyeBg, pupil, highlight, eyelashes,
            pectoralFin
        );

        return fishGroup;
    }

    private Path createScaleShape(double x, double y, Color color) {
        Path path = new Path();
        
        MoveTo moveTo = new MoveTo(x, y);
        ArcTo arcTo = new ArcTo(12, 12, 0, x + 15, y, true, false);
        LineTo lineTo = new LineTo(x + 15, y + 20);
        ArcTo arcToBottom = new ArcTo(8, 8, 0, x, y + 20, false, true);
        ClosePath closePath = new ClosePath();
        
        path.getElements().addAll(moveTo, arcTo, lineTo, arcToBottom, closePath);
        path.setFill(color);
        path.setStroke(Color.web("#FFFFFF", 0.6));
        path.setStrokeWidth(1.5);
        path.getStrokeDashArray().addAll(3.0, 3.0);

        return path;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
