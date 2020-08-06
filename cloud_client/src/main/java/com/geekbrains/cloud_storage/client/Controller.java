package com.geekbrains.cloud_storage.client;

import com.geekbrains.common.common.ProtoFileSender;
import com.geekbrains.common.common.SENDER;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;

public class Controller implements Initializable {

    @FXML
    public Button upload;
    private Socket socket;
    private DataInputStream is;
    private DataOutputStream os;
    protected static int id;
    private int countBufferBytes = 1024;
    byte[] bytes = new byte[1024];
    private AppModel model;
    protected static String nameFile;
    protected final String clientFilesPath = "./common/src/main/resources/clientFiles/user";

    @FXML
    private Label secondField;

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

    private long readLong() throws IOException {
        int countBytes = 8;
        StringBuilder bufferStr = new StringBuilder ();
        byte[] buffer = new byte[countBytes];
        while (is.available () > 0) {
            is.readFully (buffer);
            String str = new String (buffer);
            bufferStr.append (str);
        }
        return Long.getLong (String.valueOf (bufferStr));
    }

    private String bytesToStr(byte[] bytes) throws IOException {
        is.readFully (bytes);
        return new String (bytes);
    }


    public void exitAction (ActionEvent actionEvent){
        Network.getInstance().getCurrentChannel ().close ();
        Platform.exit ();
    }

    public void connect (ActionEvent actionEvent){
        CountDownLatch networkStarter = new CountDownLatch(1);
        new Thread(() -> Network.getInstance().start(networkStarter)).start();
        try {
            networkStarter.await();
        } catch (InterruptedException e) {
            e.printStackTrace ();
        }
    }

    public void uploadCommandNIO(ActionEvent actionEvent) {
        try {
            ProtoFileSender.sendFile(Paths.get(clientFilesPath + id, nameFile),id, SENDER.CLIENT,true, Network.getInstance().getCurrentChannel(), future -> {
                if (!future.isSuccess()) {
                    future.cause().printStackTrace();
                    Network.getInstance().stop();
                }
                if (future.isSuccess()) {
                    System.out.println("Файл успешно передан");
                }
            });
        } catch (IOException e) {
            e.printStackTrace ();
        }
    }

    public void downloadCommandNIO(ActionEvent actionEvent) {
        try {
            ProtoFileSender.sendFile(Paths.get(clientFilesPath + id, nameFile),id, SENDER.CLIENT,false, Network.getInstance().getCurrentChannel(), future -> {
                if (!future.isSuccess()) {
                    future.cause().printStackTrace();
                    Network.getInstance().stop();
                }
                if (future.isSuccess()) {
                    System.out.println("Данные переданы серверу!");
                }
            });
        } catch (IOException e) {
            e.printStackTrace ();
        }
    }
}
