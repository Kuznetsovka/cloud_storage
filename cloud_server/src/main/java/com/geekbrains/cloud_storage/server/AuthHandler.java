package com.geekbrains.cloud_storage.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentLinkedDeque;

public class  AuthHandler extends ChannelInboundHandlerAdapter {
    private boolean authOk = false;
    private int nextLength;
    private String str;
    private String login;
    private String password;

    private static ConcurrentLinkedDeque<SocketChannel> clients = new ConcurrentLinkedDeque<> ();
    private int id;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        SqlClient.connect(SqlClient.Type.SQLite);
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
        if (authOk==true){
            ctx.fireChannelRead (buf);
            return;
        }
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
            buf = ByteBufAllocator.DEFAULT.directBuffer (4);
            buf.writeInt (login.length ());
            ctx.fireChannelRead (buf);
            buf = ByteBufAllocator.DEFAULT.directBuffer (login.length ());
            buf.writeBytes (login.getBytes (StandardCharsets.UTF_8));
            ctx.fireChannelRead (buf);
            password = str.split(" ")[2];
            id = SqlClient.getIdUser (login, password);
            if (id==0){
                closeChannel (ctx);
            }
            System.out.println("Подключился клиент id = " + id);
            authOk = true;
            str = "";
        } else {
            closeChannel (ctx);
        }
    }

    private void closeChannel(ChannelHandlerContext ctx) {
        ctx.channel ().close ();
        System.out.println ("Не корректная авторизация!");
    }
}

