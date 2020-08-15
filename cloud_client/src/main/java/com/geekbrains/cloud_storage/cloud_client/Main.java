package com.geekbrains.cloud_storage.cloud_client;

import com.geekbrains.cloud_storage.cloud_client.controllers.ClientController;
import com.geekbrains.cloud_storage.cloud_client.controllers.Controller;
import com.geekbrains.cloud_storage.common.AppModel;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Callback;


public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        AppModel model = new AppModel ();
        Callback<Class<?>, Object> controllerFactory = type -> {
            if (type == Controller.class) {
                return new Controller(model);
            } else if (type == ClientController.class) {
                return new ClientController(model);
            } else {
                try {
                    return type.newInstance() ;
                } catch (Exception exc) {
                    System.err.println("Could not create controller for " + type.getName());
                    throw new RuntimeException(exc);
                }
            }
        };

        FXMLLoader firstLoader = new FXMLLoader(getClass().getResource("/sample.fxml"));
        firstLoader.setControllerFactory (controllerFactory);
        Parent firstUI = firstLoader.load();

        FXMLLoader secondLoader = new FXMLLoader(getClass().getResource("/client_panel.fxml"));
        secondLoader.setControllerFactory (controllerFactory);
        Parent secondUI = secondLoader.load();

        primaryStage.setTitle("Cloudy");
        primaryStage.setScene(new Scene (firstUI));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
