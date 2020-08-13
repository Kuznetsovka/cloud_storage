package com.geekbrains.cloud_storage.cloud_server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

public class ProtoServer {

    static ChannelFuture f;

    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(
                                    //new ObjectDecoder (1024 * 1024 * 100, ClassResolvers.cacheDisabled(null)),
                                    new ObjectEncoder (),
                                    new AuthHandler (),
                                    new ProtoHandlerServer ());
                        }
                    });
            System.out.println("Сервер запущен");
            f = b.bind(8189).sync();
            f.channel().closeFuture().sync();
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
