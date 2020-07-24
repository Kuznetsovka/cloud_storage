package IOSolution.server;

import java.io.*;
import java.net.Socket;

public class FileHandler implements Runnable {

    private String serverFilePath = "./common/src/main/resources/serverFiles";
    private Socket socket;
    private DataInputStream is;
    private DataOutputStream os;
    private boolean isRunning = true;
    private static int cnt = 1;
    private String userName;

    public FileHandler(Socket socket) throws IOException {
        this.socket = socket;
        is = new DataInputStream(socket.getInputStream());
        os = new DataOutputStream(socket.getOutputStream());
        userName = "user" + cnt;
        cnt++;
        serverFilePath += "/" + userName;
        File dir = new File(serverFilePath);
        if (!dir.exists()) {
            dir.mkdir();
        }
    }

    @Override
    public void run() {
        while (isRunning) {
            try {
                String command = is.readUTF ();
                String fileName = is.readUTF ();
                if (command.equals ("#download")) {
                    System.out.println ("find file with name: " + fileName);
                    File file = new File (serverFilePath + "/" + fileName);
                    if (file.exists ()) {
                        os.writeUTF ("OK");
                        long len = file.length ();
                        os.writeUTF (userName);
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
                } else if (command.equals ("#upload")) {
                    try {
                        File file = new File (serverFilePath + "/" + fileName);
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
                            System.out.println ("Файл закачан!");
                        }
                    } catch (IOException e) {
                        e.printStackTrace ();
                    }
                }
            } catch (Exception e) {

            }
        }
    }

    public static void createDirectory(String dirName) {
        File file = new File(dirName);
        if (!file.exists()) {
            file.mkdir();
        }
    }
}
