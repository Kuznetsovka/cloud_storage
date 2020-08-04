package com.geekbrains.cloud_storage.server.Netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class ProtoHandler extends ChannelInboundHandlerAdapter {
    private String serverFilesPath ="./common/src/main/resources/serverFiles/user";

    public enum State {
        IDLE, ID_USER, NAME_LENGTH, NAME, FILE_LENGTH, FILE
    }
    private final byte SIGNAL_UPLOAD = 25;
    private final byte SIGNAL_DOWNLOAD = 17;
    private byte command;
    private State currentState = State.IDLE;
    private int id_name;
    private int nextLength;
    private long fileLength;
    private long receivedFileLength;
    private BufferedOutputStream out;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);
        while (buf.readableBytes() > 0) {
            if (currentState == State.IDLE) {
                byte readed = buf.readByte();
                if (readed == SIGNAL_UPLOAD) {
                    command = SIGNAL_UPLOAD;
                    currentState = State.ID_USER;
                    receivedFileLength = 0L;
                    System.out.println("STATE: Start file receiving");
                } else if (command == SIGNAL_DOWNLOAD) {
                    command = SIGNAL_DOWNLOAD;
                    currentState = State.ID_USER;
                    receivedFileLength = 0L;
                    System.out.println ("STATE: Start file receiving");
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
                    byte[] fileName = new byte[nextLength];
                    buf.readBytes(fileName);
                    System.out.println("STATE: Filename received - " + new String(fileName, "UTF-8"));
                    String path = serverFilesPath + id_name +"/";
                    createDirectory(path);
                    out = new BufferedOutputStream(new FileOutputStream(path + new String(fileName)));
                    if (command == SIGNAL_UPLOAD) currentState = State.FILE_LENGTH;
                }
            }

            if (currentState == State.FILE_LENGTH) {
                if (buf.readableBytes() >= 8) {
                    fileLength = buf.readLong();
                    System.out.println("STATE: File length received - " + fileLength);
                    currentState = State.FILE;
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

    public static void createDirectory(String dirName) {
        File file = new File(dirName);
        if (!file.exists()) {
            file.mkdir();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
