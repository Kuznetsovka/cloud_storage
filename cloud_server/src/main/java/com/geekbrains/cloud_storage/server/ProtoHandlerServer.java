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

    private int loginLength;

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
                    readCommand (buf);
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

            List<FileInfo> userPath = Files.list (p).map (path -> new FileInfo (path)).collect (Collectors.toList ());

            ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer (1);
            buf.writeByte (SIGNAL_UPDATE);
            ctx.write (buf);
            ctx.flush ();

            buf = ByteBufAllocator.DEFAULT.directBuffer (userPath.size ());
            buf.writeInt (userPath.size ());
            ctx.write (buf);
            ctx.flush ();

            ctx.pipeline().addFirst (new ObjectEncoder ());
            for (FileInfo fileInfo : userPath) {
                ctx.write(fileInfo);
                System.out.println (fileInfo.getFilename ());
            }
            ctx.flush ();
            currentState = State.IDLE;
            userPath.clear ();
            System.out.println ("Remove Encoder");
            ctx.pipeline().removeFirst ();

        } catch (IOException e) {
            e.printStackTrace ();
        }

    }

    @Override
    public void writeFile(ByteBuf buf) throws IOException {
        while (buf.readableBytes() > 0) {
            out.write(buf.readByte());
            receivedFileLength++;
            if (fileLength == receivedFileLength) {
                currentState = State.UPDATE;
                System.out.println("File received");
                out.close();
                break;
            }
        }
    }

    @Override
    public void readLongFile(ByteBuf buf) {
        if (command==SIGNAL_UPLOAD){
            if (buf.readableBytes() >= 8) {
                fileLength = buf.readLong();
                System.out.println("STATE: File length received - " + fileLength);
                currentState = State.FILE;
            }
        }
    }

    @Override
    public boolean readNameFile(ChannelHandlerContext ctx, ByteBuf buf) throws IOException {
        if (buf.readableBytes() >= nextLength) {
            byte[] fileNameByte = new byte[nextLength];
            buf.readBytes(fileNameByte);
            fileName = new String(fileNameByte, StandardCharsets.UTF_8);
            if (command == SIGNAL_DOWNLOAD) {
                System.out.println("STATE: Filename downloading - " + fileName);
                sending (ctx);
                currentState = State.IDLE;
                return true;
            }
            String path = String.valueOf (Paths.get(PATH_SERVER,login));
            System.out.println("STATE: Filename uploading - " + fileName);
            FileFunction.createDirectory (path);
            out = new BufferedOutputStream (new FileOutputStream (path + "/" + fileName));
            currentState = State.FILE_LENGTH;
        }
        return false;
    }

    @Override
    public void readLengthNameFile(ByteBuf buf) {
        if (buf.readableBytes() >= 4) {
            nextLength = buf.readInt();
            System.out.println("STATE: Get filename length " + nextLength);
            currentState = State.NAME;
        }
    }

    @Override
    public void readCommand(ByteBuf buf) {
        byte readed = buf.readByte();
        if (readed == SIGNAL_UPLOAD ||readed == SIGNAL_DOWNLOAD) {
            command = (readed == SIGNAL_UPLOAD)?SIGNAL_UPLOAD:SIGNAL_DOWNLOAD;
            currentState = State.NAME_LENGTH;
            receivedFileLength = 0L;
            System.out.println("STATE: " + ((command == SIGNAL_UPLOAD)?"DOWNLOAD":"UPLOAD"));
        } else if (readed == SIGNAL_UPDATE) {
            currentState = State.UPDATE;
            System.out.println("STATE: UPDATE");
        } else {
            System.out.println("ERROR: Invalid first byte - " + readed);
        }
    }

    private void sending(ChannelHandlerContext ctx) throws IOException {
        ProtoFileSender.sendFile (Paths.get (PATH_SERVER, login, fileName),  SENDER.SERVER, false,ctx.channel (), future -> {
            if (!future.isSuccess ()) {
                future.cause ().printStackTrace ();
                ProtoServer.stop();
            }
            if (future.isSuccess ()) {
                System.out.println ("Файл успешно передан");
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
        cause.printStackTrace();
        ctx.close();
    }

}
