package NIO;

import IO.Server;
import NIO.FileHandler;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class NIOServer {
    private ServerSocketChannel server;
    private Selector selector;
    private boolean isRunning = true;

    public NIOServer() throws IOException {
        server = ServerSocketChannel.open();
        server.socket().bind(new InetSocketAddress(8189));
        server.configureBlocking(false);
        selector = Selector.open();
        server.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("server started!");
        while (server.isOpen()) {
            selector.select();
            Iterator<SelectionKey> iterator = selector.selectedKeys ().iterator ();
            while (iterator.hasNext ()) {
                SelectionKey key = iterator.next ();
                iterator.remove ();
                if (key.isAcceptable ()) {
                    System.out.println ("client accepted");
                    SocketChannel channel = ((ServerSocketChannel) key.channel ()).accept ();
                    channel.configureBlocking (false);
                    channel.register (selector, SelectionKey.OP_READ);
                    channel.write (ByteBuffer.wrap ("Hello!".getBytes ()));
                    new Thread(new FileHandler ()).start();
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new NIOServer ();
    }
}
