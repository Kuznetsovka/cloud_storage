package com.geekbrains.cloud_storage.server;

import com.geekbrains.common_files.common.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

class ProtoHandlerServer extends ChannelInboundHandlerAdapter implements ProtoAction, Config {
    public enum State {
        LOGIN_LENGTH,LOGIN,IDLE, NAME_LENGTH, NAME, FILE_LENGTH, FILE,UPDATE
    }
    private byte command;
    private State currentState = State.LOGIN_LENGTH;
    private String login;
    private int nextLength;
    private long fileLength;
    private long receivedFileLength;
    private BufferedOutputStream out;
    private String fileName;
    private int loginLength;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);
        while (buf.readableBytes() > 0) {
            switch (currentState) {
                case LOGIN_LENGTH:
                    readLoginLength (buf);
                    break;
                case LOGIN:
                    readLogin (ctx,buf);
                    break;
                case IDLE:
                    readCommand (ctx,buf);
                    break;
                case NAME_LENGTH:
                    readLengthNameFile (buf);
                    break;
                case NAME:
                    readNameFile (ctx, buf);
                    break;
                case FILE_LENGTH:
                    readLongFile (buf);
                    break;
                case FILE:
                    writeFile (buf);
                    break;
                case UPDATE:
                    writeFileList (ctx,Paths.get (PATH_SERVER, login));
                    break;
            }
        }
        if (buf.readableBytes () == 0) {
            buf.release ();
        }
    }

    private void readLoginLength(ByteBuf buf) {
        if (buf.readableBytes() >= 4) {
            loginLength = buf.readInt();
            MyLogger.logInfo ("STATE: Get login length " + loginLength);
            System.out.println("STATE: Get login length " + loginLength);
            currentState = State.LOGIN;
        }
    }

    private void readLogin(ChannelHandlerContext ctx,ByteBuf buf) {
        if (buf.readableBytes() >= loginLength) {
            byte[] bytes = new byte[loginLength];
            buf.readBytes (bytes);
            login = new String (bytes, StandardCharsets.UTF_8);
            Path path = Paths.get (PATH_SERVER, login);
            FileFunction.createDirectories (String.valueOf (path));
            try {
            if (FileFunction.isDirEmpty(path))
                Files.createFile (Paths.get (path + "/" + "Добро пожаловать.txt"));
            } catch (IOException e) {
                    e.printStackTrace ();
            }
            writeFileList (ctx, Paths.get (PATH_SERVER, login));
            currentState = State.IDLE;
        }
    }

    private void writeFileList(ChannelHandlerContext ctx, Path p) {
        try {
            List<FileInfo> userPath = Files.list (p).map (FileInfo::new).collect (Collectors.toList ());
            System.out.println ("Write file list!");
            ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer (1);
            buf.writeByte (SIGNAL_UPDATE);
            ctx.writeAndFlush (buf);
            buf = ByteBufAllocator.DEFAULT.directBuffer (userPath.size ());
            buf.writeInt (userPath.size ());
            ctx.writeAndFlush (buf);

            ctx.pipeline().addFirst (new ObjectEncoder ());
            for (FileInfo fileInfo : userPath) {
                ctx.writeAndFlush (fileInfo);
                System.out.println (fileInfo.getFilename ());
            }
            currentState = State.IDLE;
            userPath.clear ();
            ctx.pipeline().removeFirst ();
        } catch (IOException e) {
            e.printStackTrace ();
        }
    }

    public void writeFile(ByteBuf buf) throws IOException {
        while (buf.readableBytes() > 0) {
            out.write(buf.readByte());
            receivedFileLength++;
            if (fileLength == receivedFileLength) {
                currentState = State.UPDATE;
                System.out.println("Файл "+ fileName +" получен");
                MyLogger.logInfo ("Файл "+ fileName +" получен");
                out.close();
                break;
            }
        }
    }

    public void readLongFile(ByteBuf buf) {
        if (command==SIGNAL_UPLOAD){
            if (buf.readableBytes() >= 8) {
                fileLength = buf.readLong();
                System.out.println("Статус: Размер файла - " + fileLength);
                MyLogger.logInfo ("Статус: Размер файла - " + fileLength);
                currentState = State.FILE;
            }
        }
    }

    public boolean readNameFile(ChannelHandlerContext ctx, ByteBuf buf) throws IOException {
        if (buf.readableBytes() >= nextLength) {
            byte[] fileNameByte = new byte[nextLength];
            buf.readBytes(fileNameByte);
            fileName = new String(fileNameByte, StandardCharsets.UTF_8);
            if (command == SIGNAL_DOWNLOAD) {
                System.out.println("Статус: Файл загружен - " + fileName);
                MyLogger.logInfo ("Статус: Файл загружен - " + fileName);
                sending (ctx);
                currentState = State.IDLE;
                return true;
            }
            String path = String.valueOf (Paths.get(PATH_SERVER,login));
            System.out.println("Статус: Файл скачен - " + fileName);
            MyLogger.logInfo ("Статус: Файл скачен - " + fileName);
            FileFunction.createDirectory (path);
            out = new BufferedOutputStream (new FileOutputStream (path + "/" + fileName));
            currentState = State.FILE_LENGTH;
        }
        return false;
    }

    public void readLengthNameFile(ByteBuf buf) {
        if (buf.readableBytes() >= 4) {
            nextLength = buf.readInt();
            System.out.println("Статус: Длина имени файла " + nextLength);
            MyLogger.logInfo ("Статус: Длина имени файла " + fileName);
            currentState = State.NAME;
        }
    }

    public void readCommand(ChannelHandlerContext ctx, ByteBuf buf) {
        byte readed = buf.readByte();
        if (readed == SIGNAL_UPLOAD ||readed == SIGNAL_DOWNLOAD) {
            command = (readed == SIGNAL_UPLOAD)?SIGNAL_UPLOAD:SIGNAL_DOWNLOAD;
            currentState = State.NAME_LENGTH;
            receivedFileLength = 0L;
            System.out.println("Статус: " + ((command == SIGNAL_UPLOAD)?"UPLOAD":"DOWNLOAD"));
        } else if (readed == SIGNAL_UPDATE) {
            System.out.println("STATE: UPDATE");
            currentState = State.UPDATE;
        } else {
            MyLogger.logError ("Ошибка: Не верный сигнальный байт - " + readed);
            System.out.println("Ошибка: Не верный сигнальный байт - " + readed);
        }
    }

    private void sending(ChannelHandlerContext ctx) throws IOException {
        ProtoFileSender.sendFile (Paths.get (PATH_SERVER, login, fileName),  SENDER.SERVER, false,ctx.channel (), future -> {
            if (!future.isSuccess ()) {
                future.cause ().printStackTrace ();
                ProtoServer.stop();
            }
            if (future.isSuccess ()) {
                MyLogger.logInfo ("Файл "+ fileName + " успешно передан");
                System.out.println ("Файл "+ fileName + " успешно передан");
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
        cause.printStackTrace();
        ctx.close();
    }

}
