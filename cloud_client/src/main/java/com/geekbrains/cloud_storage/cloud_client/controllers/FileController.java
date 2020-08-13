package com.geekbrains.cloud_storage.cloud_client.controllers;

import com.geekbrains.cloud_storage.common.AppModel;
import com.geekbrains.cloud_storage.common.FileInfo;
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static java.nio.file.Files.isDirectory;
import static javafx.application.Platform.*;

public class FileController implements Initializable {
    @FXML
    TableView<FileInfo> filesTable;

    String pathPanel = "";

    @FXML
    TextField pathField;

    protected Path currentPath;

    @FXML
    public TextField tf_client;
    protected AppModel model ;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        TableColumn<FileInfo, String> filenameColumn = new TableColumn<>("Название");
        filenameColumn.setCellValueFactory(param -> new SimpleStringProperty (param.getValue().getFilename()));
        filenameColumn.setPrefWidth(300.0f);

        TableColumn<FileInfo, Long> fileSizeColumn = new TableColumn<>("Размер");
        fileSizeColumn.setCellValueFactory(param -> new SimpleObjectProperty<> (param.getValue().getSize()));
        fileSizeColumn.setCellFactory(column -> new TableCell<FileInfo, Long> () {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    String text = String.format("%,d bytes", item);
                    if (item == -1L) {
                        text = "[DIR]";
                    }
                    setText(text);
                }
            }
        });
        fileSizeColumn.setPrefWidth(120);
        TableColumn<FileInfo, String> fileDateColumn = new TableColumn("Дата изменения");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        fileDateColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getLastModified().format(dtf)));
        fileDateColumn.setPrefWidth(120);
        filesTable.getColumns().addAll(filenameColumn, fileSizeColumn, fileDateColumn);
        new Thread (()-> runLater(() -> filesTable.setOnMouseClicked (event -> {
            if (event.getClickCount () == 2 && filesTable.getSelectionModel ().getSelectedItem () != null) {
                Path path = Paths.get (pathField.getText ()).resolve (filesTable.getSelectionModel ().getSelectedItem ().getFilename ());
                if (isDirectory (path)) {
                    updateList (path);
                    if (!path.toString ().contains ("server"))
                        model.setText2 (String.valueOf (path));
                }
            }
            if (event.getClickCount () == 1 && filesTable.getSelectionModel ().getSelectedItem () != null)
                if (filesTable.getSelectionModel ().getSelectedItem ().getType () == FileInfo.FileType.FILE) {
                    model.setText1 (String.valueOf (filesTable.getSelectionModel ().getSelectedItem ().getFilename ()));
                }
        }))).start();
        updateList();
        if (!pathField.getText ().contains ("server"))
            model.setText2 (pathField.getText ());
    }

    public void updateList() {
        updateList(Paths.get(pathPanel));
    }

    public void updateList(Path path) {
        try {
            currentPath = path.normalize().toAbsolutePath();
            pathField.setText(currentPath.toString());
            filesTable.getItems().clear();
            filesTable.getItems().addAll(Files.list(path).map(path1 -> {
                try {
                    return new FileInfo (path1);
                } catch (IOException e) {
                    e.printStackTrace ();
                }
                return null;
            }).collect(Collectors.toList()));
            filesTable.sort();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Не удалось обновить список файлов", ButtonType.OK);
            alert.showAndWait();
        }
    }

//    public void selectDiskAction(ActionEvent actionEvent) {
//        ComboBox<String> element = (ComboBox<String>) actionEvent.getSource();
//        updateList(Paths.get(element.getSelectionModel().getSelectedItem()));
//    }
//
//    public String getSelectedFilename() {
//        if (!filesTable.isFocused()) {
//            return null;
//        }
//        return filesTable.getSelectionModel().getSelectedItem().getFilename();
//    }
//
//    public StringProperty firstFieldTextProperty() {
//        StringProperty o = null;
//        return o;
//    };
}