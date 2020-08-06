package com.geekbrains.cloud_storage.client;

import com.geekbrains.common.common.FileFunction;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.*;

public class ProtoHandler extends ChannelInboundHandlerAdapter {

    private String nameFile;
    private int id;

    public ProtoHandler(int id, String nameFile) {
        this.id = id;
        this.nameFile = nameFile;
    }

    public enum State {
        IDLE, FILE
    }
    private State currentState = State.IDLE;
    private long fileLength;
    private long receivedFileLength;



    private BufferedOutputStream out;
    private String clientFilesPath ="./common/src/main/resources/clientFiles/user";

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);
        while (buf.readableBytes() > 0) {
            if (currentState == State.IDLE) {
                if (buf.readableBytes() >= 8) {
                    fileLength = buf.readLong();
                    System.out.println("STATE: File length received - " + fileLength);
                    String path = clientFilesPath + id + "/";
                    FileFunction.createDirectory (path);
                    out = new BufferedOutputStream (new FileOutputStream (path + nameFile));
                    currentState = State.FILE;
                    receivedFileLength = 0L;
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

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}
