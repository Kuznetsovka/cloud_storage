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
    private String login;
    private String password;
    private static ConcurrentLinkedDeque<SocketChannel> clients = new ConcurrentLinkedDeque<> ();
    private int id;
    private ProtoHandlerServer protoHandler = new ProtoHandlerServer();

    private class  AuthHandler extends ChannelInboundHandlerAdapter {
        private boolean authOk = false;
        private ByteBuf buf;
        private int nextLength;
        private String str;

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            SqlClient.connect();
            clients.add((SocketChannel) ctx.channel());
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            SqlClient.disconnect ();
            clients.remove ((SocketChannel) ctx.channel());
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg){
            ByteBuf buf = ((ByteBuf) msg);
            if (buf.readableBytes() >= 4) {
                nextLength = buf.readInt();
            }
            if (buf.readableBytes() >= nextLength) {
                byte[] fileNameByte = new byte[nextLength];
                buf.readBytes(fileNameByte);
                str = new String(fileNameByte, StandardCharsets.UTF_8);
            }
            // #auth login password
            if (str.split(" ")[0].equals("#auth")) {
                login = str.split(" ")[1];
                password = str.split(" ")[2];
                id = SqlClient.getIdUser (login, password);
                if (id==0){
                    closeChannel (ctx);
                }
                protoHandler.setLogin (login);
                System.out.println("Подключился клиент id = " + id);
                authOk = true;
                ctx.channel ().pipeline ().remove(this);
            } else {
                closeChannel (ctx);
            }
        }
    }

    private void closeChannel(ChannelHandlerContext ctx) {
        ctx.channel ().close ();
        System.out.println ("Не корректная авторизация!");
    }

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
                            ch.pipeline().addLast(new AuthHandler (), protoHandler);
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
