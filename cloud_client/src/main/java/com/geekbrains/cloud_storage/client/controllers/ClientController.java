package com.geekbrains.cloud_storage.client.controllers;

import com.geekbrains.common.common.AppModel;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import lombok.Getter;

import java.nio.file.Path;
import java.nio.file.Paths;

@Getter
public class ClientController extends FileController implements Initializable {

    public ClientController(AppModel model) {
        super();
        this.model = model;
    }

    public void updateList() {
        updateList(Paths.get(pathPanel));
    }

    public StringProperty firstFieldTextProperty() {
        return tf_client.textProperty();
    }

    public void btnPathUpAction(ActionEvent actionEvent) {
        Path upperPath = Paths.get(pathField.getText()).getParent();
        if (upperPath != null) {
            updateList(upperPath);
            model.setText2 (String.valueOf (upperPath));
        }
    }

}