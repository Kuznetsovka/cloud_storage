package com.geekbrains.cloud_storage.client.controllers;

import com.geekbrains.common_files.common.FileInfo;
import com.geekbrains.common_files.common.OSType;
import com.geekbrains.common_files.common.Systems;
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

    String pathPanel;

    @FXML
    TextField pathField;

    private Controller parent;

    protected Path currentPath;

    @FXML
    public TextField tf_client;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (Systems.OsCheck.getOperatingSystemType () == OSType.Windows)
            pathPanel = "C:\\Users\\" + Systems.user + "\\Downloads";
        fillTable ();
        new Thread (()-> runLater(() -> filesTable.setOnMouseClicked (event -> {
            if (event.getClickCount () == 2 && filesTable.getSelectionModel ().getSelectedItem () != null) {
                Path path = Paths.get (pathField.getText ()).resolve (filesTable.getSelectionModel ().getSelectedItem ().getFilename ());
                if (isDirectory (path)) {
                    updateList (path);
                }
            }
        }))).start();
        updateList();
    }

    private void fillTable() {
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
    }

    public void updateList() {
        updateList(Paths.get(pathPanel));
    }

    public void updateList(Path path) {
        try {
            currentPath = path.normalize().toAbsolutePath();
            pathField.setText(currentPath.toString());
            filesTable.getItems().clear();
            filesTable.getItems().addAll(Files.list(path).map(FileInfo::new).collect(Collectors.toList()));
            filesTable.sort();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Не удалось обновить список файлов", ButtonType.OK);
            alert.showAndWait();
        }
    }

}
