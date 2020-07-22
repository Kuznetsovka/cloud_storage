package network;

import java.io.*;
import java.net.Socket;

public class SocketThread extends Thread {

    private final Socket socket;
    private SocketThreadListener listener;

    public SocketThread(SocketThreadListener listener, String name, Socket socket) {
        super(name);
        this.socket = socket;
        this.listener = listener;
        start();
    }

    @Override
    public void run() {
        try {
            listener.onSocketStart(this, socket);
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
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

    public synchronized boolean download(Socket socket1, String fileName, String toPath, int id) {
        try {
            System.out.println ("Download " + fileName);
            FileUtility.sendFile(socket1,
                    new File(fileName),id);
            return true;
        } catch (IOException e) {
            listener.onSocketException(this, e);
            close();
            return false;
        }
    }
}
