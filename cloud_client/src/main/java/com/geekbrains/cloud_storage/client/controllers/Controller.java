package com.geekbrains.cloud_storage.client.controllers;

import com.geekbrains.common.common.AppModel;
import com.geekbrains.cloud_storage.client.Network;
import com.geekbrains.common.common.ProtoFileSender;
import com.geekbrains.common.common.SENDER;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.io.*;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;

public class Controller implements Initializable {

    @FXML
    public Button upload;
    private AppModel model;
    protected static String nameFile="";
    protected static String clientFilesPath = "";
    Alert noConnect = new Alert(Alert.AlertType.INFORMATION, "Нет соединения!", ButtonType.OK);

    @FXML
    private TextField tfLogin;

    @FXML
    private TextField tfPassword;

    @FXML
    private Label secondField;
    private boolean isConnect = false;

    public Controller(AppModel model) {
        this.model = model;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        model.textNameFile ().addListener ((obs, oldText, newText) -> {
            nameFile = newText;
            secondField.setText ("Выбран файл: " + Paths.get (clientFilesPath, nameFile).toString ());
        });
        model.textPathSelected ().addListener ((obs, oldText, newText) -> {
            clientFilesPath = newText;
            Network.getHandle ().setClientFilesPath(clientFilesPath);
            secondField.setText ("Выбран файл: " + Paths.get (clientFilesPath, nameFile).toString ());
        });
    }


    public void exitAction (ActionEvent actionEvent){
        Network.getInstance().getCurrentChannel ().close ();
        Platform.exit ();
    }

    public void connect (ActionEvent actionEvent){
        if (!isConnect) {
            CountDownLatch networkStarter = new CountDownLatch (1);
            new Thread (() -> Network.getInstance ().start (networkStarter,tfLogin.getText (),tfPassword.getText ())).start ();
            try {
                networkStarter.await ();
            } catch (InterruptedException e) {
                e.printStackTrace ();
            }
            isConnect = Network.isConnect ();
        }
    }

    public void uploadCommandNIO(ActionEvent actionEvent) {
        if (Network.isConnect ()) {
            try {
                ProtoFileSender.sendFile (Paths.get (clientFilesPath, nameFile), SENDER.CLIENT, true, Network.getInstance ().getCurrentChannel (), future -> {
                    if (!future.isSuccess ()) {
                        future.cause ().printStackTrace ();
                        Network.getInstance ().stop ();
                    }
                    if (future.isSuccess ()) {
                        System.out.println ("Файл успешно передан");
                        Thread.sleep (500);
                        model.setText4 (tfLogin.getText ());
                    }
                });
            } catch (IOException e) {
                e.printStackTrace ();
            }
        } else {
            noConnect.show ();
        }
    }

    public void downloadCommandNIO(ActionEvent actionEvent) {
        if (Network.isConnect ()) {
            try {
                Network.getHandle ().setFileName (nameFile);
                ProtoFileSender.sendFile (Paths.get (clientFilesPath, nameFile), SENDER.CLIENT, false, Network.getInstance ().getCurrentChannel (), future -> {
                    if (!future.isSuccess ()) {
                        future.cause ().printStackTrace ();
                        Network.getInstance ().stop ();
                    }
                    if (future.isSuccess ()) {
                        System.out.println ("Файл скачался!");
                        Thread.sleep (500);
                        model.setText3 (clientFilesPath);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace ();
            }
        } else {
            noConnect.show();
        }
    }

    public void autorize(ActionEvent actionEvent) {
    }

    public void clear(MouseEvent mouseEvent) {
        TextField tf = (TextField) mouseEvent.getSource ();
        if (mouseEvent.getSource ().equals (tf))
            tf.setText ("");
    }
}
