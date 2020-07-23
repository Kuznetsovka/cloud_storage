package core;

import network.FileUtility;
import network.SocketThread;
import network.SocketThreadListener;

import java.io.*;
import java.net.Socket;


public class Client extends FileUtility implements SocketThreadListener {
    private SocketThread socketThread;
    private Socket socket;

    int id;
    public Client(int id,String pathFile){
        this.id = id;
        connect();
        upload(pathFile);
        //download();
    }

    private void download() {
        String dirName = "./common/server/user" + id + "/1.txt";
        String toPath = "./common/users/user" + id;
        socketThread.download(socket,dirName,toPath,id);
    }

    private void upload(String pathFile) {
        try {
            sendFile(socket,
                    new File(pathFile),id);
        } catch (IOException e) {
            e.printStackTrace ();
        }
    }

    private void connect() {
        try {
            socket = new Socket("localhost", 8189);
            socketThread = new SocketThread(this, "Client " + id, socket);
        } catch (IOException e) {
            showException(Thread.currentThread(), e);
        }
    }


    private void showException(Thread t, Throwable e) {
        String msg;
        StackTraceElement[] ste = e.getStackTrace();
        if (ste.length == 0)
            msg = "Empty Stacktrace";
        else {
            msg = "Exception in " + t.getName() + " " +
                    e.getClass().getCanonicalName() + ": " +
                    e.getMessage() + "\n\t at " + ste[0];
        }
    }

    public void uncaughtException(Thread t, Throwable e) {
        e.printStackTrace();
        showException(t, e);
        System.exit(1);
    }

    /**
     * Socket thread methods
     * */

    public void onSocketStart(SocketThread thread, Socket socket) {
        System.out.println ("Start");
    }

    @Override
    public void onSocketStop(SocketThread thread) {
        System.out.println ("Stop");
    }

    @Override
    public void onSocketReady(SocketThread thread, Socket socket) {
        System.out.println ("Ready");
        System.out.println ("User" + thread.getName () + " ready");
    }

    @Override
    public void onUploadFile(SocketThread socketThread, Socket socket, String fileName, int id, DataInputStream in, String command) {
        if (command.equals ("download")) {
            download (fileName, id, in);
        } else if (command.equals ("upload")){
            try {
                sendFile (socket,new File (fileName),id);
            } catch (IOException e) {
                e.printStackTrace ();
            }
        }
    }

    public synchronized void sendFile(Socket socket, File file, int id) throws IOException {
        InputStream is = new FileInputStream(file);
        long size = file.length();
        int count = (int) (size / 8192) / 10, readBuckets = 0;
        count = (size>0 && count==0)?1:count;
        // /==========/
        try(DataOutputStream os = new DataOutputStream(socket.getOutputStream())) {
            byte [] buffer = new byte[8192];
            os.writeUTF(file.getName() + "##" + id + "##" + "download");
            System.out.print("/");
            while (is.available() > 0) {
                int readBytes = is.read(buffer);
                readBuckets++;
                if (readBuckets % count == 0) {
                    System.out.print("=");
                }
                os.write(buffer, 0, readBytes);
            }
            System.out.println("/");
        }
    }

    private synchronized void download(String fileName, int id, DataInputStream in) {
        String dirName = "./common/users/user";
        System.out.println ("Client " + id + " accepted!");
        System.out.println ("fileName: " + fileName);
        try {
            createDirectory (dirName + id + "/");
            File file = new File (dirName + id + "/" + fileName);
            file.createNewFile ();
            try (FileOutputStream os = new FileOutputStream (file)) {
                byte[] buffer = new byte[8192];
                while (true) {
                    int r = in.read (buffer);
                    if (r == -1) break;
                    os.write (buffer, 0, r);
                }
            }
        } catch (IOException e) {
            e.printStackTrace ();
        }
        System.out.println ("File download!");
    }

    @Override
    public void onSocketException(SocketThread thread, Exception exception) {
        showException(thread, exception);
    }
}
