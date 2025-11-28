package com.example.motorcontrol;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class VideoView extends Application {

    private final ImageView imageView = new ImageView();
    VideoService videoService;

    @Override
    public void start(Stage stage) {
        imageView.setFitWidth(800);
        imageView.setFitHeight(600);
        imageView.setPreserveRatio(true);

        StackPane root = new StackPane(imageView);
        Scene scene = new Scene(root, 850, 650);
        stage.setScene(scene);
        stage.setTitle("ESP32-CAM Stream");
        stage.show();

        startStream();
    }

    private void startStream() {
         videoService = new VideoService(imageView);
    }



    public static void main(String[] args) {
        launch(args);
    }
}