package com.geekbrains.cloud_storage.client.controllers;

import com.geekbrains.common.common.AppModel;
import com.geekbrains.common.common.FileInfo;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import lombok.Getter;

import java.nio.file.Path;
import java.nio.file.Paths;

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
            updateList (Paths.get (pathPanel + "/" + newText));
        });
    }

    public void updateList() {
        updateList(Paths.get(pathPanel));
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
