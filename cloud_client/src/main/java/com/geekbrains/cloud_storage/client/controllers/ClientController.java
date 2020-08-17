package com.geekbrains.cloud_storage.client.controllers;

import com.geekbrains.common_files.common.AppModel;
import com.geekbrains.common_files.common.FileInfo;
import com.geekbrains.common_files.common.OSType;
import com.geekbrains.common_files.common.Systems;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import lombok.Getter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

@Getter
public class ClientController extends FileController implements Initializable {
    public ClientController(AppModel model) {
        super();
        this.model = model;

        model.textPathUpdate ().addListener ((obs, oldText, newText) -> {
            if(newText!="")
                updateList (Paths.get (newText));
        });
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

    public void updateList(Path path) {
        try {
            currentPath = path.normalize().toAbsolutePath();
            pathField.setText(currentPath.toString());
            filesTable.getItems().clear();
            filesTable.getItems().addAll(Files.list(path).map(FileInfo::new).collect(Collectors.toList()));
            filesTable.sort();
            model.setText3 ("");
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Не удалось обновить список файлов", ButtonType.OK);
            alert.showAndWait();
        }
    }

}