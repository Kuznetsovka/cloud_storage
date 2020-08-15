package com.geekbrains.cloud_storage.cloud_client;

import com.geekbrains.cloud_storage.cloud_client.controllers.Controller;
import com.geekbrains.cloud_storage.common.AppModel;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;

public class Network {
    private static ProtoHandlerClient handle;
    private static Network ourInstance = new Network();
    private boolean isConnect = false;

    public static ProtoHandlerClient getHandle() {
        return handle;
    }

    public static Network getInstance() {
        return ourInstance;
    }

    private Channel currentChannel;

    public Channel getCurrentChannel() {
        return currentChannel;
    }

    public void start(Controller controller, CountDownLatch countDownLatch, String login, String password, AppModel model) {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap clientBootstrap = new Bootstrap ();
            handle = new ProtoHandlerClient (model);
                clientBootstrap.group (group)
                        .channel (NioSocketChannel.class)
                        .remoteAddress (new InetSocketAddress ("localhost", 8189))
                        .handler (new ChannelInitializer<SocketChannel> () {
                            @Override
                            protected void initChannel(SocketChannel socketChannel) {
                                socketChannel.pipeline ().addLast (
                                        new ObjectDecoder (1024 * 1024 * 100, ClassResolvers.cacheDisabled (null)),
                                        handle);
                                isConnect = true;
                                controller.setConnect (isConnect);
                                currentChannel = socketChannel;
                            }

                            @Override
                            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                System.out.println ("Нет соединения!");
                            }
                        });
            ChannelFuture channelFuture = clientBootstrap.connect ().sync ();
            authorize (login, password, currentChannel);
            countDownLatch.countDown ();
            channelFuture.channel ().closeFuture ().sync ();
        } catch (Exception e) {
            System.out.println ("Нет соединения!");
            countDownLatch.countDown ();
            isConnect = false;
            controller.setConnect (isConnect);
            return;
        } finally {
            try {
                group.shutdownGracefully ().sync ();
            } catch (InterruptedException e) {
                e.printStackTrace ();
            }
        }
    }

    private void authorize(String login, String password, Channel channel) {
        ByteBuf buf;
        String str = "#auth " + login + " " + password;

        byte[] strByte = str.getBytes (StandardCharsets.UTF_8);

        buf = ByteBufAllocator.DEFAULT.directBuffer (4);
        buf.writeInt (str.length ());
        channel.writeAndFlush (buf);

        buf = ByteBufAllocator.DEFAULT.directBuffer (str.length ());
        buf.writeBytes (strByte);
        channel.writeAndFlush (buf);
    }

    public void stop() {
        currentChannel.close();
    }
}
