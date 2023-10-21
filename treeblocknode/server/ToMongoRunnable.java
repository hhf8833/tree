package com.hhf.treeblocknode.server;

import com.alibaba.fastjson2.JSON;
import com.hhf.treeblocknode.pojo.ForestChain;
import com.hhf.treeblocknode.pojo.ForestChainBlock;
import com.hhf.treeblocknode.pojo.IotMessage;
import com.hhf.treeblocknode.server.inter.IpfsServiceImpl;
import com.hhf.treeblocknode.server.inter.MongoServiceImpl;
import com.hhf.treeblocknode.util.SpringContextUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

public class ToMongoRunnable implements Runnable {
    Logger logger = LogManager.getLogger(ToMongoRunnable.class);
    private MongoServiceImpl mongoService = SpringContextUtil.getBean(MongoServiceImpl.class);
    private IpfsServiceImpl ipfsService = SpringContextUtil.getBean(IpfsServiceImpl.class);

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

            while (!ForestChain.Forest_BLOCK_HASH_CACHE.isEmpty()) {
                LinkedBlockingQueue<String> forest_block_hash_cache = ForestChain.Forest_BLOCK_HASH_CACHE;
                byte[] forestChainBlockByte= new byte[0];
                try {
                    forestChainBlockByte  = ipfsService.downFromIpfs(forest_block_hash_cache.take());
                } catch (InterruptedException e) {
                    logger.error("取出区块错误");
                    e.printStackTrace();
                }
                //获得区块哈希并解析
            ForestChainBlock forestChainBlock = JSON.parseObject(forestChainBlockByte, ForestChainBlock.class);

                //存入mongodb
                Set<String> data = forestChainBlock.getData();
                int term = forestChainBlock.getTerm();

                String nodeIp = forestChainBlock.getNode().getIp();
                for (String value:
                     data) {
                    IotMessage iovMessage = new IotMessage();
                    iovMessage.setNodeIP(nodeIp);
                    iovMessage.setTerm(term);
                    iovMessage.setData(value);
                    //System.out.println(iovMessage);

                    mongoService.insert(iovMessage);
                }
            }
        }
    }
}
