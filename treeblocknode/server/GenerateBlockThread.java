package com.hhf.treeblocknode.server;

import com.alibaba.fastjson2.JSON;
import com.hhf.treeblocknode.TreeblocknodeApplication;
import com.hhf.treeblocknode.pojo.*;
import com.hhf.treeblocknode.server.communicationNetty.NettyClient;
import com.hhf.treeblocknode.server.inter.Ipfs2ServiceImpl;
import com.hhf.treeblocknode.server.inter.IpfsServiceImpl;
import com.hhf.treeblocknode.util.SpringContextUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.group.ChannelGroup;
import io.netty.util.CharsetUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;


public class GenerateBlockThread implements Runnable {
    Logger logger = LogManager.getLogger(GenerateBlockThread.class);
//    @Autowired
//    private IpfsService ipfsService;

    private IpfsServiceImpl ipfsService = SpringContextUtil.getBean(IpfsServiceImpl.class);
    private Ipfs2ServiceImpl ipfsService2 = SpringContextUtil.getBean(Ipfs2ServiceImpl.class);
    public static long startTime = System.currentTimeMillis() / 1000;
    public static long endTime = System.currentTimeMillis() / 1000;
    public static long startTime2 = System.currentTimeMillis() / 1000;
    public static long endTime2 = System.currentTimeMillis() / 1000;
    public static int num = 0;
    ChannelGroup channelGroup = GroupServerHandler.channelGroup;

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
        LinkedBlockingQueue<String> datacache = ForestChain.IOV_DATA_CACHE;
        int num =0;
        while (true) {
            //主链生成
            if (!MainChain.Main_BLOCK_CACHE.isEmpty()){
                try {
                    MainChainBlock mainblock =MainChain.Main_BLOCK_CACHE.take();
                    String mainblockstr = JSON.toJSONString(mainblock);
                    String mainblockHash = ipfsService.uploadToIpfs(mainblockstr);
                    MainChain.PREHASH = mainblockHash;
                    //广播主链哈希
                    broacastHash("402",mainblockHash);
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            }
            //次链
            if (!SecondChain.Second_BLOCK_CACHE.isEmpty()){
                try {
                    SecondChainBlock takedSecond = SecondChain.Second_BLOCK_CACHE.take();
                    String secondblockstr = JSON.toJSONString(takedSecond);
                    String secondblockHash = ipfsService.uploadToIpfs(secondblockstr);
                    SecondChain.PREHASH = secondblockHash;
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            }
            if (endTime2 - startTime2 >= 5) {
                startTime2 = endTime2;
                //startTime2 = System.currentTimeMillis() / 1000;
                logger.info("最新主链区块哈希为{},最新的次链区块哈希为{}",MainChain.PREHASH,SecondChain.PREHASH);
            }
            if (datacache.size() >= 4 ) {
                if (TreeblocknodeApplication.atomicVoteAndBlockgenerate.get() ==1 || LeaderManagement.getLeaderNode() ==null){
                    endTime = System.currentTimeMillis() / 1000;
                    if (endTime - startTime >= 1) {
                        endTime = startTime;
                        startTime = System.currentTimeMillis() / 1000;
                        logger.info("当前正迭代任期，区块生成停止");
                    }
                    continue;
                }

                //生成区块
                ForestChainBlock block = null;
                String blockStr =null;
                String blockHash =null;
                try {

                    block = blockGenerate("111111", datacache);
                    //写入ipfs
                    blockStr = JSON.toJSONString(block);
                    //System.out.println(blockStr);
                    if ((num & 1) ==0 ){
                        blockHash = ipfsService.uploadToIpfs(blockStr);
                    }else {
                        blockHash = ipfsService2.uploadToIpfs(blockStr);
                    }
                    block.setBlockHash(blockHash);
                    ForestChain.PREHASH = blockHash;
                    num++;

                } catch (Exception e) {
                    e.printStackTrace();
                }

                //广播森林链hash
                broacastHash("401",blockHash);

                num++;
                endTime = System.currentTimeMillis() / 1000;
                endTime2 = System.currentTimeMillis() / 1000;
                if (endTime - startTime >= 5) {
                    // System.out.println(blockStr);
                    logger.info("最新[生成]的区块哈希为：{}",blockHash);
                    logger.info("-----------区块[生成]速度为：{} 块每秒",num/5);
                    //System.out.println("区块哈希为：" + blockHash);
                   // System.out.println("-----------区块速度为：" + num + "条每秒");
                    num = 0;
                    endTime = startTime;
                    startTime = System.currentTimeMillis() / 1000;
                    //System.out.println("当前缓存中有" + datacache.size()+"条数据");
                }
            }
            //Thread.sleep(2000);
        }
    }

    public ForestChainBlock blockGenerate(String priKey, LinkedBlockingQueue<String> datacache) throws Exception {
        ForestChainBlock curblock = new ForestChainBlock();//初始化区块
        if (ForestChain.PREHASH != null){
            curblock.setPreBlockHash(ForestChain.PREHASH);
        }
        curblock.setBlockOffset(++ForestChain.OFFSET);
        Node node = NodeInitial.getInstance();
        Node leaderNode = LeaderManagement.getLeaderNode();
        node.setTerm(leaderNode.getTerm());
        curblock.setNode(node);
        curblock.setTerm(leaderNode.getTerm());
        //从缓存中获取四条数据
        Set<String> datas = new HashSet<>();
        try {
            for (int i = 0; i < 4; i++) {
                datas.add(datacache.take());
            }
            curblock.setData(datas);
        } catch (InterruptedException e) {
            System.out.println("数据取出错误");
            e.printStackTrace();
        }
        //生成默克尔根
        String merkleRoot = merkleRootGenerate(datas);
        curblock.setMerkleRoot(merkleRoot);
        //生成时间
        long currentTimeMillis = System.currentTimeMillis() / 1000;
        curblock.setTimestamp(currentTimeMillis);
        //todo 默克尔根签名
        //String rootSignature = ECCUtils.geneateSignature(merkleRoot,priKey);
        //curblock.setSignature(rootSignature);
        //todo 生成哈希
//        String blockJson = JSON.toJSONString(curblock);
//        String curHash = IPFS.hashGenerate(blockJson);
//        curblock.setBlockHash(curHash);
        return curblock;
    }

    public String merkleRootGenerate(Set<String> data) throws NoSuchAlgorithmException {
        List<byte[]> leaves = new ArrayList<>();
        //将数据写入byte数组内
        for (String mes : data) {
            byte[] jsonBytes = JSON.toJSONBytes(mes);
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(jsonBytes);//求出数据的哈希
            leaves.add(digest);
        }
        //构建默克尔树
        while (leaves.size() > 1) {
            List<byte[]> parents = new ArrayList<>();
            for (int i = 0; i < leaves.size(); i += 2) {
                byte[] left = leaves.get(i);
                byte[] right = (i + 1 < leaves.size()) ? leaves.get(i + 1) : left;
                //计算出两个byte数组的哈希
                byte[] tmp = new byte[left.length + right.length];
                System.arraycopy(left, 0, tmp, 0, left.length);
                System.arraycopy(right, 0, tmp, left.length, right.length);
                byte[] digest = MessageDigest.getInstance("SHA-256").digest(tmp);//求出数据的哈希
                parents.add(digest);
            }
            leaves = parents;
        }
        //将默克尔根转为十六进制字符串
        StringBuilder sb = new StringBuilder();
        for (byte b : leaves.get(0)) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }

    public void broacastHash(String header,String blockHash){
        String message = header + blockHash + "@@@";
        Map<String, NettyClient> nodeClients = ClientManagement.nodeClients;
        for (Map.Entry<String, NettyClient> nettyClients:
                nodeClients.entrySet()) {
            Channel clientChannel = nettyClients.getValue().getClientChannel();
            if (clientChannel !=null){
                ChannelFuture channelFuture = clientChannel.writeAndFlush(Unpooled.copiedBuffer(message, CharsetUtil.UTF_8));
                channelFuture.addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (channelFuture.isSuccess()) {
                            //logger.info("向远程节点{}，传播401,消息传递成功",clientChannel.remoteAddress());
                        } else {
                            logger.error("向远程节点{}，传播401,消息传递失败",clientChannel.remoteAddress());
                        }
                    }
                });
            }
        }
    }
}
