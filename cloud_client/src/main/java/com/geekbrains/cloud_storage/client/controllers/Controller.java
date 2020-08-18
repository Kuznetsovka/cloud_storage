package com.geekbrains.cloud_storage.client.controllers;

import com.geekbrains.cloud_storage.client.Network;
import com.geekbrains.common_files.common.AppModel;
import com.geekbrains.common_files.common.Config;
import com.geekbrains.common_files.common.ProtoFileSender;
import com.geekbrains.common_files.common.SENDER;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;

import static javafx.application.Platform.runLater;

public class Controller implements Initializable, Config {

    public void setStage(Stage stage) {
        clientFilesPath = "C:\\Users\\" + System.getProperty ("user.name") + "\\Downloads";
        this.stage = stage;
    }
    String clientFilesPath; //TODO Исправить нулевой путь
    private static Stage stage;
    @FXML
    public Button btnUpload;
    @FXML
    public Button btnDownload;

    @FXML
    public Button btnConnect;
    private AppModel model;
    protected static String nameFile="";
    Alert noConnect = new Alert(Alert.AlertType.INFORMATION, "Нет соединения!", ButtonType.OK);
    @FXML
    private TextField tfLogin;

    @FXML
    private TextField tfPassword;

    @FXML
    private Label infoField;
    private boolean isConnect = false;
    @FXML
    private Button btnDisconnect;

    public Controller(AppModel model) {
        this.model = model;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnUpload.setMaxWidth(Double.MAX_VALUE);
        btnDownload.setMaxWidth (Double.MAX_VALUE);
        model.textNameFile ().addListener ((obs, oldText, newText) -> {
            nameFile = newText;
            infoField.setText ("Выбран файл: " + Paths.get (nameFile).toString ());
        });
        model.textPathSelected ().addListener ((obs, oldText, newText) -> {
            clientFilesPath = newText;
            if (isConnect)
                Network.getHandle ().setClientFilesPath(clientFilesPath);
            infoField.setText ("Выбран файл: " + Paths.get (clientFilesPath, nameFile).toString ());
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
            waitCursor ();
            CountDownLatch networkStarter = new CountDownLatch (1);
            new Thread (() -> Network.getInstance ().start (this, networkStarter, tfLogin.getText (), tfPassword.getText (),model)).start ();
            try {
                networkStarter.await ();
            } catch (InterruptedException e) {
                e.printStackTrace ();
            }
            if (isConnect) {
                infoField.setText ("Соединение установлено");
                btnConnect.setVisible (false);
                btnDisconnect.setVisible(true);
            } else {
                noConnect.show ();
            }
            notWaitCursor ();
        }
    }

    public void uploadCommandNIO(ActionEvent actionEvent) {
        if (isConnect) {
            try {
                waitCursor ();
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
                notWaitCursor ();
            } catch (IOException e) {
                e.printStackTrace ();
            }
        } else {
            noConnect.show ();
        }
    }

    public void downloadCommandNIO(ActionEvent actionEvent) {
        if (isConnect) {
            waitCursor();
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
            notWaitCursor ();
        } else {
            noConnect.show();
        }
    }

    public void clear(MouseEvent mouseEvent) {
        TextField tf = (TextField) mouseEvent.getSource ();
        if (mouseEvent.getSource ().equals (tf))
            tf.setText ("");
    }

    public void disConnect(ActionEvent actionEvent) {
        if (isConnect)
            Network.getInstance().getCurrentChannel ().close ();
        btnDisconnect.setVisible(false);
        btnConnect.setVisible (true);
    }

    void waitCursor(){
        new Thread (()-> runLater(() ->{
                    stage.getScene ().setCursor (Cursor.WAIT);
                }
        ));
    }

    void notWaitCursor(){
        new Thread (()-> runLater(() ->{
                    stage.getScene().setCursor(Cursor.DEFAULT);
                }
        ));
    }
}
