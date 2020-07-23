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
        this.nickname = name;
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
        System.out.println ("Client auth" + getName ());
    }

    void authFail() {
        System.out.println ("Client deny" + getName ());
        close();
    }

    void msgFormatError(String msg) {
        System.out.println ("Error" + getName ());
        close();
    }

}
