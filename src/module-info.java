module Cryptext {
    requires javafx.fxml;
    requires javafx.controls;
    requires javafx.graphics;
    requires java.sql;
    requires java.mail;
    opens application to javafx.fxml;
    exports application;

}