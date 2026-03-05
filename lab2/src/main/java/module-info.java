module lab {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    opens lab to javafx.fxml;
    exports lab;
}
