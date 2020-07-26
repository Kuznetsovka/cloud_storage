package NIO;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;

public class NIOServer {
    private String serverFilePath = "./common/src/main/resources/serverFilesNIO";
    private ServerSocketChannel server;
    private Selector selector;
    private boolean isRunning = true;
    private static int cnt = 1;
    private String userName;
    private SocketChannel channel;

    public NIOServer() throws IOException {
        server = ServerSocketChannel.open ();
        server.socket ().bind (new InetSocketAddress (8189));
        server.configureBlocking (false);
        selector = Selector.open ();
        server.register (selector, SelectionKey.OP_ACCEPT);
        System.out.println ("server started!");
        while (server.isOpen ()) {
            selector.select ();
            Iterator<SelectionKey> iterator = selector.selectedKeys ().iterator ();
            while (iterator.hasNext ()) {
                SelectionKey key = iterator.next ();
                iterator.remove ();
                if (key.isAcceptable ()) {
                    System.out.println ("client accepted");
                    userName = "user" + cnt++;
                    channel = ((ServerSocketChannel) key.channel ()).accept ();
                    channel.configureBlocking (false);
                    channel.register (selector, SelectionKey.OP_READ);
                    Path path = Paths.get (serverFilePath + "/" + userName + "/");
                    if (!Files.exists(path)) {
                        Files.createDirectories (path);
                    }
                    //new Thread (new FileHandler (iterator)).start ();
                }
                // Не получилось выделить в Handler, вроде все работает, а канал не приходит. Поэтому решил сделать в прямую.
                if (key.isReadable ()) {
                    System.out.println ("read key");
                    ByteBuffer buffer = ByteBuffer.allocate (1024);
                    int count = ((SocketChannel) key.channel ()).read (buffer);
                    if (count == -1) {
                        key.channel ().close ();
                        break;
                    }
                    buffer.flip ();
                    StringBuilder s = new StringBuilder ();
                    while (buffer.hasRemaining ()){
                        s.append((char) buffer.get ());
                    }
                    String[] arr = s.toString().split("&&");
                    String command = arr[0];
                    switch (command){
                        case ("#upload"):
                            String fileName = arr[1];
                            Path path = Paths.get (serverFilePath + "/" + userName + "/" + fileName);
                            try (FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
                                buffer.flip ();
                                fileChannel.transferFrom(channel, 0, Long.MAX_VALUE);
                            }
                        case ("#download"):
                            for (SelectionKey key1 : selector.keys ()) {
                                if (key1.channel () instanceof SocketChannel && key1.isReadable ()) {
                                    ((SocketChannel) key1.channel ()).write (ByteBuffer.wrap ("OK".getBytes ()));
                                }
                            }
                    }
                    System.out.println ();
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new NIOServer ();
    }
}
