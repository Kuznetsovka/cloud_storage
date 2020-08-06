package com.geekbrains.cloud_storage.server.Netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import org.apache.logging.log4j.core.net.DatagramOutputStream;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ProtoHandler extends ChannelInboundHandlerAdapter {


    private int sendFileLength;

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
    private BufferedInputStream in;
    private String serverFilesPath ="./common/src/main/resources/serverFiles/user";
    private String fileName;

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
                } else if (readed == SIGNAL_DOWNLOAD) {
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
                    byte[] fileNameByte = new byte[nextLength];
                    buf.readBytes(fileNameByte);
                    fileName = new String(fileNameByte, "UTF-8");
                    System.out.println("STATE: Filename received - " + fileName);
                    String path = serverFilesPath + id_name + "/";
                    if (command == SIGNAL_UPLOAD) {
                        createDirectory (path);
                        out = new BufferedOutputStream (new FileOutputStream (path + fileName));
                    } else {
                        in = new BufferedInputStream (new FileInputStream (path + fileName));
                    }
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
                } else {
                    File file = new File (serverFilesPath + id_name + "/" + fileName);
                    if (file.exists ()) {
                        System.out.println ("send OK");
                        ctx.channel().writeAndFlush("OK\n");

                        long len = file.length ();
                        buf = ByteBufAllocator.DEFAULT.directBuffer(8);
                        buf.writeLong(len);
                        ctx.channel().writeAndFlush(buf);

                        Path path = Paths.get(file.getPath ());
                        //FileRegion region = new DefaultFileRegion(path.toFile(), 0, Files.size(path));
                        FileInputStream fis = new FileInputStream (file);
                        byte[] buffer = new byte[1024];
                        ByteBufAllocator al = new PooledByteBufAllocator();
                        System.out.println ("send file");
                        while (fis.available () > 0) {
                            int count = fis.read (buffer);
                            ByteBuf bufFile = al.buffer(buffer.length);
                            ctx.channel ().writeAndFlush(bufFile);
                            bufFile.release ();
                            System.out.print ("=");
                        }
                        currentState = State.IDLE;
                        System.out.print ("/");
                    } else {
                        ctx.writeAndFlush ("File not exists\n");
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
