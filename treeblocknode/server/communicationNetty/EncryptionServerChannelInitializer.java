package com.hhf.treeblocknode.server.communicationNetty;
import com.hhf.treeblocknode.server.GroupServerHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.nio.charset.Charset;
public class EncryptionServerChannelInitializer extends ChannelInitializer<SocketChannel> {

    private SslContext sslContext;

    public EncryptionServerChannelInitializer(SslContext sslContext) {
        this.sslContext = sslContext;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        // 添加SSL安装验证
        socketChannel.pipeline().addLast(sslContext.newHandler(socketChannel.alloc()));
        //socketChannel.pipeline().addLast(new MyServerHandler());
        ByteBuf delimiter = Unpooled.copiedBuffer("@@@".getBytes());
        socketChannel.pipeline().addLast(new DelimiterBasedFrameDecoder(Integer.MAX_VALUE, delimiter));
        socketChannel.pipeline().addLast("decoder", new StringDecoder());
        socketChannel.pipeline().addLast("encoder", new StringEncoder());
        socketChannel.pipeline().addLast(new ChunkedWriteHandler());
        socketChannel.pipeline().addLast(new GroupServerHandler());

    }
}
