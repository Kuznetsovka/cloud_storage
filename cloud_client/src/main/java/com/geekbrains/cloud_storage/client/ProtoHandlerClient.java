package com.geekbrains.cloud_storage.client;

import com.geekbrains.common.common.FileFunction;
import com.geekbrains.common.common.ProtoAction;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.*;

public class ProtoHandlerClient extends ChannelInboundHandlerAdapter implements ProtoAction {

    private String nameFile;
    private int id;

    public ProtoHandlerClient(int id) {
        this.id = id;
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
                readLongFile (buf);
            }

            if (currentState == State.FILE) {
                String path = clientFilesPath + id + "/";
                FileFunction.createDirectory (path);
                try {out = new BufferedOutputStream (new FileOutputStream (path + nameFile));
                } catch (FileNotFoundException e) {
                    e.printStackTrace ();
                }
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
    public void readLongFile(ByteBuf buf)  {
        if (buf.readableBytes() >= 8) {
            fileLength = buf.readLong();
            System.out.println("STATE: File length received - " + fileLength);
            currentState = State.FILE;
            receivedFileLength = 0L;
        }
    }

    @Override
    public void writeFile(ByteBuf buf) throws IOException {
        String path = clientFilesPath + id + "/";
        FileFunction.createDirectory (path);
        try {out = new BufferedOutputStream (new FileOutputStream (path + nameFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace ();
        }
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
    protected void setFileName(String s){
        nameFile = s;
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
    public void readIDUser(ByteBuf buf) {

    }

    @Override
    public void readCommand(ByteBuf buf) {

    }

}
