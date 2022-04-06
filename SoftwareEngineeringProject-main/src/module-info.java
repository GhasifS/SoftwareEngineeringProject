module Cryptext {
    requires javafx.fxml;
    requires javafx.controls;
    requires javafx.graphics;
    opens application to javafx.fxml;
    exports application;
}