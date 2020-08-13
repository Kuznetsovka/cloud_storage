package com.geekbrains.cloud_storage.cloud_client.controllers;

import com.geekbrains.cloud_storage.common.AppModel;
import com.geekbrains.cloud_storage.common.FileInfo;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import lombok.Getter;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.geekbrains.cloud_storage.cloud_client.ProtoHandlerClient.listFileServer;

@Getter
public class ServerController extends FileController implements Initializable {

    @FXML
    TableView<FileInfo> filesTable;

    @FXML
    TextField pathField;

    @FXML
    public TextField tf_server;

    protected String pathPanel="./common/src/main/resources/serverFiles";

    public ServerController(AppModel model) {
        super();
        this.model = model;
        model.textLogin ().addListener ((obs, oldText, newText) -> {
            updateList ();
        });
    }

    public void updateList() {
        filesTable.getItems().clear();
        filesTable.getItems().addAll(listFileServer);
        filesTable.sort();
    }

    public StringProperty textNameFile() {
        return tf_client.textProperty ();
    }

    public void btnPathUpAction(ActionEvent actionEvent) {
        Path upperPath = Paths.get(pathField.getText()).getParent();
        if (upperPath != null) {
            updateList(upperPath);
        }
    }
}
