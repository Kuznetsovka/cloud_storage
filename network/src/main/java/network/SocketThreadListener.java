package network;

import java.io.DataInputStream;
import java.net.Socket;
import java.util.List;

public interface SocketThreadListener {
    void onSocketStart(SocketThread thread, Socket socket);
    void onSocketStop(SocketThread thread);
    void onSocketReady(SocketThread thread, Socket socket);
    void onSocketException(SocketThread thread, Exception exception);
    void onUploadFile(SocketThread socketThread,Socket socket, String fileName, int id, DataInputStream in);
    void uncaughtException(Thread t, Throwable e);
}