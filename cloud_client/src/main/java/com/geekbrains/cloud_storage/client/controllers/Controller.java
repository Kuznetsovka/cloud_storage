package com.geekbrains.cloud_storage.client.controllers;

import com.geekbrains.cloud_storage.client.Network;
import com.geekbrains.common_files.common.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;

import static com.geekbrains.cloud_storage.client.ProtoHandlerClient.*;

public class Controller implements Initializable, Config {

    @FXML
    public Cursor cursor;
    @FXML
    public VBox clientBox;
    @FXML
    public VBox serverBox;

    public static  String clientFilesPath = new String();
    @FXML
    public Button btnUpload;
    @FXML
    public Button btnDownload;

    @FXML
    public Button btnConnect;
    protected String nameFile;
    @FXML
    Alert noConnect = new Alert(Alert.AlertType.INFORMATION, "Нет соединения!", ButtonType.OK);
    Alert disConnect = new Alert(Alert.AlertType.INFORMATION, "Соединение разорвано!", ButtonType.OK);
    Alert noSelect = new Alert(Alert.AlertType.INFORMATION, "Ни один файл не выбран!", ButtonType.OK);
    @FXML
    private TextField tfLogin;

    @FXML
    private TextField tfPassword;

    @FXML
    private Label infoField;
    private boolean isConnect = false;
    @FXML
    private Button btnDisconnect;
    ClientController clientPanel;
    ServerController serverPanel;
    public static boolean busy = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnUpload.setMaxWidth(Double.MAX_VALUE);
        btnDownload.setMaxWidth (Double.MAX_VALUE);
        clientPanel = (ClientController) clientBox.getProperties().get("ctrl");
        serverPanel = (ServerController) serverBox.getProperties().get("ctrl");
        clientFilesPath = clientPanel.pathField.getText ();
        isUpdateServer.addListener ((observable, oldValue, newValue) -> {
            if (isUpdateServer.get ()) {
                serverPanel.updateListServer (listFileServer);
                isUpdateServer.setValue (false);
            }
        });
        isUpdateClient.addListener ((observable, oldValue, newValue) -> {
            if (isUpdateClient.get ()) {
                clientPanel.updateList (Paths.get (clientFilesPath));
                isUpdateClient.setValue (false);
            }
        });
    }


    public void exitAction (ActionEvent actionEvent){
        if (isConnect)
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
            new Thread (() -> Network.getInstance ().start (this, networkStarter, tfLogin.getText (),tfPassword.getText ())).start ();
            try {
                networkStarter.await ();
            } catch (InterruptedException e) {
                e.printStackTrace ();
            }
            if (isConnect) {
                infoField.setText ("Соединение установлено");
                MyLogger.logInfo ("Соединение с сервером установлено ");
                btnConnect.setVisible (false);
                btnDisconnect.setVisible(true);
            } else {
                noConnect.show ();
            }
            notWaitCursor ();
        }
    }

    public void upload(ActionEvent actionEvent) {
        if (isConnect && busy == false) {
            busy = true;
            try {
                if(!isSelectedFile(SENDER.CLIENT)) {
                    noSelect.show ();
                    return;
                }
                waitCursor ();
                clientFilesPath = clientPanel.pathField.getText ();
                nameFile = String.valueOf (clientPanel.filesTable.getSelectionModel ().getSelectedItem ().getFilename ());
                ProtoFileSender.sendFile (Paths.get (clientFilesPath, nameFile), SENDER.CLIENT, true, Network.getInstance ().getCurrentChannel (), future -> {
                    if (!future.isSuccess ()) {
                        future.cause ().printStackTrace ();
                        Network.getInstance ().stop ();
                    }
                    if (future.isSuccess ()) {
                        ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer (1);
                        buf.writeByte (SIGNAL_UPDATE);
                        Network.getInstance ().getCurrentChannel ().writeAndFlush (buf);
                        System.out.println ("Файл успешно передан");
                        MyLogger.logInfo ("Файл успешно передан клиенту: " + tfLogin);
                    }
                });
                notWaitCursor ();
                busy = false;
                System.out.println (busy);
            } catch (IOException e) {
                e.printStackTrace ();
            }
        } else {
            noConnect.show ();
        }
    }

    public void download(ActionEvent actionEvent) {
        if (isConnect && busy == false) {
            busy = true;
            if(!isSelectedFile(SENDER.SERVER)) {
                noSelect.show ();
                return;
            }
            waitCursor ();
            clientFilesPath = clientPanel.pathField.getText ();
            nameFile = String.valueOf (serverPanel.filesTable.getSelectionModel ().getSelectedItem ().getFilename ());
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
            busy = false;
        } else {
            noConnect.show();
        }
    }

    private boolean isSelectedFile(SENDER sender) {
        TableView<FileInfo> table;
        if (sender == SENDER.CLIENT)
            table = clientPanel.filesTable;
        else
            table = serverPanel.filesTable;
        try {
            if (table.getSelectionModel ().getSelectedItem () != null && table.getSelectionModel ().getSelectedItem ().getType () == FileInfo.FileType.FILE)
                return true;
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    public void clear(MouseEvent mouseEvent) {
        TextField tf = (TextField) mouseEvent.getSource ();
        if (mouseEvent.getSource ().equals (tf))
            tf.setText ("");
    }

    public void disConnect(ActionEvent actionEvent) {
        if (isConnect)
            Network.stop ();
        isConnect = false;
        infoField.setText ("");
        disConnect.show ();
        btnDisconnect.setVisible(false);
        btnConnect.setVisible (true);
    }

    void waitCursor(){
        cursor = Cursor.WAIT;
        new Thread (() -> (clientPanel.filesTable.getScene().getWindow ()).getScene ().setCursor (Cursor.WAIT)).start ();
    }

    void notWaitCursor(){
        cursor = Cursor.DEFAULT;
        new Thread (() -> (clientPanel.filesTable.getScene().getWindow ()).getScene ().setCursor (Cursor.DEFAULT)).start ();
    }


    public void onDragDrop(DragEvent dragEvent) {
        if(dragEvent.isDropCompleted ()){
            Object dstPanel = dragEvent.getGestureTarget ();
        }
        String nameFile = (String) dragEvent.getAcceptingObject ();
        Object srcPanel = dragEvent.getGestureSource ();
    }

    public void onSeparatorMoved(MouseEvent mouseEvent) {
    }

    public void onEnter(MouseEvent mouseEvent) {

    }
}
