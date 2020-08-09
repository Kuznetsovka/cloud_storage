package com.geekbrains.cloud_storage.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;

public class Network {
    private static ProtoHandlerClient handle = new ProtoHandlerClient (Controller.id);
    private static Network ourInstance = new Network();
    private static boolean isConnect = false;
    public  static boolean isConnect() {
        return isConnect;
    }

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

    public void start(CountDownLatch countDownLatch, String login, String password) {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap clientBootstrap = new Bootstrap();
            clientBootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress("localhost", 8189))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) {
                            socketChannel.pipeline().addLast(handle);
                            isConnect = true;
                            currentChannel = socketChannel;
                        }
                    });
            ChannelFuture channelFuture = clientBootstrap.connect().sync();
            //authorize (login, password,currentChannel);
            countDownLatch.countDown();
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
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

    public void stop() {
        currentChannel.close();
    }
}
