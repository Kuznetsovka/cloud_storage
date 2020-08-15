package com.geekbrains.cloud_storage.cloud_client.controllers;

import com.geekbrains.cloud_storage.common.AppModel;
import com.geekbrains.cloud_storage.common.FileInfo;
import com.geekbrains.cloud_storage.common.SENDER;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import lombok.Getter;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static com.geekbrains.cloud_storage.cloud_client.ProtoHandlerClient.listFileServer;
import static com.geekbrains.cloud_storage.common.Config.PATH_SERVER;
import static java.nio.file.Files.isDirectory;
import static javafx.application.Platform.runLater;


@Getter
public class ClientController implements Initializable {

    @FXML
    TableView<FileInfo> filesTableServer;

    @FXML
    public TextField tf_server;

    @FXML
    TableView<FileInfo> filesTableClient;

    String pathPanel = "";

    @FXML
    TextField pathField;

    protected Path currentPath;

    protected AppModel model ;
    @FXML
    protected TextField login;

    public ClientController(AppModel model) {
        this.model = model;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fillTable(filesTableClient);
        fillTable(filesTableServer);
        new Thread (()-> runLater(() -> filesTableClient.setOnMouseClicked (event -> {
            clickToTable (event,filesTableClient,pathField.getText ());
        }))).start();

        new Thread (()-> runLater(() -> filesTableServer.setOnMouseClicked (event -> {
            clickToTable (event,filesTableServer,PATH_SERVER);
        }))).start();

        updateTableClient ();
        model.setText2 (pathField.getText ());

        model.textPathUpdate ().addListener ((obs, oldText, newText) -> {
            updateTableClient ();
        });

        model.textLogin ().addListener ((obs, oldText, newText) -> {
            updateListServer();
        });
    }

    private void fillTable(TableView<FileInfo> filesTableClient) {
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
        filesTableClient.getColumns().addAll(filenameColumn, fileSizeColumn, fileDateColumn);
    }

    private void updateTableClient() {
        updateList (filesTableClient, Paths.get (pathField.getText ()));
    }

    private void clickToTable(MouseEvent event,TableView<FileInfo> table,String strPath) {
        if (event.getClickCount () == 2 && table.getSelectionModel ().getSelectedItem () != null) {
            Path path = Paths.get (strPath).resolve (table.getSelectionModel ().getSelectedItem ().getFilename ());
            if (isDirectory (path)) {
                updateList (table,path);
                if (table.equals (filesTableClient))
                    model.setText2 (String.valueOf (path));
            }
        }
        if (event.getClickCount () == 1 && table.getSelectionModel ().getSelectedItem () != null)
            if (table.getSelectionModel ().getSelectedItem ().getType () == FileInfo.FileType.FILE) {
                model.setText1 (String.valueOf (table.getSelectionModel ().getSelectedItem ().getFilename ()));
            }
    }

    public void updateLists() {
        updateList(filesTableClient,Paths.get(pathPanel));
        updateList(filesTableServer,Paths.get(PATH_SERVER, login.getText ()));
    }

    public void updateList(TableView<FileInfo> table,Path path) {
        try {
            if (table.equals (filesTableClient)) {
                currentPath = path.normalize ().toAbsolutePath ();
                pathField.setText (currentPath.toString ());
            }
            table.getItems().clear();
            table.getItems().addAll(Files.list(path).map(path1 -> {
                try {
                    return new FileInfo (path1);
                } catch (IOException e) {
                    e.printStackTrace ();
                }
                return null;
            }).collect(Collectors.toList()));
            table.sort();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Не удалось обновить список файлов", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void btnPathUpAction(ActionEvent actionEvent) {
        Path upperPath = Paths.get(pathField.getText()).getParent();
        if (upperPath != null) {
            updateList(filesTableClient,upperPath);
            model.setText2 (String.valueOf (upperPath));
        }
    }

    public void updateListServer() {
        filesTableServer.getItems().clear();
        filesTableServer.getItems().addAll(listFileServer);
        filesTableServer.sort();

    }

}