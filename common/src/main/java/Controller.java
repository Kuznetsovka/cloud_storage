
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    public ListView<String> lv;
    public TextField txt;
    public Button send;
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
            lv.getItems().add(file);
        }
    }

    // ./download fileName
    // ./upload fileName
    public void sendCommand(ActionEvent actionEvent) throws IOException {
        String command = txt.getText();
        String [] op = command.split(" ");
        switch (op[0]){
            case("#download"):
                os.writeUTF (op[0]);
                os.writeUTF (op[1]);
                try {
                    String response = is.readUTF ();
                    System.out.println ("resp: " + response);
                    if (response.equals ("OK")) {
                        String userName = is.readUTF ();
                        String path = clientFilesPath + "/" + userName + "/";
                        createDirectory(path);
                        File file = new File (path + op[1]);
                        if (!file.exists ()) {
                            file.createNewFile ();
                        }
                        long len = is.readLong ();
                        byte[] buffer = new byte[1024];
                        try (FileOutputStream fos = new FileOutputStream (file)) {
                            if (len < 1024) {
                                int count = is.read (buffer);
                                fos.write (buffer, 0, count);
                            } else {
                                for (long i = 0; i < len / 1024; i++) {
                                    int count = is.read (buffer);
                                    fos.write (buffer, 0, count);
                                }
                            }
                        }
                        lv.getItems ().add (op[1]);
                    }
                } catch (IOException e) {
                    e.printStackTrace ();
                } finally {
                    break;
                }
            case("#upload"):
                os.writeUTF(op[0]);
                os.writeUTF(op[1]);
                System.out.println ("find file with name: " + op[1]);
                File file = new File (clientFilesPath + "/" + op[1]);
                if (file.exists ()) {
                    long len = file.length ();
                    os.writeLong (len);
                    FileInputStream fis = new FileInputStream (file);
                    byte[] buffer = new byte[1024];
                    while (fis.available () > 0) {
                        int count = fis.read (buffer);
                        os.write (buffer, 0, count);
                    }
                } else {
                    os.writeUTF ("File not exists");
                }
                System.out.println("/");
                break;
            case("#close"):
                socket.close ();
            default:
                socket.close ();
        }
    }

    public static void createDirectory(String dirName) {
        File file = new File(dirName);
        if (!file.exists()) {
            file.mkdir();
        }
    }
}
