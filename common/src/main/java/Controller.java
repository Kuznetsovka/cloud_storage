
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
import java.util.ResourceBundle;

public class Controller implements Initializable {

    public ListView<String> lv_client;
    public TextField tf_client;
    public TextField tf_server;
    public Button upload;
    private Socket socket;
    private DataInputStream is;
    private DataOutputStream os;
    private final String clientFilesPath = "./common/src/main/resources/clientFiles";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            socket = new Socket("localhost", 8189);
            is = new DataInputStream(socket.getInputStream());
            os = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        File dir = new File(clientFilesPath);
        for (String file : dir.list()) {
            lv_client.getItems().add(file);
        }
    }

    // #download fileName
    // #upload fileName
    public void downloadCommand(ActionEvent actionEvent) throws IOException {
        String fileName = tf_server.getText();
        if (fileName=="" || !fileName.contains (".")) return;
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
                int countBytes =1024;
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

    public void uploadCommand(ActionEvent actionEvent) throws IOException {
        String fileName = tf_client.getText();
        if (fileName=="" || !fileName.contains (".")) return;
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
        tf_client.setAccessibleText (lv_client.getSelectionModel ().getSelectedItem ());
    }

    public void exit(MouseEvent mouseEvent) {
        try {
            socket.close ();
        } catch (IOException e) {
            e.printStackTrace ();
        }
    }

}
