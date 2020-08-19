package com.geekbrains.cloud_storage.client;

import com.geekbrains.common_files.common.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static com.geekbrains.cloud_storage.client.controllers.Controller.clientFilesPath;

public class ProtoHandlerClient extends ChannelInboundHandlerAdapter implements ProtoAction {

    private String nameFile;
    public static List<FileInfo> listFileServer = new ArrayList<> ();
    private int countFileList;
    private int listItem;
    private State currentState = State.IDLE;
    private long fileLength;
    private long receivedFileLength;
    private BufferedOutputStream out;
    private ByteBuf buf;
    public static BooleanProperty isUpdateServer = new SimpleBooleanProperty ();
    public static BooleanProperty isUpdateClient = new SimpleBooleanProperty ();

    public enum State {
        IDLE,COUNT_LIST,UPDATE,LONG, FILE
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
                MyLogger.logInfo ("Файл [" + listItem + "] в списке update " + ((FileInfo) msg).getFilename ());
                System.out.println ("Файл [" + listItem + "] в списке update " + ((FileInfo) msg).getFilename ());
                if (listItem == countFileList) {
                    listItem = 0;
                    currentState = State.IDLE;
                    ctx.pipeline ().removeFirst ();
                }
                isUpdateServer.setValue (true);
            }
        }

        private void readInt(ChannelHandlerContext ctx,ByteBuf buf) throws IOException {
            if (buf.readableBytes () >= 4) {
                countFileList = buf.readInt ();
                MyLogger.logInfo ("STATE: Count list files " + countFileList);
                System.out.println ("STATE: Count list files " + countFileList);
                currentState = State.UPDATE;
                ctx.pipeline ().addFirst (new ObjectDecoder (1024 * 1024 * 100, ClassResolvers.cacheDisabled (null)));
            }
        }

        @Override
        public void readCommand(ByteBuf buf) {
            listFileServer.clear();
            byte readed = buf.readByte ();
            if (readed != 15) {
                currentState = State.LONG;
            } else {
                currentState = State.COUNT_LIST;
            }
            System.out.println (currentState);
        }

    @Override
    public void readLongFile(ByteBuf buf)  {
        if (buf.readableBytes() >= 8) {
            fileLength = buf.readLong();
            MyLogger.logInfo ("STATE: File length received - " + fileLength);
            System.out.println("STATE: File length received - " + fileLength);
            currentState = State.FILE;
            receivedFileLength = 0L;
        }
        String path = clientFilesPath;
        FileFunction.createDirectory (path);
        try {out = new BufferedOutputStream (new FileOutputStream (String.valueOf (Paths.get(path,nameFile))));
        } catch (FileNotFoundException e) {
            e.printStackTrace ();
            MyLogger.logError ("Файл не найдет или запись в каталоге запрещена!");
            System.out.println ("Файл не найдет или запись в каталоге запрещена!");
        }
    }

    @Override
    public void writeFile(ByteBuf buf) throws IOException {
        while (buf.readableBytes() > 0) {
                out.write (buf.readByte ());
                receivedFileLength++;
                if (fileLength == receivedFileLength) {
                    currentState = State.IDLE;
                    MyLogger.logInfo ("Файл получен.");
                    System.out.println ("Файл получен.");
                    isUpdateClient.setValue (true);
                    out.close ();
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

}
