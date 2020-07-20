package core;

import network.SocketThread;
import network.SocketThreadListener;

import java.net.Socket;

public class ClientThread extends SocketThread {
    private String nickname;
    private boolean isAuthorized;
    private boolean isReconnected;

    public ClientThread(SocketThreadListener listener, String name, Socket socket) {
        super(listener, name, socket);
    }

    public String getNickname() {
        return nickname;
    }

    public boolean isAuthorized() {
        return isAuthorized;
    }

    public boolean isReconnected() {
        return isReconnected;
    }

    void reconnect() {
        isReconnected = true;
        close();
    }

    void authAccept(String nickname) {
        isAuthorized = true;
        this.nickname = nickname;
        sendMessage("Client auth");
    }

    void authFail() {
        sendMessage("Client deny");
        close();
    }

    void msgFormatError(String msg) {
        sendMessage("Error");
        close();
    }

}
