package com.geekbrains.cloud_storage.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    public Button upload;
    private Socket socket;
    private DataInputStream is;
    private DataOutputStream os;
    private int countBufferBytes = 1024;
    byte[] bytes = new byte[1024];
    private AppModel model ;
    private String nameFile;
    private final String clientFilesPath = "./common/src/main/resources/serverFiles";

    @FXML
    private Label secondField;

    public Controller(AppModel model) {
        this.model = model ;
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        model.textProperty().addListener((obs, oldText, newText) -> {
            nameFile = newText;
            secondField.setText ("Выбран файл: " + newText);
        });
        }

    public byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(x);
        return buffer.array();
    }


    private long readLong() throws IOException {
        int countBytes = 1024;
        StringBuilder bufferStr = new StringBuilder();
        byte[] buffer = new byte[countBytes];
        while (is.available () > 0) {
            is.readFully (buffer);
            String str = new String(buffer);
            bufferStr.append (str);
        }
        return Long.getLong (String.valueOf (bufferStr));
    }

    private String bytesToStr(byte[] bytes) throws IOException {
        is.readFully (bytes);
        return new String (bytes);
    }

    public void downloadCommandIO(ActionEvent actionEvent) throws IOException {
        String fileName = nameFile;
        if (fileName.equals ("") || !fileName.contains (".")) return;
        os.writeUTF ("#download");
        os.writeUTF (fileName);
        try {
            String response = is.readUTF ();
            System.out.println ("resp: " + response);
            if (response.equals ("OK")) {
                String userName = is.readUTF ();
                String path = clientFilesPath + "/" + userName + "/";
                createDirectory(path);
                File file = new File (path + fileName);
                if (!file.exists ()) {
                    file.createNewFile ();
                }
                long len = is.readLong ();
                byte[] buffer = new byte[countBufferBytes];
                try (FileOutputStream fos = new FileOutputStream (file)) {
                    if (len < countBufferBytes) {
                        int count = is.read (buffer);
                        fos.write (buffer, 0, count);
                    } else {
                        for (long i = 0; i < len / countBufferBytes; i++) {
                            int count = is.read (buffer);
                            fos.write (buffer, 0, count);
                        }
                    }
                }
                System.out.println ("Файл скачен!");
//                if(!isExistElement (fileName)) {
//                    //lv_client.getItems ().add(fileName);
//                    tv_client_file.setCellValueFactory(new PropertyValueFactory<> (fileName));
//                }
            }
        } catch (IOException e) {
            e.printStackTrace ();
        }
    }

    public void uploadCommandIO(ActionEvent actionEvent) throws IOException {
        String fileName = nameFile;
        if (fileName.equals ("") || !fileName.contains (".")) return;
        os.writeUTF ("#upload");
        os.writeUTF(fileName);
        System.out.println ("find file with name: " + fileName);
        File file = new File (clientFilesPath + "/" + fileName);
        if (file.exists ()) {
            long len = file.length ();
            os.writeLong (len);
            FileInputStream fis = new FileInputStream (file);
            System.out.println("/");
            byte[] buffer = new byte[1024];
            while (fis.available () > 0) {
                int count = fis.read (buffer);
                os.write (buffer, 0, count);
                System.out.print("=");
            }
        } else {
            os.writeUTF ("File not exists");
        }
        System.out.println("/");
    }

//    private boolean isExistElement(String fileName) {
//
//        for (String b : lv_client.getItems ()) {
//            if (b.equals (fileName)){
//                return true;
//            }
//        }
//        return false;
//    }

    public static void createDirectory(String dirName) {
        File file = new File(dirName);
        if (!file.exists()) {
            file.mkdir();
        }
    }

    public void exitAction(ActionEvent actionEvent) {
        try {
            socket.close ();
            Platform.exit ();
        } catch (IOException e) {
            e.printStackTrace ();
        }
        Platform.exit ();
    }

    public void connect(ActionEvent actionEvent) {
        try {
            socket = new Socket("localhost", 8189);
            is = new DataInputStream(socket.getInputStream ());
            os = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    public void deleteBtnAction(ActionEvent actionEvent) {
//        PanelController leftPC = (PanelController) leftPanel.getProperties().get("ctrl");
//        PanelController rightPC = (PanelController) rightPanel.getProperties().get("ctrl");
//        if (this.getSelectedFilename() == null && rightPC.getSelectedFilename() == null) {
//            Alert alert = new Alert(Alert.AlertType.ERROR, "Ни один файл не был выбран", ButtonType.OK);
//            alert.showAndWait();
//            return;
//        }
//        PanelController currentPC = null;
//        if (leftPC.getSelectedFilename() != null) {
//            currentPC = leftPC;
//        }
//        if (rightPC.getSelectedFilename() != null) {
//            currentPC = rightPC;
//        }
//        Path pathToFile = currentPC.getCurrentPath().resolve(currentPC.getSelectedFilename());
//        if(!Files.isDirectory(pathToFile)) {
//            try {
//                Files.delete(pathToFile);
//                currentPC.updateList();
//            } catch (IOException e) {
//                Alert alert = new Alert(Alert.AlertType.ERROR, "Не удалось удалить выбранный файл", ButtonType.OK);
//                alert.showAndWait();
//            }
//        }
//    }
}
