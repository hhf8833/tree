package com.hhf.treeblocknode.server.communicationNetty;

import com.hhf.treeblocknode.server.ClientManagement;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
public class NettyClient {
    Logger logger = LogManager.getLogger(NettyClient.class);
//    @Autowired
//    @Qualifier("bootstrap")
//    private Bootstrap bootstrap;

    private Channel clientChannel;
    private Bootstrap bootstrap;
    private String ip;

    public NettyClient(Bootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    public Channel start(String host, int port) {
        if (ClientManagement.nodeClients.containsKey(host)) {
            return null;
        }
        try {
            ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
            connectMonitor(host, channelFuture);
            clientChannel = channelFuture.channel();

        } catch (Exception e) {
            logger.debug("client无法连接ip为{}的远程服务端", host);
            // e.printStackTrace();
        }
        return clientChannel;
    }

    private void connectMonitor(String host, ChannelFuture channelFuture) {
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    logger.info("本地client成功连接ip为{}的远程服务端", host);
                } else {
                    logger.info("本地client无法连接ip为{}的远程服务端，尝试重连", host);
                    //重连交给后端线程执行
                    future.channel().eventLoop().schedule(() -> {
                        System.err.println("重连服务端...");
                        try {
                            connectMonitor(host, channelFuture);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }, 3000, TimeUnit.MILLISECONDS);
                    //stop();
                }
            }
        });
    }

    public void writeFlush(String s) {
        clientChannel.writeAndFlush(s);
    }

    @PreDestroy
    public void stop() throws Exception {
        clientChannel.close();
        clientChannel.parent().close();
    }


}
