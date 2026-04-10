module lab {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    opens lab to javafx.fxml;
    exports lab;
}
