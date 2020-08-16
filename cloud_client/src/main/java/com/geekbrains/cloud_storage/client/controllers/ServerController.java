package com.geekbrains.cloud_storage.client.controllers;

import com.geekbrains.common.common.AppModel;
import com.geekbrains.common.common.FileInfo;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import lombok.Getter;

import static com.geekbrains.cloud_storage.client.ProtoHandlerClient.listFileServer;
import static com.geekbrains.common.common.Config.PATH_SERVER;

@Getter
public class ServerController extends FileController implements Initializable {

    @FXML
    TableView<FileInfo> filesTable;

    @FXML
    TextField pathField;

    @FXML
    public TextField tf_server;

    protected String pathPanel=PATH_SERVER;

    public ServerController(AppModel model) {
        super();
        this.model = model;
        model.textLogin ().addListener ((obs, oldText, newText) -> {
            pathField.setText (newText);
            updateListServer();
        });
    }

    public void updateListServer() {
        filesTable.setVisible (true);
        filesTable.getItems().clear();
        filesTable.getItems().addAll(listFileServer);
        filesTable.sort();
        listFileServer.clear();
    }

    public StringProperty textNameFile() {
        return tf_client.textProperty ();
    }
}
