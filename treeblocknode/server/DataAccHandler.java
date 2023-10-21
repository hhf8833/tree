package com.hhf.treeblocknode.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.atomic.AtomicInteger;

public class DataAccHandler extends SimpleChannelInboundHandler<String> {

    public static AtomicInteger accBlockNum = new AtomicInteger(0);
    /**
     * Is called for each message of type {@link I}.
     *
     * @param ctx the {@link ChannelHandlerContext} which this {@link SimpleChannelInboundHandler}
     *            belongs to
     * @param msg the message to handle
     * @throws Exception is thrown if an error occurred
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        if (msg.startsWith("401@")){
            String[] split = msg.split("@");
            String hash = split[1];
            int num = accBlockNum.incrementAndGet();
            System.out.println("401 收到区块哈希"+ hash + "数量为" + num);
            return;
        }
    }
}
