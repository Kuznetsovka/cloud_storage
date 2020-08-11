package com.geekbrains.common.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public interface ProtoAction {
    void writeFile(ByteBuf buf) throws IOException;
    void readLongFile(ByteBuf buf);
    boolean readNameFile(ChannelHandlerContext ctx, ByteBuf buf) throws IOException;
    void readLengthNameFile(ByteBuf buf);
    void readCommand(ByteBuf buf);
}
