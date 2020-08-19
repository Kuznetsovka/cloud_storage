package com.geekbrains.cloud_storage.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class ProtoServer {

    static ChannelFuture f;

    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new AuthHandler (), new ProtoHandlerServer ());
                        }
                    })
                    .childOption(ChannelOption.SO_KEEPALIVE, true);;
            System.out.println("Сервер запущен");
            f = b.bind(8189).sync();
            f.channel().closeFuture().sync();
        } catch (Exception e){
            e.getStackTrace ();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static void stop() {
            f.channel().close ();
    }

    public static void main(String[] args) throws Exception {
        new ProtoServer().run();
    }
}
