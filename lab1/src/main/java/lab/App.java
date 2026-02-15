package lab;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Setting up basic parameters
        double sceneWidth = 800.0;
        double sceneHeight = 800.0;
        double centerX = sceneWidth / 2;
        double centerY = sceneHeight / 2;
        double maxRadius = 350.0;
        double radiusStep = 60.0;
        double crossSize = 10.0;

        Group root = new Group();

        // --- Blue circles (outer) ---
        Circle circle1 = createCircle(centerX, centerY, maxRadius, Color.BLUE);
        Circle circle2 = createCircle(centerX, centerY, maxRadius - radiusStep, Color.BLUE);

        // --- Red circles (middle) ---
        Circle circle3 = createCircle(centerX, centerY, maxRadius - (radiusStep * 2), Color.RED);
        Circle circle4 = createCircle(centerX, centerY, maxRadius - (radiusStep * 3), Color.RED);

        // --- Yellow circles (central) ---
        Circle circle5 = createCircle(centerX, centerY, maxRadius - (radiusStep * 4), Color.YELLOW);
        Circle circle6 = createCircle(centerX, centerY, maxRadius - (radiusStep * 5), Color.YELLOW);

        Line vLine = new Line(centerX, centerY - crossSize, centerX, centerY + crossSize);
        Line hLine = new Line(centerX - crossSize, centerY, centerX + crossSize, centerY);
        
        vLine.setStroke(Color.BLACK);
        hLine.setStroke(Color.BLACK);

        root.getChildren().addAll(circle1, circle2, circle3, circle4, circle5, circle6, vLine, hLine);

        Scene scene = new Scene(root, sceneWidth, sceneHeight, Color.BLACK);

        stage.setTitle("Target Drawing");
        stage.setScene(scene);
        stage.show();
    }

    private Circle createCircle(double x, double y, double r, Color color) {
        Circle circle = new Circle(x, y, r);
        circle.setFill(color);
        circle.setStroke(Color.BLACK);
        circle.setStrokeWidth(1.0);
        return circle;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
