module com.example.motorcontrol {
    requires javafx.controls;
    requires javafx.fxml;
    requires purejavacomm;
    requires javafx.web;
    requires javafx.graphics;
    requires opencv;
    requires ai.djl.api;


    opens com.example.motorcontrol to javafx.fxml;
    exports com.example.motorcontrol;
}