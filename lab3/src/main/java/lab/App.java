package lab;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class App extends Application {

    private ImageView fishView;
    private ImageView mapView;
    private PixelReader mapReader;
    
    private double x = 400, y = 300;
    private double dx = -2.0, dy = 1.5;
    
    private double angle = 0;
    private double scale = 1.0;
    private double dScale = 0.005;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Image fishImage = new Image(getClass().getResource("/lab/fish.png").toExternalForm());
        fishView = new ImageView(fishImage);
        fishView.setFitWidth(200);
        fishView.setPreserveRatio(true);

        Image mapImage = new Image(getClass().getResource("/lab/map.bmp").toExternalForm());
        mapView = new ImageView(mapImage);
        mapReader = mapImage.getPixelReader();
        
        int mapWidth = (int) mapImage.getWidth();
        int mapHeight = (int) mapImage.getHeight();

        Group root = new Group(mapView, fishView);
        Scene scene = new Scene(root, mapWidth, mapHeight, Color.AZURE);

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                double w = fishView.getFitWidth();
                double h = (fishView.getFitHeight() == 0) ? 100 : fishView.getFitHeight();

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
                    int cx = corner[0];
                    int cy = corner[1];

                    if (cx >= 1 && cx < mapWidth - 1 && cy >= 1 && cy < mapHeight - 1) {
                        if (mapReader.getColor(cx, cy).getBrightness() < 0.1) {
                            collisionDetected = true;
                            colX = cx; colY = cy;
                            break;
                        }
                    } else {
                        collisionDetected = true;
                        colX = cx; colY = cy;
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
                            double nx = gradX / len;
                            double ny = gradY / len;

                            double dot = dx * nx + dy * ny;
                            dx = dx - 2 * dot * nx;
                            dy = dy - 2 * dot * ny;
                        } else {
                            dx = -dx; dy = -dy;
                        }
                    } else {
                        dx = -dx; dy = -dy;
                    }

                    fishView.setScaleX(dx > 0 ? -1 : 1); 
                    
                } else {
                    x = nextX;
                    y = nextY;
                }

                fishView.setTranslateX(x - w/2);
                fishView.setTranslateY(y - h/2);

                angle += 0.05;
                fishView.setRotate(Math.sin(angle) * 15);
                
                scale += dScale;
                if (scale > 1.1 || scale < 0.9) dScale = -dScale;
                fishView.setScaleY(scale);
            }
        }.start();

        primaryStage.setTitle("Fish Animation");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
