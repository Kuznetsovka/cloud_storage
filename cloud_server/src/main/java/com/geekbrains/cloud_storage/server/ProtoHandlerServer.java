package com.geekbrains.cloud_storage.server;

import com.geekbrains.common.common.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

class ProtoHandlerServer extends ChannelInboundHandlerAdapter implements ProtoAction,Config {

    public enum State {
        LOGIN,IDLE, NAME_LENGTH, NAME, FILE_LENGTH, FILE
    }
    private byte command;
    private State currentState = State.LOGIN;
    private String login;
    private int nextLength;
    private long fileLength;
    private long receivedFileLength;
    private BufferedOutputStream out;
    private String serverFilesPath ="./common/src/main/resources/serverFiles/";
    private String fileName;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);
        while (buf.readableBytes() > 0) {
            switch (currentState) {
                case LOGIN:
                    readLogin (buf);
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
            }
        }
        if (buf.readableBytes () == 0) {
            buf.release ();
        }
    }

    private void readLogin(ByteBuf buf) {
        byte[] bytes = new byte[buf.readInt ()];
        buf.readBytes(bytes);
        login = new String(bytes, StandardCharsets.UTF_8);
        currentState = State.IDLE;
    }

    @Override
    public void writeFile(ByteBuf buf) throws IOException {
        while (buf.readableBytes() > 0) {
            out.write(buf.readByte());
            receivedFileLength++;
            if (fileLength == receivedFileLength) {
                currentState = State.IDLE;
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
            String path = String.valueOf (Paths.get(serverFilesPath,login));
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
            System.out.println("STATE: Start file receiving");

        } else {
            System.out.println("ERROR: Invalid first byte - " + readed);
        }
    }

    private void sending(ChannelHandlerContext ctx) throws IOException {
        ProtoFileSender.sendFile (Paths.get (serverFilesPath, login, fileName),  SENDER.SERVER, false,ctx.channel (),future -> {
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
