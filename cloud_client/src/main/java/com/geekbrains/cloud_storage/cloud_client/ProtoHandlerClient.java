package com.geekbrains.cloud_storage.cloud_client;

import com.geekbrains.cloud_storage.common.AppModel;
import com.geekbrains.cloud_storage.common.FileFunction;
import com.geekbrains.cloud_storage.common.FileInfo;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ProtoHandlerClient extends ChannelInboundHandlerAdapter {

    private String nameFile;
    private String clientFilesPath;
    private AppModel model;

    public ProtoHandlerClient(AppModel model) {
        this.model = model;
    }

    public void setFileName(String s){
        nameFile = s;
    }

    public void setClientFilesPath(String clientFilesPath) {
        this.clientFilesPath = clientFilesPath;
    }

    public enum State {
        IDLE, FILE
    }
    private State currentState = State.IDLE;
    private long fileLength;
    private long receivedFileLength;
    private BufferedOutputStream out;
    public static List<FileInfo> listFileServer= new ArrayList<> ();

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        Thread.sleep (500);
        model.setText4 ("");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FileInfo) {
            listFileServer.add ((FileInfo) msg);
        } else {
            ByteBuf buf = ((ByteBuf) msg);
            readFile (buf);
        }
    }

    private void readFile(ByteBuf buf) throws IOException {
        while (buf.readableBytes() > 0) {
            if (currentState == State.IDLE) {
                readLongFile (buf);
            }
            if (currentState == State.FILE) {
                writeFile(buf);
            }
        }
        if (buf.readableBytes() == 0) {
            buf.release();
        }
    }

    public void readLongFile(ByteBuf buf)  {
        if (buf.readableBytes() >= 8) {
            fileLength = buf.readLong();
            System.out.println("STATE: File length received - " + fileLength);
            currentState = State.FILE;
            receivedFileLength = 0L;
        }
        String path = clientFilesPath;
        FileFunction.createDirectory (path);
        try {out = new BufferedOutputStream (new FileOutputStream (String.valueOf (Paths.get(path,nameFile))));
        } catch (FileNotFoundException e) {
            e.printStackTrace ();
        }
    }

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
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}
