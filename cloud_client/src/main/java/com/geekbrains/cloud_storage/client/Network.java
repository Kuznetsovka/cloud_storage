package com.geekbrains.cloud_storage.client;

import com.geekbrains.cloud_storage.client.controllers.Controller;
import com.geekbrains.common_files.common.MyLogger;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;

import static com.geekbrains.cloud_storage.client.ProtoHandlerClient.isUpdateServer;
import static com.geekbrains.cloud_storage.client.ProtoHandlerClient.listFileServer;

public class Network {
    private static ProtoHandlerClient handle;
    private static Network ourInstance = new Network();

    public static ProtoHandlerClient getHandle() {
        return handle;
    }

    public static Network getInstance() {
        return ourInstance;
    }

    private static Channel currentChannel;

    public Channel getCurrentChannel() {
        return currentChannel;
    }

    public void start(Controller controller, CountDownLatch countDownLatch, String login, String password) {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap clientBootstrap = new Bootstrap();
            handle = new ProtoHandlerClient ();
            clientBootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress("localhost", 8189))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) {
                            socketChannel.pipeline().addLast(
                                    handle);
                            boolean isConnect = true;
                            controller.setConnect(isConnect);
                            currentChannel = socketChannel;
                        }

                    });
            ChannelFuture channelFuture = clientBootstrap.connect().sync();
            authorize (login, password,currentChannel);
            countDownLatch.countDown();
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            MyLogger.logInfo ("Нет соединения!");
            System.out.println ("Нет соединения!");
            countDownLatch.countDown ();
            boolean isConnect = false;
            controller.setConnect (isConnect);
        } finally {
            try {
                controller.clearServerPanel();
                stop();
                group.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
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

    public static void stop() {
        System.out.println ("Соединение разорвано");
        currentChannel.close();
    }
}
