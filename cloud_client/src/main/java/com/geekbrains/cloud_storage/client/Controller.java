package com.geekbrains.cloud_storage.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    public ListView<String> lv_client;
    //public TableView<File> tv_client;
    //private TableColumn<File,String> tv_client_file;
    public TextField tf_client;

    public ListView<String> lv_server;
    //public TableView<File> tv_server;
    public TextField tf_server;
    public Button upload;
    private Socket socket;
    private DataInputStream is;
    private DataOutputStream os;
    private int countBufferBytes = 1024;
    byte[] bytes = new byte[1024];
    private final String clientFilesPath = "./common/src/main/resources/clientFiles";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            socket = new Socket("localhost", 8189);
            is = new DataInputStream(socket.getInputStream ());
            os = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        File dir = new File(clientFilesPath);
        for (String file : dir.list()) {
            //tv_client_file.setCellValueFactory(new PropertyValueFactory<> (file));
            lv_client.getItems().add(file);
        }
        lv_client.setOnMouseClicked(
            e -> {
                new Thread (() -> Platform.runLater(() ->
                        tf_client.setText (String.valueOf (lv_client.getSelectionModel ().getSelectedItem ())))).start();
            });
        }

    // #download fileName
    // #upload fileName

    public void downloadCommandNIO(ActionEvent actionEvent) throws IOException {
        String fileName = tf_server.getText();
        if (fileName.equals ("") || !fileName.contains (".")) return;
        os.writeBytes ("#download");
        os.writeBytes (fileName);
        try {
            String response = bytesToStr (bytes);
            System.out.println ("resp: " + response);
            if (response.equals ("OK")) {
                String userName = bytesToStr (bytes);
                String path = clientFilesPath + "/" + userName + "/";
                createDirectory(path);
                File file = new File (path + fileName);
                if (!file.exists ()) {
                    file.createNewFile ();
                }
                long len = readLong();
                int countBytes = 1024;
                byte[] buffer = new byte[countBytes];
                try (FileOutputStream fos = new FileOutputStream (file)) {
                    if (len < countBytes) {
                        int count = is.read (buffer);
                        fos.write (buffer, 0, count);
                    } else {
                        for (long i = 0; i < len / countBytes; i++) {
                            int count = is.read (buffer);
                            fos.write (buffer, 0, count);
                        }
                    }
                }
                System.out.println ("Файл скачен!");
                if(!isExistElement (fileName)) {
                    lv_client.getItems ().add(fileName);
                    //tv_client_file.setCellValueFactory(new PropertyValueFactory<> (fileName));
                }

            }
        } catch (IOException e) {
            e.printStackTrace ();
        }
    }

    public void uploadCommandNIO(ActionEvent actionEvent) throws IOException {
        String fileName = tf_client.getText();

        byte signal =-1;
        if (fileName.equals ("") || !fileName.contains (".")) return;
        System.out.println ("find file with name: " + fileName);
        File file = new File (clientFilesPath + "/" + fileName);
        if (file.exists ()) {
            os.writeBytes ("upload");
            os.write (-1);
            os.writeBytes (fileName);
            os.write (-1);
            long len = file.length ();
            waitResponce ();
            byte res;
            os.write (longToBytes (len));
            FileInputStream fis = new FileInputStream (file);
            System.out.print("/");
            byte[] buffer = new byte[countBufferBytes];
            waitResponce ();
            while (is.available () > 0) {
                    while (fis.available () > 0) {
                        int count = fis.read (buffer);
                        try {
                            os.write (buffer, 0, count);
                        } catch (Exception e) {
                            System.out.println ("Error" + count);
                        }
                        System.out.print ("=");
                    }
            }
            System.out.print("/");
        } else {
            os.writeUTF ("File not exists");
        }
        socket.close ();
    }

    private void waitResponce() throws IOException {
        byte res = is.readByte (); // Прилетает -48
        while (res!=-1){}
        ;
        res=0;
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
        String fileName = tf_server.getText();
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
                if(!isExistElement (fileName)) {
                    lv_client.getItems ().add(fileName);
                    //tv_client_file.setCellValueFactory(new PropertyValueFactory<> (fileName));
                }

            }
        } catch (IOException e) {
            e.printStackTrace ();
        }
    }

    public void uploadCommandIO(ActionEvent actionEvent) throws IOException {
        String fileName = tf_client.getText();
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
                System.out.println("=");
            }
        } else {
            os.writeUTF ("File not exists");
        }
        System.out.println("/");
    }

    private boolean isExistElement(String fileName) {

        for (String b : lv_client.getItems ()) {
            if (b.equals (fileName)){
                return true;
            }
        }
        return false;
    }

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
}
