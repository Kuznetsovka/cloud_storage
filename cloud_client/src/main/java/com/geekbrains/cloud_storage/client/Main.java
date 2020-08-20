package com.geekbrains.cloud_storage.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/client_GUI.fxml"));
        Scene scene = new Scene(root);
        primaryStage.setTitle("Cloudy");
        primaryStage.setScene(scene);
        scene.getStylesheets().add("MyStyle.css");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
