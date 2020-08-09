package com.geekbrains.cloud_storage.client;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class FileController implements Initializable {
    @FXML
    TableView<FileInfo> filesTable;

    String pathPanel = "";

    @FXML
    TextField pathField;

    private Controller parent;

    protected Path currentPath;

    @FXML
    public TextField tf_client;
    protected AppModel model ;

    public Path getCurrentPath() {
        return getCurrentPath();
    }

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
        new Thread (()-> {
            Platform.runLater(() -> {
                filesTable.setOnMouseClicked (event -> {
                    if (event.getClickCount () == 2 && filesTable.getSelectionModel ().getSelectedItem () != null) {
                        Path path = Paths.get (pathField.getText ()).resolve (filesTable.getSelectionModel ().getSelectedItem ().getFilename ());
                        if (Files.isDirectory (path)) {
                            updateList (path);
                        }
                    }
                    if (event.getClickCount () == 1 && filesTable.getSelectionModel ().getSelectedItem () != null)
                        if (filesTable.getSelectionModel ().getSelectedItem ().getType () == FileInfo.FileType.FILE) {
                            model.setText (String.valueOf (filesTable.getSelectionModel ().getSelectedItem ().getFilename ()));
                        }
                });
            });
        }).start();
        updateList();
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

    public void btnPathUpAction(ActionEvent actionEvent) {
        Path upperPath = Paths.get(pathField.getText()).getParent();
        if (upperPath != null) {
            updateList(upperPath);
        }
    }

    public void selectDiskAction(ActionEvent actionEvent) {
        ComboBox<String> element = (ComboBox<String>) actionEvent.getSource();
        updateList(Paths.get(element.getSelectionModel().getSelectedItem()));
    }

    public String getSelectedFilename() {
        if (!filesTable.isFocused()) {
            return null;
        }
        return filesTable.getSelectionModel().getSelectedItem().getFilename();
    }

    public StringProperty firstFieldTextProperty() {
        StringProperty o = null;
        return o;
    };
}
