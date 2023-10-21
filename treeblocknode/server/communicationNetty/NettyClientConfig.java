package com.hhf.treeblocknode.server.communicationNetty;

import com.hhf.treeblocknode.server.GroupClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;


@Configuration
public class NettyClientConfig {


//    @Autowired
//    NettyWebsocketHandler nettyWebsocketHandler;

//    @Autowired
//    ChatRoomHandler chatRoomHandler;

    @Bean(name = "ClinetBossGroup", destroyMethod = "shutdownGracefully")
    public NioEventLoopGroup ClinetGroup() {
        return new NioEventLoopGroup();
    }


    @Bean(name = "bootstrap")
    @Scope("prototype")
    public Bootstrap bootstrap() {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(ClinetGroup())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        ByteBuf delimiter = Unpooled.copiedBuffer("@@@".getBytes());
                        pipeline.addLast(new DelimiterBasedFrameDecoder(Integer.MAX_VALUE, delimiter));
                        pipeline.addLast("decoder", new StringDecoder());
                        pipeline.addLast("encoder", new StringEncoder());
                        pipeline.addLast(new GroupClientHandler());
                    }
                });
        return bootstrap;
    }


}
