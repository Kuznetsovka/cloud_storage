package network;

import java.io.*;
import java.net.Socket;

public class SocketThread extends Thread {

    private final Socket socket;
    private SocketThreadListener listener;
    private DataOutputStream out;
    private DataInputStream in;

    public SocketThread(SocketThreadListener listener, String name, Socket socket) {
        super(name);
        this.socket = socket;
        try {
            this.out = new DataOutputStream(socket.getOutputStream());
            this.in = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace ();
        }
        this.listener = listener;
        start();
    }

    @Override
    public void run() {
        try {
            listener.onSocketStart(this, socket);
            listener.onSocketReady(this, socket);
            String[] arr = in.readUTF().split ("##");
            String fileName = arr[0];
            int id = Integer.parseInt ( arr[1]);
            String command = arr[2];
            while (!isInterrupted() && in.available ()>0) {
                listener.onUploadFile(this,socket, fileName,id, in,command);
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

    public synchronized boolean download(Socket socket, String fileName, String toPath, int id) {
        try {
            System.out.println ("Download " + fileName);
            out.writeUTF(fileName + "##" + id + "##"+"upload");
            out.flush();
            return true;
        } catch (IOException e) {
            listener.onSocketException(this, e);
            close();
            return false;
        }
    }
}
