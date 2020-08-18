package com.geekbrains.cloud_storage.client.controllers;
import com.geekbrains.common_files.common.Config;
import com.geekbrains.common_files.common.FileInfo;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import lombok.Getter;

import java.util.List;

@Getter
public class ServerController extends FileController implements Initializable, Config {

    @FXML
    TableView<FileInfo> filesTable;

    @FXML
    TextField pathField;

    @FXML
    public TextField tf_server;

    protected String pathPanel=PATH_SERVER;

    public ServerController() {
        super();
    }

    public void updateListServer(List<FileInfo> list) {
        filesTable.setVisible (true);
        if (!list.isEmpty ()) {
            filesTable.getItems ().clear ();
            filesTable.getItems ().addAll (list);
            filesTable.sort ();
        }
    }

    public StringProperty textNameFile() {
        return tf_client.textProperty ();
    }
}
