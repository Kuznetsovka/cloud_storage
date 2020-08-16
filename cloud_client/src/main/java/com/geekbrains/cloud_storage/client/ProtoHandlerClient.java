package com.geekbrains.cloud_storage.client;

import com.geekbrains.common.common.AppModel;
import com.geekbrains.common.common.FileFunction;
import com.geekbrains.common.common.FileInfo;
import com.geekbrains.common.common.ProtoAction;
import com.sun.deploy.net.MessageHeader;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.geekbrains.common.common.Config.SIGNAL_DOWNLOAD;

public class ProtoHandlerClient extends ChannelInboundHandlerAdapter implements ProtoAction {

    private String nameFile;
    private String login;
    private String clientFilesPath;
    public static List<FileInfo> listFileServer= new ArrayList<> ();
    private int countFileList;
    private int listItem;
    private State currentState = State.IDLE;
    private long fileLength;
    private long receivedFileLength;
    private BufferedOutputStream out;
    private AppModel model;
    private ByteBuf buf;

    public enum State {
        IDLE,COUNT_LIST,UPDATE,LONG, FILE
    }
    public ProtoHandlerClient(AppModel model, String login) {
        this.model = model;
        this.login = login;
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (currentState!=State.UPDATE) {
            buf = ((ByteBuf) msg);
        } else {
            buf = ByteBufAllocator.DEFAULT.directBuffer (1);
            buf.writeInt (1);
        }
        while (buf.readableBytes() > 0) {
            switch (currentState) {
                case IDLE:
                    readCommand (buf);
                    break;
                case COUNT_LIST:
                    readInt (ctx,buf);
                    break;
                case UPDATE:
                    readUpdate (ctx,msg);
                    break;
                case LONG:
                    readLongFile (buf);
                    break;
                case FILE:
                    writeFile (buf);
                    break;
            }
        }
        if (buf.readableBytes() == 0) {
            buf.release();
        }
    }
        private void readUpdate(ChannelHandlerContext ctx,Object msg){
            if (msg instanceof FileInfo) {
                listItem++;
                buf.readInt();
                listFileServer.add ((FileInfo) msg);
                if (listItem == countFileList) {
                    listItem = 0;
                    currentState = State.IDLE;
                    model.setText4 (login);
                    ctx.pipeline ().removeFirst ();
                }
            }
        }

        private void readInt(ChannelHandlerContext ctx,ByteBuf buf) throws IOException {
            if (buf.readableBytes () >= 4) {
                countFileList = buf.readInt ();
                System.out.println ("STATE: Count list files " + countFileList);
                currentState = State.UPDATE;
                ctx.pipeline ().addFirst (new ObjectDecoder (1024 * 1024 * 100, ClassResolvers.cacheDisabled (null)));
            }
        }

        @Override
        public void readCommand(ByteBuf buf) {
            byte readed = buf.readByte ();
            if (readed != 15) {
                currentState = State.LONG;
            } else {
                currentState = State.COUNT_LIST;
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

    public void setFileName(String s){
        nameFile = s;
    }

    public void setClientFilesPath(String clientFilesPath) {
        this.clientFilesPath = clientFilesPath;
    }

}
