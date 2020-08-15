package com.geekbrains.cloud_storage.client;

import com.geekbrains.common.common.AppModel;
import com.geekbrains.common.common.FileFunction;
import com.geekbrains.common.common.FileInfo;
import com.geekbrains.common.common.ProtoAction;
import com.sun.deploy.net.MessageHeader;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static com.geekbrains.common.common.Config.SIGNAL_DOWNLOAD;

public class ProtoHandlerClient extends ChannelInboundHandlerAdapter implements ProtoAction {

    private String nameFile;
    private String clientFilesPath;
    public static List<FileInfo> listFileServer= new ArrayList<> ();

    public void setFileName(String s){
        nameFile = s;
    }

    public void setClientFilesPath(String clientFilesPath) {
        this.clientFilesPath = clientFilesPath;
    }

    public enum State {
        IDLE, LONG,FILE
    }
    private State currentState = State.IDLE;
    private long fileLength;
    private long receivedFileLength;
    private BufferedOutputStream out;
    private AppModel model;

    public ProtoHandlerClient(AppModel model) {
        this.model = model;
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ctx.pipeline ().addFirst (new ObjectDecoder (1024 * 1024 * 100, ClassResolvers.cacheDisabled (null)));
        if (msg instanceof FileInfo) {
            listFileServer.add ((FileInfo) msg);//TODO
        } else {
            ctx.pipeline ().removeFirst ();
            buf = ((ByteBuf) msg);
            readFile (buf);
        }
    }

    private void readFile(ByteBuf buf) throws IOException {
        while (buf.readableBytes() > 0) {
            if (currentState == State.LONG) {
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


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        Thread.sleep (500);
        model.setText4 ("");
    }

    @Override
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
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    /****
     *
     * @param buf
     */

    @Override
    public boolean readNameFile(ChannelHandlerContext ctx, ByteBuf buf){
        return false;
    }

    @Override
    public void readLengthNameFile(ByteBuf buf) {

    }

    @Override
    public void readCommand(ByteBuf buf) {

    }

}
