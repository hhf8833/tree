package com.hhf.treeblocknode.server;

import com.hhf.treeblocknode.server.communicationNetty.NettyClient;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientManagement {
    private static Logger logger = LogManager.getLogger(ClientManagement.class);
    //ip,
    public static Map<String, NettyClient> nodeClients = new ConcurrentHashMap<>();
    // public static Map<String, NettyClient> blockClients = new ConcurrentHashMap<>();

    public static boolean addClient(Bootstrap bootstrap, String host, int port) throws Exception {
        NettyClient nettyClinet = new NettyClient(bootstrap);
        Channel start = nettyClinet.start(host, port);
        if (start == null) {
            return false;
        }
        nettyClinet.setIp(host);
        nodeClients.put(host, nettyClinet);
        logger.info("当前新增节点其客户端ip为：{}",nettyClinet.getIp());
        return true;
    }

    public static boolean removeClient(String host) {
        if (!nodeClients.containsKey(host)) {
            return false;
        }
        nodeClients.remove(host);
        return true;
    }

}
