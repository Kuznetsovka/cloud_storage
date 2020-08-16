package com.geekbrains.common1.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;

public interface ProtoAction {
    void writeFile(ByteBuf buf) throws IOException;
    void readLongFile(ByteBuf buf);
    boolean readNameFile(ChannelHandlerContext ctx, ByteBuf buf) throws IOException;
    void readLengthNameFile(ByteBuf buf);
    void readCommand(ByteBuf buf);
}
