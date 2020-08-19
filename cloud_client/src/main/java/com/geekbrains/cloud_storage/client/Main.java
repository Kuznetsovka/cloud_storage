package com.geekbrains.cloud_storage.client;

import com.geekbrains.cloud_storage.client.controllers.Controller;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Main extends Application {
    Stage primaryStage;

    private Controller controller;

    @Override
    public void start(Stage primaryStage) throws Exception{
//        AppModel model = new AppModel ();
//        Callback<Class<?>, Object> controllerFactory = type -> {
//            if (type == Controller.class) {
//                controller = new Controller(model);
//                return controller;
//            } else if (type == ClientController.class) {
//                return new ClientController(model);
//            } else if (type == ServerController.class) {
//                return new ServerController(model);
//            } else {
//                try {
//                    return type.newInstance() ;
//                } catch (Exception exc) {
//                    System.err.println("Could not create controller for " + type.getName());
//                    throw new RuntimeException(exc);
//                }
//            }
//
//        };
//        FXMLLoader firstLoader = new FXMLLoader(getClass().getResource("/sample.fxml"));
//        firstLoader.setControllerFactory (controllerFactory);
//        Parent firstUI = firstLoader.load();
//        Scene scene =new Scene(firstUI);
//
//        FXMLLoader secondLoader = new FXMLLoader(getClass().getResource("/client_panel.fxml"));
//        secondLoader.setControllerFactory (controllerFactory);
//        Parent secondUI = secondLoader.load();
//
//        FXMLLoader thirdLoader = new FXMLLoader(getClass().getResource("/server_panel.fxml"));
//        thirdLoader.setControllerFactory (controllerFactory);
//        Parent thirdUI = thirdLoader.load();
//        primaryStage.setTitle("Cloudy");

        Parent root = FXMLLoader.load(getClass().getResource("/sample.fxml"));
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
