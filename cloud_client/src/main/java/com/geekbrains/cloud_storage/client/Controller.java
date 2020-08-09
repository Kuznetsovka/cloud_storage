package com.geekbrains.cloud_storage.client;

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
    protected static int id;
    private AppModel model;
    protected static String nameFile;
    protected final String clientFilesPath = "./common/src/main/resources/clientFiles/user";
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
        model.textProperty ().addListener ((obs, oldText, newText) -> {
            nameFile = newText;
            secondField.setText ("Выбран файл: " + newText);
        });
        id++;
    }


    public void exitAction (ActionEvent actionEvent){
        Network.getInstance().getCurrentChannel ().close ();
        Platform.exit ();
    }

    public void connect (ActionEvent actionEvent){
        if (!isConnect) {
            CountDownLatch networkStarter = new CountDownLatch (1);
            new Thread (() -> Network.getInstance ().start (networkStarter)).start ();
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
                ProtoFileSender.sendFile (Paths.get (clientFilesPath + id, nameFile), id, SENDER.CLIENT, true, Network.getInstance ().getCurrentChannel (), future -> {
                    if (!future.isSuccess ()) {
                        future.cause ().printStackTrace ();
                        Network.getInstance ().stop ();
                    }
                    if (future.isSuccess ()) {
                        System.out.println ("Файл успешно передан");
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
                ProtoFileSender.sendFile (Paths.get (clientFilesPath + id, nameFile), id, SENDER.CLIENT, false, Network.getInstance ().getCurrentChannel (), future -> {
                    if (!future.isSuccess ()) {
                        future.cause ().printStackTrace ();
                        Network.getInstance ().stop ();
                    }
                    if (future.isSuccess ()) {
                        System.out.println ("Данные скачаны!");
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
