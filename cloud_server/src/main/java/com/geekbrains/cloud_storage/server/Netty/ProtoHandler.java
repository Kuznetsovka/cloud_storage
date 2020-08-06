package com.geekbrains.cloud_storage.server.Netty;

import com.geekbrains.common.common.Config;
import com.geekbrains.common.common.FileFunction;
import com.geekbrains.common.common.ProtoFileSender;
import com.geekbrains.common.common.SENDER;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

public class ProtoHandler extends ChannelInboundHandlerAdapter implements Config {

    public enum State {
        IDLE, ID_USER, NAME_LENGTH, NAME, FILE_LENGTH, FILE
    }
    private byte command;
    private State currentState = State.IDLE;
    private int id_name;
    private int nextLength;
    private long fileLength;
    private long receivedFileLength;
    private BufferedOutputStream out;
    private BufferedInputStream in;
    private String serverFilesPath ="./common/src/main/resources/serverFiles/user";
    private String fileName;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);
        while (buf.readableBytes() > 0) {
            if (currentState == State.IDLE) {
                byte readed = buf.readByte();
                if (readed == SIGNAL_UPLOAD ||readed == SIGNAL_DOWNLOAD) {
                    command = (readed == SIGNAL_UPLOAD)?SIGNAL_UPLOAD:SIGNAL_DOWNLOAD;
                    currentState = State.ID_USER;
                    receivedFileLength = 0L;
                    System.out.println("STATE: Start file receiving");
                } else {
                    System.out.println("ERROR: Invalid first byte - " + readed);
                }
            }
            if (currentState == State.ID_USER) {
                if (buf.readableBytes() >= 4) {
                    id_name = buf.readInt();
                    System.out.println("STATE: Get id_name " + id_name);
                    currentState = State.NAME_LENGTH;
                }
            }

            if (currentState == State.NAME_LENGTH) {
                if (buf.readableBytes() >= 4) {
                    nextLength = buf.readInt();
                    System.out.println("STATE: Get filename length " + nextLength);
                    currentState = State.NAME;
                }
            }
            if (currentState == State.NAME) {
                if (buf.readableBytes() >= nextLength) {
                    byte[] fileNameByte = new byte[nextLength];
                    buf.readBytes(fileNameByte);
                    fileName = new String(fileNameByte, StandardCharsets.UTF_8);
                    if (command == SIGNAL_DOWNLOAD) {
                        System.out.println("STATE: Filename downloading - " + fileName);
                        sending (ctx);
                        currentState = State.IDLE;
                        break;
                    }
                    String path = serverFilesPath + id_name + "/";
                    FileFunction.createDirectory (path);
                    out = new BufferedOutputStream (new FileOutputStream (path + fileName));
                    currentState = State.FILE_LENGTH;
                }
            }
            if (currentState == State.FILE_LENGTH) {
                if (command==SIGNAL_UPLOAD){
                    if (buf.readableBytes() >= 8) {
                        fileLength = buf.readLong();
                        System.out.println("STATE: File length received - " + fileLength);
                        currentState = State.FILE;
                    }
                }
            }

            if (currentState == State.FILE) {
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
        }
        if (buf.readableBytes() == 0) {
            buf.release();
        }
    }

    private void sending(ChannelHandlerContext ctx) throws IOException {
        ProtoFileSender.sendFile (Paths.get (serverFilesPath + id_name, fileName), id_name,  SENDER.SERVER, false,ctx.channel (),future -> {
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
