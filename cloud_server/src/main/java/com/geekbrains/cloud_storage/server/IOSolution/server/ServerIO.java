package com.geekbrains.cloud_storage.server.IOSolution.server;

import java.net.ServerSocket;
import java.net.Socket;

public class ServerIO {

    private boolean isRunning = true;
    //private ArrayList<FileHandler> handlers;

    public void stop() {
        isRunning = false;
    }

    public ServerIO() {

        try(ServerSocket server = new ServerSocket(8189)) {
//            handlers = new ArrayList<> ();
            System.out.println("server started!");
            while (isRunning) {
                Socket conn = server.accept();
                System.out.println("client accepted!");
//                handlers.add(new FileHandler(conn));
                new Thread(new FileHandler(conn)).start();
//                boolean connect = false;
//                for (FileHandler handler : handlers) {
//                    if (handler.isRunning ()){
//                        connect = true;
//                    }
//                }
//                isRunning = connect;
            }
            System.out.println("server stopped!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new ServerIO();
    }
}
