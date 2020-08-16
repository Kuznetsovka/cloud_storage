package com.geekbrains.cloud_storage.client.controllers;
import com.geekbrains.common_files.common.AppModel;
import com.geekbrains.common_files.common.Config;
import com.geekbrains.common_files.common.FileInfo;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import lombok.Getter;

import static com.geekbrains.cloud_storage.client.ProtoHandlerClient.listFileServer;

@Getter
public class ServerController extends FileController implements Initializable, Config {

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
            if (newText!="") {
                pathField.setText (newText);
                updateListServer ();
            }
        });
    }

    public void updateListServer() {
        filesTable.setVisible (true);
        filesTable.getItems().clear();
        filesTable.getItems().addAll(listFileServer);
        filesTable.sort();
        listFileServer.clear();
        model.setText4 ("");
    }

    public StringProperty textNameFile() {
        return tf_client.textProperty ();
    }
}
