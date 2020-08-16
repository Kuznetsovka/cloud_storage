package com.geekbrains.common_files.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ProtoFileSender implements Config {
    public static void sendFile(Path path,SENDER sender, boolean upload, Channel channel, ChannelFutureListener finishListener) throws IOException {
        ByteBuf buf;
        byte signal = upload ? SIGNAL_UPLOAD:SIGNAL_DOWNLOAD;
            buf = ByteBufAllocator.DEFAULT.directBuffer (1);
            buf.writeByte (signal);
            channel.writeAndFlush (buf);

        if (sender == SENDER.CLIENT) {
            byte[] filenameBytes = path.getFileName ().toString ().getBytes (StandardCharsets.UTF_8);
            buf = ByteBufAllocator.DEFAULT.directBuffer (4);
            buf.writeInt (filenameBytes.length);
            channel.writeAndFlush (buf);

            buf = ByteBufAllocator.DEFAULT.directBuffer (filenameBytes.length);
            buf.writeBytes (filenameBytes);
            channel.writeAndFlush (buf);
        }

        if (sender == SENDER.SERVER || upload) {
            FileRegion region = new DefaultFileRegion(path.toFile(), 0, Files.size(path));
            buf = ByteBufAllocator.DEFAULT.directBuffer (8);
            buf.writeLong (Files.size (path));
            channel.writeAndFlush (buf);

            ChannelFuture transferOperationFuture = channel.writeAndFlush (region);
            if (finishListener != null) {
                transferOperationFuture.addListener (finishListener);
            }
        }
    }
}