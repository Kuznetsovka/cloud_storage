package com.geekbrains.cloud_storage.client;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import lombok.Getter;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

@Getter
public class ServerController extends ClientController implements Initializable {

    @FXML
    TableView<FileInfo> filesTable;

    @FXML
    TextField pathField;

    @FXML
    public TextField tf_server;

    protected String pathPanel="./common/src/main/resources/serverFiles";

    public ServerController(AppModel model) {
        super (model);
    }
//
//    @Override
//    public void initialize(URL location, ResourceBundle resources) {
//        TableColumn<FileInfo, String> filenameColumn = new TableColumn<>("Название");
//        filenameColumn.setCellValueFactory(param -> new SimpleStringProperty (param.getValue().getFilename()));
//        filenameColumn.setPrefWidth(300.0f);
//
//        TableColumn<FileInfo, Long> fileSizeColumn = new TableColumn<>("Размер");
//        fileSizeColumn.setCellValueFactory(param -> new SimpleObjectProperty<> (param.getValue().getSize()));
//        fileSizeColumn.setCellFactory(column -> new TableCell<FileInfo, Long>() {
//            @Override
//            protected void updateItem(Long item, boolean empty) {
//                super.updateItem(item, empty);
//                if (item == null || empty) {
//                    setText(null);
//                    setStyle("");
//                } else {
//                    String text = String.format("%,d bytes", item);
//                    if (item == -1L) {
//                        text = "[DIR]";
//                    }
//                    setText(text);
//                }
//            }
//        });
//        fileSizeColumn.setPrefWidth(120);
//        TableColumn<FileInfo, String> fileDateColumn = new TableColumn("Дата изменения");
//        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//        fileDateColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getLastModified().format(dtf)));
//        fileDateColumn.setPrefWidth(120);
//        filesTable.getColumns().addAll(filenameColumn, fileSizeColumn, fileDateColumn);
//        new Thread (()-> {
//            Platform.runLater(() -> {
//                filesTable.setOnMouseClicked (event -> {
//                    if (event.getClickCount () == 2 && filesTable.getSelectionModel ().getSelectedItem () != null) {
//                        Path path = Paths.get (pathField.getText ()).resolve (filesTable.getSelectionModel ().getSelectedItem ().getFilename ());
//                        if (Files.isDirectory (path)) {
//                            updateList (path);
//                        }
//                    }
//                    if (event.getClickCount () == 1 && filesTable.getSelectionModel ().getSelectedItem () != null)
//                        if (filesTable.getSelectionModel ().getSelectedItem ().getType () == FileInfo.FileType.FILE) {
//                            model.setText (String.valueOf (filesTable.getSelectionModel ().getSelectedItem ().getFilename ()));
//                        }
//                });
//            });
//        }).start();
//        updateList();
//    }

    public void updateList() {
        updateList(Paths.get(pathPanel));
    }

    public StringProperty firstFieldTextProperty() {
        return tf_server.textProperty();
    }
}
