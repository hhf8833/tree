package com.hhf.treeblocknode.server;


import com.alibaba.fastjson2.JSON;
import com.hhf.treeblocknode.pojo.LeaderManagement;
import com.hhf.treeblocknode.pojo.Node;
import com.hhf.treeblocknode.server.communicationNetty.NettyClientConfig;
import com.hhf.treeblocknode.util.SpringContextUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;


public class GroupClientHandler extends SimpleChannelInboundHandler<String> {
    Logger logger = LogManager.getLogger(GroupClientHandler.class);

//    @Autowired
//    private DataAccService dataAccService;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {
        Channel channel = channelHandlerContext.channel();
        if (s.startsWith("102")) {
            String sub = s.substring(3);
            logger.info("102 得到的ips是{}", sub);
            String[] ips = sub.split("#");
            for (String ip :
                    ips) {
                if (ClientManagement.nodeClients.containsKey(ip) || InetAddress.getLocalHost().getHostAddress().equals(ip)) {
                    logger.info("当前{}已经加入", ip);
                    continue;
                }
                NettyClientConfig nettyClientConfig = SpringContextUtil.getBean(NettyClientConfig.class);
                Bootstrap bootstrap = nettyClientConfig.bootstrap();
                ClientManagement.addClient(bootstrap, ip, 6668);
            }
            return;
        }
        //获取本节点的leader
        if (s.startsWith("103")) {
            logger.info("接收到节点{}，发起103请求",channel.remoteAddress());
            Node leaderNode = LeaderManagement.getLeaderNode();
            logger.info("103通知，当前领导节点为:{}",leaderNode);
            if (leaderNode == null || leaderNode.getEmployStart() + leaderNode.getEmploymentPeriod() < System.currentTimeMillis()/1000) {
                channel.writeAndFlush(Unpooled.copiedBuffer("104none@@@", CharsetUtil.UTF_8));
            } else {
                //json封装
                String string = JSON.toJSONString(leaderNode);
                channel.writeAndFlush(Unpooled.copiedBuffer("104" + string + "@@@", CharsetUtil.UTF_8));
            }
            return;
        }
        //接收其他节点的领导请求
        if (s.startsWith("105")) {
            Node leaderNode = LeaderManagement.getLeaderNode();
            //没有领导或者领导过期，且当前节点没有参与其他选举
            if ((leaderNode == null || leaderNode.getEmployStart() + leaderNode.getEmploymentPeriod() < System.currentTimeMillis() / 1000) && MonitorRunnable.voteStates.get() == 0) {
                logger.info("同意来着ip为：{}节点作为领导", channel.remoteAddress());
                channel.writeAndFlush(Unpooled.copiedBuffer("106yes@@@", CharsetUtil.UTF_8));
            } else {
                if (MonitorRunnable.voteStates.get() != 0) {
                    logger.info("当前该节点在参与选举，状态为：{}，不能投票", MonitorRunnable.voteStates.get());
                }
                channel.writeAndFlush(Unpooled.copiedBuffer("106no@@@", CharsetUtil.UTF_8));
            }
            return;
        }

        //更新 新的领导节点
        if (s.startsWith("107")) {
            String sub = s.substring(3);

            Node node = JSON.parseObject(sub, Node.class);
            logger.info("107 得到的新节点就任领导是{}", node.getIp());
            //System.out.println(node.toString());
            boolean iSuccess = LeaderManagement.changeLeaderNode(node);
            logger.info("107 改变领导节点是否成功？{}", iSuccess);
            return;
        }

        //接收hash
        if (s.startsWith("401@")){
            String[] split = s.split("@");
            String hash = split[1];
            System.out.println("401 收到区块哈希"+ hash);
//            Runnable runnable = ()-> dataAccService.blockAcc(hash);
//            TreeblocknodeApplication.dataAccExecutor.execute(runnable);
            return;
        }
    }
}
