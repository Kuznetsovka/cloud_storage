package com.geekbrains.cloud_storage.server.IOSolution.NIO;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Iterator;

import static java.lang.Thread.sleep;

public class NIOServer implements Runnable {
    private String serverFilePath = "./common/src/main/resources/serverFilesNIO";
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private String userName;
    private boolean isRunning = true;
    String fileName=null;
    String command=null;
    Long fileSize = null;
    private static int cnt = 1;
    private SocketChannel ch;
    private ByteBuffer buf = ByteBuffer.allocate(1024);
    private ByteBuffer lengthBuf = ByteBuffer.allocate(8);
    StringBuilder sb = new StringBuilder();
    private final ByteBuffer welcomeBuf = ByteBuffer.wrap("Клиент подключился!\n".getBytes());
    private final ByteBuffer signalBuf = ByteBuffer.wrap("OK".getBytes());
    private int step;
    //private ByteBuffer signalBuf = ByteBuffer.allocate(1);

    public NIOServer() throws IOException {
        this.serverSocketChannel = ServerSocketChannel.open ();
        this.serverSocketChannel.socket ().bind (new InetSocketAddress (8189));
        this.serverSocketChannel.configureBlocking (false);
        this.selector = Selector.open ();
        this.serverSocketChannel.register (selector, SelectionKey.OP_ACCEPT);
    }

    @Override
    public void run() {
        try {
            System.out.println("Сервер запущен (Порт: 8189)");
            Iterator<SelectionKey> iter;
            SelectionKey key;
            signalBuf.put ((byte) -1);
            while (this.serverSocketChannel.isOpen()) {
                selector.select();
                iter = this.selector.selectedKeys().iterator();
                while (iter.hasNext()) {
                    key = iter.next();
                    iter.remove();
                    if (key.isAcceptable()) this.handleAccept(key);
                    if (key.isReadable()) {
                        try {
                            this.handleRead(key);
                        } catch (InterruptedException e) {
                            e.printStackTrace ();
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void handleAccept(SelectionKey key) throws IOException {
        SocketChannel sc = ((ServerSocketChannel) key.channel()).accept();
        userName = "user" + cnt++;
        sc.configureBlocking(false);
        sc.register(selector, SelectionKey.OP_READ, userName);
        Path path = Paths.get (serverFilePath,userName);
        if (Files.notExists (path)) {
            Files.createDirectories (path);
        }
        sc.write(welcomeBuf);
        welcomeBuf.rewind();
        System.out.println("Подключился новый клиент " + userName);
    }

    private void handleRead(SelectionKey key) throws IOException, InterruptedException {
        SocketChannel ch = (SocketChannel) key.channel ();
        System.out.println ("read key");
        buf.clear ();
        int count = ch.read (buf);
        if (count == -1) {
            ch.close ();
            return;
        }
        buf.flip ();
        command = (command == null) ? getStr () : command;
        switch (command) {
            case "upload":
                upload (key, ch);
                break;
            case "download":
        }
    }

    private void upload(SelectionKey key, SocketChannel ch) throws IOException {
        fileName = (fileName == null)?getStr ():fileName;
        ch.write (signalBuf);
        if ( step++ == 1) {
            ch.read (lengthBuf);
            fileSize = lengthBuf.getLong ();
            lengthBuf.clear();
            lengthBuf.flip();
        }
        Path path = Paths.get (serverFilePath + "/" + key.attachment () + "/" + fileName);
        if (fileSize!=null && step==3)
            try (FileChannel fileChannel = FileChannel.open (path, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND)) {
                int offset = 0;
                while (true) {
                    if (offset == fileSize) {
                        break;
                    }
                    byte[] response = receiveByteArray (ch);
                    int bytesRead = offset + response.length >= fileSize ? (int) (fileSize - offset) : response.length;
                    fileChannel.write (ByteBuffer.wrap (Arrays.copyOfRange (response, 0, bytesRead)), offset);
                    offset += bytesRead;
                }
            } catch (Exception e) {
                System.out.println ("Error");
            }
    }

    private String getStr() {
        sb.setLength (0);
        byte[] arr = buf.array ();
        for (int i = this.buf.position (); i < arr.length; i++) {
            if (arr[i] == -1) {
                this.buf.position (i+1);
                if(this.buf.position () == buf.limit()) {
                    buf.clear ();
                    buf.flip ();
                }
                return String.valueOf (sb);
            } else {
                char chr = (char) this.buf.get ();
                sb.append (chr);
            }
        }
        return null;
    }

    private Long getLong() {
        this.buf.flip ();
        sb.setLength (0);
        byte[] arr = buf.array ();
        for (int i = 0; i < arr.length-1; i++) {
            if (arr[i] == 17) {
                this.buf.get ();
                this.buf.compact ();
                return Long.getLong (String.valueOf (sb));
            } else {
                int ii = this.buf.getInt ();
                sb.append (ii);
            }
        }
        return null;
    }


    private byte[] receiveByteArray(SocketChannel channel) throws IOException {
        ByteBuffer readBuffer = ByteBuffer.allocate(1024); //size = 1460
        channel.read(readBuffer); //label 1
        readBuffer.flip();
        return readBuffer.array();
    }

    public static void main(String[] args) throws IOException {
        new Thread(new NIOServer ()).start();
    }

}

/*
    private void broadcastMessage(String msg) throws IOException {
        ByteBuffer msgBuf = ByteBuffer.wrap(msg.getBytes());
        for (SelectionKey key : selector.keys()) {
            if (key.isValid() && key.channel() instanceof SocketChannel) {
                SocketChannel sch = (SocketChannel) key.channel();
                sch.write(msgBuf);
                msgBuf.rewind();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new Thread(new NioChatServerExample()).start();
    }
}
*/