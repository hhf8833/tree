package com.hhf.treeblocknode.server;

import com.alibaba.fastjson2.JSON;
import com.hhf.treeblocknode.TreeblocknodeApplication;
import com.hhf.treeblocknode.pojo.*;
import com.hhf.treeblocknode.util.ECCUtils;
import io.netty.buffer.Unpooled;
import io.netty.channel.group.ChannelGroup;
import io.netty.util.CharsetUtil;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class MonitorRunnable implements Runnable {
    Logger logger = LogManager.getLogger(MonitorRunnable.class);
    public static AtomicInteger leaderQueryResponNum = new AtomicInteger(0);
    public static AtomicInteger leaderVoteResponNum = new AtomicInteger(0);
    //用于判断当前节点选举状态 0为此时未选举 1为当前正在选举
    public static AtomicInteger voteStates = new AtomicInteger(0);
    @Autowired
    private IOTSubShare iotSubShare;
    ChannelGroup channelGroup;

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */

    @Override
    public void run() {
        while (true) {

            channelGroup = GroupServerHandler.channelGroup;
            //leaderQueryResponNum.set(0);

            //第一次没领导则先查询领导
            if (LeaderManagement.getLeaderNode() == null) {
                logger.info("当前无领导");
                //向其他节点发送103 更新领导
                channelGroup.forEach(channel -> {
                    channel.writeAndFlush(Unpooled.copiedBuffer("103@@@", CharsetUtil.UTF_8));
                    logger.info("向远程节点{}，传播103@@@请求查验是否存在领导", channel.remoteAddress());
                });
                //延时两秒
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                logger.info("经过103之后的leader是{}", LeaderManagement.getLeaderNode());
            }

            //当前没有领导,而且还未参加投票选举自己或其他人时
            if (LeaderManagement.getLeaderNode() == null && voteStates.get() == 0) {
                //领导竞选
                boolean isSuccess = false;
                try {
                    isSuccess = vote();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (isSuccess) {

                }
                logger.info("领导选举是否成功？{}", isSuccess);
            }

            //当前存在领导的时候  && leaderNode.getCurRole() == 1
            if (LeaderManagement.getLeaderNode() != null) {
                Node leaderNode = LeaderManagement.getLeaderNode();
                long employStartTime = leaderNode.getEmployStart();
                int employmentPeriod = leaderNode.getEmploymentPeriod();
                logger.info("当前存在领导节点为：{}", leaderNode);
                logger.info("任期开始时间{}，任期时长{}，当前时间{},任期剩余时间{}", employStartTime, employmentPeriod,System.currentTimeMillis()/1000,employStartTime + employmentPeriod - System.currentTimeMillis()/1000);

                //领导到期
                if (employStartTime + employmentPeriod - System.currentTimeMillis()/1000 <= 5) {
                    int incrementAndGet = TreeblocknodeApplication.atomicVoteAndBlockgenerate.incrementAndGet();
                    logger.info("----阻塞区块生成，竞选领导----{}",incrementAndGet);
                    logger.info("----领导到期----");
                    LeaderManagement.deleteLeaderNode();
                    //leaderNode =null;
                    //TODO 竞争当leader  向其他节点发送请求，并等待一定时间，若2/3节点收到该请求并同意，则竞选成功，再操作下一步
                   // int curTerm = leaderNode.getTerm();
                    //生成认证消息，证明自己身份
//                    String publicKey = leaderNode.getSignatureMap().get(leaderNode.getId());
//                    String signature = ECCUtils.geneateSignature(leaderNode.getId());
//                    String requestMessage = "105@@@" + publicKey + "@" + signature + "@" + leaderNode.getId() + ++curTerm;
                    String requestMessage = "105@@@";
                    channelGroup.forEach(channel -> {
                        channel.writeAndFlush(Unpooled.copiedBuffer(requestMessage, CharsetUtil.UTF_8));
                        System.out.print(channel.remoteAddress() + "\t");
                    });
                    boolean isSuccess = false;
                    try {
                        isSuccess = vote();
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    logger.info("领导选举是否成功？？？{}", isSuccess);
                    int decrementAndGet = TreeblocknodeApplication.atomicVoteAndBlockgenerate.decrementAndGet();
                    logger.info("竞选领导结束，继续生成区块----{}", decrementAndGet);
                    //todo 生成次链区块
                    SecondChainBlock secondChainBlock = generateSecondBlock(leaderNode);
                    logger.info("生成次链区块");
                    SecondChain.Second_BLOCK_CACHE.offer(secondChainBlock);

                }
            }
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    private boolean vote() throws UnknownHostException, InterruptedException {
        channelGroup = GroupServerHandler.channelGroup;
        int size = channelGroup.size();
        //logger.info("当前系统中节点数量为：-------{}", size);
        if (voteStates.get() == 0) {
            //1000-5000随机数，随机延时防止出现同时竞选的情况
            Random random = new Random();
            int min = 0;
            int max = 5000;
            int randomNum = random.nextInt((max - min) + 1) + min;
            logger.info("随机休眠{}毫秒",randomNum);
            Thread.sleep(randomNum);

            //正在选举中
            voteStates.incrementAndGet();
            //选举leader,要用一个新的变量记录传播的请求回应数量
            leaderVoteResponNum.set(0);

            channelGroup.forEach(channel -> {
                channel.writeAndFlush(Unpooled.copiedBuffer("105@@@", CharsetUtil.UTF_8));
                logger.info("向远程节点{}发起竞选领导请求", channel.remoteAddress());
            });
            try {
                logger.info("正在接收选票");
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (leaderVoteResponNum.get() >= size / 2 + 1) {
                //证明当前绝大多数节点同意其作为领导 todo 这里有问题因为之前是不把本机算在iplist里面的，需要加上
                Node leaderNode = LeaderManagement.getLeaderNode();
                if (leaderNode == null) {
                    logger.info("leader为空，收到足够选票，创建leader");
                    leaderNode = LeaderManagement.createLeaderNode();
                } else {
                    //当前有领导,更改领导节点情况
                    leaderNode.setIp(InetAddress.getLocalHost().getHostAddress());
                    leaderNode.setId(NodeInitial.getInstance().getId());
                    leaderNode.setTerm(leaderNode.getTerm() + 1);
                    Map<String, Integer> nodeDistribution = LeaderManagement.createNodeDistribution();
                    leaderNode.setNodeDistribution(nodeDistribution);
                    LeaderManagement.TERM = leaderNode.getTerm() + 1;
                    logger.info("任期更新，当前任期为{}",LeaderManagement.TERM);
//                    leaderNode.setEmployStart(leaderNode.getEmployStart() + leaderNode.getEmploymentPeriod());
                }
                leaderNode.setEmployStart(System.currentTimeMillis()/1000);
                String newLeader = JSON.toJSONString(leaderNode);
                //将结果发送至其他节点，其他节点也更改
                logger.info("收到足够选票，创建leader");
                logger.info("更新的领导节点为{}",leaderNode);
                // 生成主链区块
                MainChainBlock mainChainBlock = generateMainBlock(leaderNode);
                logger.info("生成主链区块");
                MainChain.Main_BLOCK_CACHE.offer(mainChainBlock);

                channelGroup.forEach(channel -> {
                    channel.writeAndFlush(Unpooled.copiedBuffer("107" + newLeader + "@@@", CharsetUtil.UTF_8));
                    logger.info("向远程节点{}通知，当前领导为{}", channel.remoteAddress(),LeaderManagement.getLeaderNode().getIp());
                });
                //creatConsensus(createdLeader);
                logger.info("当前领导为：{},任期为{}", leaderNode.getIp(), leaderNode.getTerm());
                //修改竞选状态为非竞选状态
                voteStates.decrementAndGet();
                return true;
            }
            if (leaderQueryResponNum.get() < size / 2 + 1) {
                voteStates.decrementAndGet();
                leaderQueryResponNum.set(0);
                logger.info("节点回复数量少于阈值,本次竞选失败");
            }
        }
        return false;
    }

    public void creatConsensus(Node createdLeader) throws UnknownHostException {
        //本节点更改物联网数据接收 todo 启动时开启mqtt

        String hostAddress = InetAddress.getLocalHost().getHostAddress();
        //iotSubShare.putSub(createdLeader.getSectionMap().get(hostAddress));
    }

    public MainChainBlock generateMainBlock(Node leaderNode){
        MainChainBlock mainChainBlock = new MainChainBlock();
        if (MainChain.PREHASH != null){
            mainChainBlock.setPreMainHash(MainChain.PREHASH);
        }
        mainChainBlock.setLeaderNodeId(leaderNode.getIp());
        mainChainBlock.setTerm(leaderNode.getTerm());
        StringBuilder sb = new StringBuilder();
        for (String ip :
                ClientManagement.nodeClients.keySet()) {
            sb.append(ip);
            sb.append("#");
        }
        mainChainBlock.setNodeId(sb.toString());
        mainChainBlock.setTimestap(System.currentTimeMillis()/1000);
        return mainChainBlock;
    }

    public SecondChainBlock generateSecondBlock(Node leaderNode){
        SecondChainBlock secondChainBlock = new SecondChainBlock();
        if (SecondChain.PREHASH != null){
            secondChainBlock.setPreSecondHash(SecondChain.PREHASH);
        }

        secondChainBlock.setTerm(leaderNode.getTerm());
        secondChainBlock.setTimestap(System.currentTimeMillis()/1000);
        if (ForestChain.PREHASH !=null){
            secondChainBlock.setSpliceHash(ForestChain.PREHASH);
        }
        //secondChainBlock.setNodeId(sb.toString());

        return secondChainBlock;
    }
}
