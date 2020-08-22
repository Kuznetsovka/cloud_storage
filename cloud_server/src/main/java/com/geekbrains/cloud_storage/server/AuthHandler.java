package com.geekbrains.cloud_storage.server;

import com.geekbrains.common_files.common.MyLogger;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentLinkedDeque;

public class  AuthHandler extends ChannelInboundHandlerAdapter {
    public enum State {
        AUTH_LENGTH,AUTH,AUTH_STR
    }
    private boolean authOk = false;
    private int nextLength;
    private State currentState = State.AUTH_LENGTH;
    private String str;

    private static ConcurrentLinkedDeque<SocketChannel> clients = new ConcurrentLinkedDeque<> ();
    private int id;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        clients.add((SocketChannel) ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        SqlClient.disconnect ();
        if (id!=0) {
            MyLogger.logInfo ("Клиент [" + id + "] вышел!");
            System.out.println ("Клиент [" + id + "] вышел!");
        } else {
            MyLogger.logInfo ("Клиент не найдет в базе!");
            System.out.println ("Клиент не найдет в базе!");
        }
        clients.remove ((SocketChannel) ctx.channel());

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg){
        ByteBuf buf = ((ByteBuf) msg);
        if (authOk) {
            ctx.fireChannelRead (buf);
            return;
        }
        while (buf.readableBytes() > 0 || !authOk) {
            switch (currentState) {
                case AUTH_LENGTH:
                    readLengthStr (buf);
                    break;
                case AUTH_STR:
                    readAuthStr (buf);
                    break;
                case AUTH:
                    auth (ctx);
                    break;
            }
        }
        if (buf.readableBytes () == 0) {
            buf.release ();
        }
    }

    private void readLengthStr(ByteBuf buf) {
        if (buf.readableBytes() >= 4) {
            nextLength = buf.readInt();
            currentState = State.AUTH_STR;
        }
        System.out.println (nextLength);
    }

    private void readAuthStr(ByteBuf buf) {
        if (buf.readableBytes() >= nextLength) {
            byte[] fileNameByte = new byte[nextLength];
            buf.readBytes(fileNameByte);
            str = new String(fileNameByte, StandardCharsets.UTF_8);
            currentState = State.AUTH;
        }
        System.out.println (str);
    }

    private void auth(ChannelHandlerContext ctx) {
        ByteBuf buf;// #auth login password
        if (str.split(" ")[0].equals("#auth")) {
            String login = str.split (" ")[1];
            String password = str.split (" ")[2];
            id = SqlClient.getIdUser (login, password);
            if (id==0){
                closeChannel (ctx);
                return;
            }
            buf = ByteBufAllocator.DEFAULT.directBuffer (4);
            buf.writeInt (login.length ());
            ctx.fireChannelRead (buf);
            buf = ByteBufAllocator.DEFAULT.directBuffer (login.length ());
            buf.writeBytes (login.getBytes (StandardCharsets.UTF_8));
            ctx.fireChannelRead (buf);
            MyLogger.logInfo ("Подключился клиент id = " + id);
            System.out.println("Подключился клиент id = " + id);
            authOk = true;
            str = "";
        } else {
            closeChannel (ctx);
        }
    }

    private void closeChannel(ChannelHandlerContext ctx) {
        ctx.channel ().close ();
        MyLogger.logError ("Не корректная авторизация!");
        System.out.println ("Не корректная авторизация!");
    }
}

