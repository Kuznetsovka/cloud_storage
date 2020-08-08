package com.geekbrains.cloud_storage.client;

import javafx.beans.property.StringProperty;
import javafx.fxml.Initializable;
import lombok.Getter;
import java.nio.file.Paths;

@Getter
public class ClientController extends FileController implements Initializable {

    protected String pathPanel="./common/src/main/resources/clientFiles";

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



}