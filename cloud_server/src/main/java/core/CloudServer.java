package core;

import network.ServerSocketThread;
import network.ServerSocketThreadListener;
import network.SocketThread;
import network.SocketThreadListener;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

import static network.FileUtility.createDirectory;

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
            System.out.println ("Server already started!");
        }
    }

    public void stop() {
        if (server != null && server.isAlive()) {
            server.interrupt(); //null.interrupt();
        } else {
            System.out.println ("Server is not running");
        }
    }

    /**
     * Server Socket Thread methods
     * */

    @Override
    public void onServerStart(ServerSocketThread thread) {
        System.out.println ("Server started");
  //
    }

    @Override
    public void onServerStop(ServerSocketThread thread) {
        System.out.println ("Server stopped");
        for (int i = 0; i < clients.size(); i++) {
            clients.get(i).close();
        }
        //
    }

    @Override
    public void onServerSocketCreated(ServerSocketThread thread, ServerSocket server) {
        System.out.println ("Server socket created");
    }

    @Override
    public void onServerTimeout(ServerSocketThread thread, ServerSocket server) { }

    @Override
    public void onSocketAccepted(ServerSocketThread thread, ServerSocket server, Socket socket) {
        System.out.println ("Client connected " + thread.getName ());
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
        System.out.println ("Client connected");
    }

    @Override
    public synchronized void onSocketStop(SocketThread thread) {
        ClientThread client = (ClientThread) thread;
        clients.remove(thread);
    }

    @Override
    public synchronized void onSocketReady(SocketThread thread, Socket socket) {
        System.out.println ("Client is ready");
        clients.add(thread);
    }

    @Override
    public void onUploadFile(SocketThread socketThread, Socket socket, String fileName, int id, DataInputStream in) {
        String dirName = "./common/server/user";
        System.out.println("Client "+ id + " accepted!");
        System.out.println("fileName: " + fileName);
        try {
            createDirectory(dirName + id + "/");
            File file = new File(dirName + id + "/" + fileName);
            file.createNewFile();
            try (FileOutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[8192];
                while (true) {
                    int r = in.read(buffer);
                    if (r == -1) break;
                    os.write(buffer, 0, r);
                }
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

}
