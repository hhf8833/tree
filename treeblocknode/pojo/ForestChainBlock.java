package com.hhf.treeblocknode.pojo;

import com.alibaba.fastjson2.JSON;
import com.hhf.treeblocknode.server.GroupServerHandler;
import com.hhf.treeblocknode.server.NodeInitial;
import com.hhf.treeblocknode.util.ECCUtils;
import io.netty.channel.group.ChannelGroup;
import lombok.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ForestChainBlock {
    String blockHash;
    String preBlockHash;
    Node node;
    int term;
    int blockOffset;
    long timestamp;
    String merkleRoot;
    Set<String> data;
    long firstDataTime;
    long lastDataTime;
    String signature;


    static Map<String, BlockBit> BITMAP = new ConcurrentHashMap();
    private static Object LOCK = new Object();
    //static ConcurrentLinkedQueue
    public void blockComplement(){
        ScheduledExecutorService pool = Executors.newSingleThreadScheduledExecutor();

        Runnable blockComplement =()->{
            //获取当前分支链的位图列表
            Map<String, BlockBit> bitmap = ForestChainBlock.BITMAP;
            //唤醒区块下载线程
            Object lock = ForestChainBlock.LOCK;
            //遍历每一条链，查询确实区块
            for (Map.Entry<String, BlockBit> entry: bitmap.entrySet()) {
                //获得标识位和bitset
                BitSet bitSet = entry.getValue().getBitSet();
                int identiPoint = entry.getValue().getIdentificationPoint();
                String id = entry.getKey();
                for (int i = identiPoint; i < bitSet.length(); i++) {//寻找缺失区块
                    if (!bitSet.get(i)){//当前位置没有区块
                        synchronized (lock){
                            ChannelGroup channelGroup = GroupServerHandler.channelGroup;//获取对应节点的传输通道
                            String message = "108" + bitSet.get(i) +"@@@";//补全区块请求
                            //todo
//                            String encodeMes = SM4Utils.encodeMessage(message,SM4Utils.SECRET);//消息加密
//                            boolean isSuccess = sendRequest(channelGroup,encodeMes);//发送补全消息请求
//                            if (!isSuccess){
//                                sendRequest(channelGroup,encodeMes);//不成功则再次发送
//                            }else {
//                               bitSet.set(i);
//                            }
//                            List<ForestChainBlock> forestChainBlock = chains.get(id);//得到对应的链
                        }
                    }
                }
                entry.getValue().setIdentificationPoint(bitSet.length()+1);//更新标识点
            }
        };
        pool.scheduleWithFixedDelay(blockComplement,0,10,TimeUnit.SECONDS);
    }

}
