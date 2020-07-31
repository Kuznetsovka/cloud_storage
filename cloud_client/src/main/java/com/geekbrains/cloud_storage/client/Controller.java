package com.geekbrains.cloud_storage.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.StringBinding;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    public ListView<String> lv_client;
    public TextField tf_client;
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
            lv_client.getItems().add(file);
        }
        new Thread (new Runnable () {
            @Override
            public void run() {
                Platform.runLater(new Runnable() {
                    @Override public void run() {
                        if (lv_client.getSelectionModel ().getSelectedIndex ()>0)
                            tf_client.setAccessibleText (lv_client.getSelectionModel ().getSelectedItem ());
                    }
                });
            }
        }).start();
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
                    lv_client.getItems ().add (fileName);
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
            byte [] res = new byte[1];
            is.readFully (res);
            os.write (longToBytes (len));
            FileInputStream fis = new FileInputStream (file);
            System.out.print("/");
            byte[] buffer = new byte[countBufferBytes];
            is.readFully (res);
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
                    lv_client.getItems ().add (fileName);
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

    public void selectfiles(TouchEvent touchEvent) {
       // tf_client.setAccessibleText (lv_client.getSelectionModel ().getSelectedItem ());
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
