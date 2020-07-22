package network;

import java.io.*;
import java.net.Socket;

public class SocketThread extends Thread {

    private final Socket socket;
    private DataOutputStream out;
    private SocketThreadListener listener;

    public SocketThread(SocketThreadListener listener, String name, Socket socket) {
        super(name);
        this.socket = socket;
        this.listener = listener;
        try {
            this.out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace ();
        }
        start();
    }

    @Override
    public void run() {
        try {
            listener.onSocketStart(this, socket);
            DataInputStream in = new DataInputStream(socket.getInputStream());
            listener.onSocketReady(this, socket);
            while (!isInterrupted() && in.available ()>0) {
                String[] arr = in.readUTF().split ("##");
                String fileName = arr[0];
                int id = Integer.parseInt (arr[1]);
                listener.onUploadFile(this,socket, fileName,id, in);
            }
        } catch (IOException e) {
            listener.onSocketException(this, e);
        } finally {
            close();
            listener.onSocketStop(this);
        }
    }

    public synchronized void close() {
        interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            listener.onSocketException(this, e);
        }
    }

    public synchronized boolean download(String fileName, String toPath, int id) {
        try {
            System.out.println ("Download " + fileName);
            try {
                sendFile(socket,
                        new File(fileName),id);
            } catch (IOException e) {
                e.printStackTrace ();
            }
            out.flush();
            return true;
        } catch (IOException e) {
            listener.onSocketException(this, e);
            close();
            return false;
        }
    }

    public synchronized void sendFile(Socket socket, File file, int id) throws IOException {
        InputStream is = new FileInputStream(file);
        long size = file.length();
        int count = (int) (size / 8192) / 10, readBuckets = 0;
        count = (size>0 && count==0)?1:count;
        // /==========/
        try(DataOutputStream os = new DataOutputStream(out)) {
            byte [] buffer = new byte[8192];
            os.writeUTF(file.getName() + "##" + id);
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
}
