package core;

import network.ServerSocketThread;
import network.ServerSocketThreadListener;
import network.SocketThread;
import network.SocketThreadListener;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class CloudServer implements ServerSocketThreadListener, SocketThreadListener {

    ServerSocketThread server;
    CloudServerListener listener;
    private Vector<SocketThread> clients = new Vector<>();

    public CloudServer(CloudServerListener listener) {
        this.listener = listener;
    }

    public void start(int port) {
        if (server == null || !server.isAlive()) {
            server = new ServerSocketThread(this, "Server", port, 2000);
        } else {
            putLog("Server already started!");
        }
    }

    public void stop() {
        if (server != null && server.isAlive()) {
            server.interrupt(); //null.interrupt();
        } else {
            putLog("Server is not running");
        }
    }

    private void putLog(String msg) {
        listener.onCloudServerMessage(msg);
    }

    /**
     * Server Socket Thread methods
     * */

    @Override
    public void onServerStart(ServerSocketThread thread) {
        putLog("Server started");
  //
    }

    @Override
    public void onServerStop(ServerSocketThread thread) {
        putLog("Server stopped");
        for (int i = 0; i < clients.size(); i++) {
            clients.get(i).close();
        }
        //
    }

    @Override
    public void onServerSocketCreated(ServerSocketThread thread, ServerSocket server) {
        putLog("Server socket created");
    }

    @Override
    public void onServerTimeout(ServerSocketThread thread, ServerSocket server) { }

    @Override
    public void onSocketAccepted(ServerSocketThread thread, ServerSocket server, Socket socket) {
        putLog("Client connected");
        String name = "Socket Thread " + socket.getInetAddress() + ":" + socket.getPort();
        new ClientThread(this, name, socket);
    }

    @Override
    public void onServerException(ServerSocketThread thread, Throwable exception) {
        exception.printStackTrace();
    }

    /**
     * Socket Thread methods
     * */

    @Override
    public synchronized void onSocketStart(SocketThread thread, Socket socket) {
        putLog("Client connected");
    }

    @Override
    public synchronized void onSocketStop(SocketThread thread) {
        ClientThread client = (ClientThread) thread;
        clients.remove(thread);
    }

    @Override
    public synchronized void onSocketReady(SocketThread thread, Socket socket) {
        putLog("Client is ready");
        clients.add(thread);
    }

    @Override
    public synchronized void onReceiveString(SocketThread thread, Socket socket, String msg) {
        ClientThread client = (ClientThread) thread;
        if (client.isAuthorized()) {

        } else {

        }
    }

    @Override
    public void onUploadFile(SocketThread socketThread, Socket socket, String fileName) {
        String toPath = "./common/" + socketThread.getName () + "/";
        File file = new File(toPath + "" + fileName);
        // ./common/server/ - для сервера
        // ./common/client/id/ - для юзера
        try {
            DataInputStream in = new DataInputStream (socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            file.createNewFile();
            try (FileOutputStream fos = new FileOutputStream(file)) {
                byte[] buffer = new byte[8192];
                while (true) {
                    int r = in.read(buffer);
                    if (r == -1) break;
                    out.write(buffer, 0, r);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace ();
            } catch (IOException e) {
                e.printStackTrace ();
            }
        } catch (IOException e) {
            e.printStackTrace ();
        }
        System.out.println("File uploaded!");
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {

    }

    @Override
    public synchronized void onSocketException(SocketThread thread, Exception exception) {
        exception.printStackTrace();
    }

    private synchronized ClientThread findClientByNickname(String nickname) {
        for (int i = 0; i < clients.size(); i++) {
            ClientThread client = (ClientThread) clients.get(i);
            if (!client.isAuthorized()) continue;
            if (client.getNickname().equals(nickname))
                return client;
        }
        return null;
    }

}
