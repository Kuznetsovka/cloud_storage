package com.geekbrains.cloud_storage.client.controllers;

import com.geekbrains.cloud_storage.client.Network;
import com.geekbrains.common1.common.AppModel;
import com.geekbrains.common1.common.ProtoFileSender;
import com.geekbrains.common1.common.SENDER;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
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

import static com.geekbrains.common1.common.Config.SIGNAL_UPDATE;

public class Controller implements Initializable {

    @FXML
    public Button upload;

    @FXML
    public Button btnConnect;
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
            if (isConnect)
                Network.getHandle ().setClientFilesPath(clientFilesPath);
            secondField.setText ("Выбран файл: " + Paths.get (clientFilesPath, nameFile).toString ());
        });
    }


    public void exitAction (ActionEvent actionEvent){
        Network.getInstance().getCurrentChannel ().close ();
        Platform.exit ();
    }

    public void setConnect(boolean connect) {
        isConnect = connect;
    }

    public synchronized void connect (ActionEvent actionEvent) {
        if (!isConnect) {
            CountDownLatch networkStarter = new CountDownLatch (1);
            new Thread (() -> Network.getInstance ().start (this, networkStarter, tfLogin.getText (), tfPassword.getText (), model)).start ();
            try {
                networkStarter.await ();
            } catch (InterruptedException e) {
                e.printStackTrace ();
            }
            if (isConnect) {
                isConnect = true;
                secondField.setText ("Connect");
                btnConnect.setVisible (false);
            } else {
                noConnect.show ();
            }
        }
    }

    public void uploadCommandNIO(ActionEvent actionEvent) {
        if (isConnect) {
            try {
                ProtoFileSender.sendFile (Paths.get (clientFilesPath, nameFile), SENDER.CLIENT, true, Network.getInstance ().getCurrentChannel (), future -> {
                    if (!future.isSuccess ()) {
                        future.cause ().printStackTrace ();
                        Network.getInstance ().stop ();
                    }
                    if (future.isSuccess ()) {
                        System.out.println ("Файл успешно передан");
                        ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer (1);
                        buf.writeByte (SIGNAL_UPDATE);
                        Network.getInstance ().getCurrentChannel ().writeAndFlush (buf);
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
        if (isConnect) {
            try {
                Network.getHandle ().setFileName (nameFile);
                ProtoFileSender.sendFile (Paths.get (nameFile), SENDER.CLIENT, false, Network.getInstance ().getCurrentChannel (), future -> {
                    if (!future.isSuccess ()) {
                        future.cause ().printStackTrace ();
                        Network.getInstance ().stop ();
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
