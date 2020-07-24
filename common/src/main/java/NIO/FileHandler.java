package NIO;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class FileHandler implements Runnable {

    private String serverFilePath = "./common/src/main/resources/serverFiles";
    private boolean isRunning = true;
    private static int cnt = 1;
    private String userName;
    private Selector selector;

    public FileHandler() throws IOException {
        selector = Selector.open ();
        userName = "user" + cnt;
        cnt++;
        serverFilePath += "/" + userName;
        createDirectory(serverFilePath);
    }

    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public void run() {
        try {
            selector = Selector.open();
            Iterator<SelectionKey> iterator = selector.selectedKeys ().iterator ();
            while (iterator.hasNext ()) {
                SelectionKey key = iterator.next ();
                if (key.isReadable ()) {
                    // TODO: 7/23/2020 fileStorage handle
                    System.out.println ("read key");
                    ByteBuffer buffer = ByteBuffer.allocate (80);
                    int count = ((SocketChannel) key.channel ()).read (buffer);
                    if (count == -1) {
                        key.channel ().close ();
                        isRunning = false;
                    }
                    buffer.flip ();
                    StringBuilder s = new StringBuilder ();
                    while (buffer.hasRemaining ()) {
                        s.append ((char) buffer.get ());
                    }
                    for (SelectionKey key1 : selector.keys ()) {
                        if (key1.channel () instanceof SocketChannel && key1.isReadable ()) {
                            ((SocketChannel) key1.channel ()).write (ByteBuffer.wrap (s.toString ().getBytes ()));
                        }
                    }
                    System.out.println ();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void createDirectory(String dirName) {
        File file = new File(dirName);
        if (!file.exists()) {
            file.mkdir();
        }
    }
}
