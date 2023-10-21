package com.hhf.treeblocknode.server.communicationNetty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

@Component
public class NettyServer {
    Logger logger = LogManager.getLogger(NettyServer.class);
    @Autowired
    @Qualifier("serverBootstrap")
    private ServerBootstrap serverBootstrap;

    private Channel serverChannel;

    public void start() throws Exception {
        logger.info("netty启动");
        ChannelFuture channelFuture = serverBootstrap.bind(6668).sync();
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (channelFuture.isSuccess()) {
                    logger.info("6668监听成功");
                } else {
                    logger.error("6668监听失败");
                }
            }
        });

//        serverChannel = serverBootstrap.bind(6668).sync().channel().closeFuture().sync().channel();
        serverChannel = channelFuture.channel().closeFuture().sync().channel();
        //System.out.println("222222");
    }

    @PreDestroy
    public void stop() throws Exception {
        serverChannel.close();
        serverChannel.parent().close();
    }


}
