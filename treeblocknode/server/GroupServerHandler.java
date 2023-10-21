package com.hhf.treeblocknode.server;


import com.alibaba.fastjson2.JSON;
import com.hhf.treeblocknode.pojo.*;
import com.hhf.treeblocknode.server.communicationNetty.NettyClient;
import com.hhf.treeblocknode.server.communicationNetty.NettyClientConfig;
import com.hhf.treeblocknode.server.inter.Ipfs2ServiceImpl;
import com.hhf.treeblocknode.util.SpringContextUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicInteger;

public class GroupServerHandler extends SimpleChannelInboundHandler<String> {
    Logger logger = LogManager.getLogger(GroupServerHandler.class);
    //定义一个channel组，管理所有的channel
    //全局事件执行器，单例 存的所有客户端的通道
    public static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    //用来存当前的与服务器连接的客户端，用来交换数据
    //public static Set<String> groupAddress= new HashSet<>();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    AtomicInteger curReplyNum = MonitorRunnable.leaderQueryResponNum;
    public static AtomicInteger accBlockNum = new AtomicInteger(0);
    //一旦又channel连接就会调用
    public static long startTime = System.currentTimeMillis()/1000;
    public static long endTime = System.currentTimeMillis()/1000;


   // private MongoServiceImpl mongoService = SpringContextUtil.getBean(MongoServiceImpl.class);
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        InetSocketAddress socketAddress = (InetSocketAddress) channel.remoteAddress();
        String address = socketAddress.getAddress().getHostAddress();
        //
        if (ClientManagement.nodeClients.containsKey(address) || InetAddress.getLocalHost().getHostAddress().equals(address)) {
            return;
        }
        logger.info("handlerAdded ip地址是" + socketAddress);
        //将客户加入聊天的信息推送到其他在线的客户
        NettyClientConfig nettyClientConfig = SpringContextUtil.getBean(NettyClientConfig.class);
        Bootstrap bootstrap = nettyClientConfig.bootstrap();
        boolean addClientSuccess = ClientManagement.addClient(bootstrap, address, 6668);
        logger.info("添加客户端成功了吗？-----{}", addClientSuccess);
//        if (addClientSuccess) {
//            channelGroup.add(channel);
//            logger.info("channelGroup是：：：：{}", channelGroup);
//        }
        if (!channelGroup.contains(channel)){
            channelGroup.add(channel);
            logger.info("channelGroup是：：：：{}", channelGroup);
        }

    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        //将客户加入聊天的信息推送到其他在线的客户
        channelGroup.writeAndFlush("[客户端]" + channel.remoteAddress() + "离开了");
        channelGroup.remove(channel);
        InetSocketAddress socketAddress = (InetSocketAddress) channel.remoteAddress();
        String address = socketAddress.getAddress().getHostAddress();
        ClientManagement.removeClient(address);
    }

    //表示channel处于活动状态，提示xx上线
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx.channel().remoteAddress() + "上线了");
        //ctx.channel().writeAndFlush()
        if (!channelGroup.contains(ctx.channel())){
            channelGroup.add(ctx.channel());
            logger.info("channelGroup是：：：：{}", channelGroup);
        }

    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx.channel().remoteAddress() + "离线了");
        channelGroup.remove(ctx.channel());
        InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        String address = socketAddress.getAddress().getHostAddress();
        ClientManagement.removeClient(address);
        Thread.sleep(1000);
        //NettyClient nettyClient = ClientManagement.nodeClients.get(address);
        logger.info("正尝试与节点 {} 进行重连",address);
        NettyClientConfig nettyClientConfig = SpringContextUtil.getBean(NettyClientConfig.class);
        Bootstrap bootstrap = nettyClientConfig.bootstrap();
        for (int i = 0; i < 3; i++) {
            boolean addClient = ClientManagement.addClient(bootstrap, address, 6668);
            if (addClient){
                logger.info("重连成功");
                return;
            }else {
                logger.info("重连 {} 次失败",i+1);
            }
            Thread.sleep(5000);
        }
        logger.info("尝试与节点：{} 进行三次重连，均无法连接，结束",address);
        //ClientManagement.removeClient(address);
        logger.info("channelGroup是：：：：{}", channelGroup);
    }

    private Ipfs2ServiceImpl ipfsService2 = SpringContextUtil.getBean(Ipfs2ServiceImpl.class);
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {
        //获得当前发送消息的用户，该消息不会在服务器中显示
        Channel channel = channelHandlerContext.channel();

        if (s.startsWith("401")){

            String hash = s.substring(3, s.length());
            //区块存入接收缓存内
            ForestChain.Forest_BLOCK_HASH_CACHE.offer(hash);
            int num = accBlockNum.incrementAndGet();
            endTime = System.currentTimeMillis()/1000;
            if ( endTime - startTime >=5){
                //System.out.println("接收消息主题:" + topic);
                //System.out.println("接收消息Qos:" + message.getQos());
                // System.out.println("接收消息内容:" + s);
                logger.info("区块[接收]速度为：{} 块每块,最新森林链区块哈希为{}",num/3,hash);
                System.out.println();
                accBlockNum.set(0);
                endTime = startTime;
                startTime = System.currentTimeMillis()/1000;
            }
            //System.out.println("401 收到区块哈希"+ s);
            return;
        }
        if (s.startsWith("402")){
            String hash = s.substring(3, s.length());
            //区块存入接收缓存内
            MainChain.PREHASH = hash;
            logger.info("接收到领导节点传来的最新主链哈希");
            return;
        }

        //收到发送路由的请求，将服务器所有连接节点的ip发送
        if (s.startsWith("101")) {
            logger.info("服务端接收到101，来自{}", channel.remoteAddress());
            StringBuilder sb = new StringBuilder();
            for (String ip :
                    ClientManagement.nodeClients.keySet()) {
                sb.append(ip);
                sb.append("#");
            }
            channel.writeAndFlush(Unpooled.copiedBuffer("102" + sb.toString() + "@@@", CharsetUtil.UTF_8));
            return;
        }

        //103的回复
        if (s.startsWith("104")) {
            //curReplyNum.getAndIncrement();
            String str = s.substring(3);
            if ("none".equals(str)) {
                logger.info("一个远程节点其ip为{},无leader", channel.remoteAddress());
                return;
            }
            Node node = JSON.parseObject(str, Node.class);
            logger.info("改变领导节点，换为来自{}节点的领导，", channel.remoteAddress());
            LeaderManagement.changeLeaderNode(node);
//            if ((node.getCurRole()==1 && !StringUtils.equals(node.getIp(), LeaderManagement.getLeaderNode().getIp()) && (node.getEmployStart() > LeaderManagement.getLeaderNode().getEmployStart() && node.getTerm() > LeaderManagement.getLeaderNode().getTerm()) ) || LeaderManagement.getLeaderNode() ==null){
//
//            }
            return;
        }

        //105的回复 记录当前有多少个节点同意其作为领导
        if (s.startsWith("106")) {
            System.out.println(s);
            String str = s.substring(3);
            if (StringUtils.equals(str, "yes")) {
                MonitorRunnable.leaderVoteResponNum.incrementAndGet();
                logger.info("106@@@节点：{}同意其作为领导", channel.remoteAddress());
            } else {
                logger.info("106@@@节点：{} 不同意 其作为领导", channel.remoteAddress());
            }

            return;
        }


        if (s.startsWith("201")) {
            logger.info("服务端接收到201，来自轻节点{},消息{}", channel.remoteAddress(), s);
            return;

        }
        //获得其他节点的证书
        if (s.startsWith("300")) {
            logger.info("服务端接收到300");
            String str = s.substring(3);
            byte[] decode = Base64.getDecoder().decode(str.getBytes());
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            InputStream in = new ByteArrayInputStream(decode);
            X509Certificate cert = (X509Certificate)certFactory.generateCertificate(in);
            logger.info("服务端接收到来自节点{}的证书", channel.remoteAddress());
            logger.info("证书为{}",cert);
            logger.info("证书合法",cert);

            //logger.info("证书公钥为：{}", certificate.getPublicKey());
           // logger.info("证书公钥为：{}",new String(Base64.getEncoder().encode(certificate.getSignature())));
            return;

        }
//        //根据不同的情况回复不同消息
//        channelGroup.forEach(ch -> {
//            if (ch != channel) {
//                ByteBuf byteBuf = Unpooled.copiedBuffer(s, CharsetUtil.UTF_8);
//                ch.writeAndFlush("[客户]" + channel.remoteAddress() + "发送消息：" + s + "\n");
//            } else {
//                ch.writeAndFlush("[自己]发送了消息" + s + "\n");
//            }
//        });
        //channelGroup.writeAndFlush("[客户]"+channel.remoteAddress()+"发送消息："+s+"\n");


    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
