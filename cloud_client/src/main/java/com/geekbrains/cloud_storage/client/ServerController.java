package com.geekbrains.cloud_storage.client;

import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import lombok.Getter;

import java.nio.file.Paths;
@Getter
public class ServerController extends ClientController implements Initializable {

    @FXML
    TableView<FileInfo> filesTable;

    @FXML
    TextField pathField;

    @FXML
    public TextField tf_server;
    protected String pathPanel="./common/src/main/resources/serverFilesNIO";

    public void updateList() {
        updateList(Paths.get(pathPanel));
    }

    public StringProperty firstFieldTextProperty() {
        return tf_server.textProperty();
    }
}
