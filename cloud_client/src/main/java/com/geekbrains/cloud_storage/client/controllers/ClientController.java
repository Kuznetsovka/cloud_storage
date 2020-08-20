package com.geekbrains.cloud_storage.client.controllers;

import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import lombok.Getter;
import java.nio.file.Path;
import java.nio.file.Paths;

@Getter
public class ClientController extends FileController implements Initializable {
    @FXML
    TextField pathField;


    public ClientController() {
        super();
    }

    public StringProperty firstFieldTextProperty() {
        return tf_client.textProperty();
    }

    public void btnPathUpAction(ActionEvent actionEvent) {
        Path upperPath = Paths.get(pathField.getText()).getParent();
        if (upperPath != null) {
            updateList(upperPath);
        }
    }

    public void onDragDrop(DragEvent dragEvent) {
        if(dragEvent.isDropCompleted ()){
            Object dstPanel = dragEvent.getGestureTarget ();
        }
        String nameFile = (String) dragEvent.getAcceptingObject ();
        Object srcPanel = dragEvent.getGestureSource ();
    }

}