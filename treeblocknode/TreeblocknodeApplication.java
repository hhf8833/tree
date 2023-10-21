package com.hhf.treeblocknode;

import com.alibaba.fastjson2.JSON;
import com.hhf.treeblocknode.pojo.Node;
import com.hhf.treeblocknode.server.*;
import com.hhf.treeblocknode.server.communicationNetty.NettyClient;
import com.hhf.treeblocknode.server.communicationNetty.NettyClientConfig;
import com.hhf.treeblocknode.server.communicationNetty.NettyServer;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.util.CharsetUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootApplication
public class TreeblocknodeApplication {

    private static Logger logger = LogManager.getLogger(TreeblocknodeApplication.class);
    public static String MQTT_IP = null;
    // data服务调度池(receive)
    public static ThreadPoolExecutor dataAccExecutor = new ThreadPoolExecutor(
            12,
            36,
            0L,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>());
    public static AtomicInteger atomicVoteAndBlockgenerate = new AtomicInteger();


    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext context = SpringApplication.run(TreeblocknodeApplication.class, args);
        CertificationManagement certificationManagement = new CertificationManagement();
        String cerString2 = JSON.toJSONString(certificationManagement.getCertificate());
        System.out.println(cerString2);
        NettyServer nettyServer = context.getBean(NettyServer.class);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    nettyServer.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        //初始化节点
        Node node = NodeInitial.getInstance();
        logger.info("当前角色是：" + node.getCurRole() + "当前ip为：" + node.getIp());
        NettyClientConfig nettyClientConfig = context.getBean(NettyClientConfig.class);
        //需要将自己与对应节点连接,然后发送消息让对方发出相连的ip地址，自己再次启动多个client与之相连
        Bootstrap bootstrap = nettyClientConfig.bootstrap();
        boolean addClientSucess;
        String host = null;
        //和其他服务端建立联系
        if (args.length != 0) {
            MQTT_IP = args[0];
            if (args.length >= 2) {
                host = args[1];
                //本节点开启客户端连接对方
                addClientSucess = ClientManagement.addClient(bootstrap, host, 6668);
                if (addClientSucess) {
                    NettyClient client = ClientManagement.nodeClients.get(host);
                    //获得当前节点加上系统时的证书签名
//                Certificate certificate = client.getSignature();
                    String cerStr= new String(Base64.getEncoder().encode(certificationManagement.getCertificate().getEncoded()));
                    //System.out.println(cerString);
                    Channel clientChannel = client.getClientChannel();
                    clientChannel.writeAndFlush("300"+cerStr+"@@@");
                    clientChannel.writeAndFlush(Unpooled.copiedBuffer("101@@@", CharsetUtil.UTF_8));
                } else {
                    logger.error("client 连接失败{}", host);
                }
            }

        }
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while (true){
//                    Set<Map.Entry<String, NettyClient>> entries = ClientManagement.nodeClients.entrySet();
//                    for (Map.Entry<String, NettyClient> entry :
//                            entries) {
//                        Channel clientChannel = entry.getValue().getClientChannel();
//                        logger.info("客户端ip是{}，向其询问更新", clientChannel.remoteAddress());
//                        clientChannel.writeAndFlush(Unpooled.copiedBuffer("101@@@", CharsetUtil.UTF_8));
//                    }
//                    try {
//                        Thread.sleep(5000);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }).start();
        //TODO:服务端向所有的其他服务端的客户端询问 当前任期和任期的开始结束时间，发送的话就发对应节点的role；以此判断自己是否能争抢领导，或者记录当前领导，开启线程监控
        //List<ChannelFuture> backMessage
//        while (channelGroup.size() < 2){
//            logger.info("当前节点数小于3");
//            Thread.sleep(1000);
//        }

//        int size = channelGroup.size();
//        channelGroup.forEach(channel -> {
//            channel.writeAndFlush(Unpooled.copiedBuffer("103@@@", CharsetUtil.UTF_8));
//            System.out.print(channel.remoteAddress() +"\t");
//        });
        Thread.sleep(10000);
        new Thread(new MonitorRunnable()).start();

        new Thread(new MqttServer()).start();


        new Thread(new GenerateBlockThread()).start();

        new Thread(new ToMongoRunnable()).start();

        new Thread(new TokenRunnable()).start();
        //new Thread(new IPFSServerThread()).start();

        ChannelGroup channelGroup = GroupServerHandler.channelGroup;

//        while (true) {
//            Thread.sleep(5000);
//            System.out.println(ClientManagement.nodeClients.size()+" ");
//            Set<String> strings = ClientManagement.nodeClients.keySet();
//            for (String ip:
//            ClientManagement.nodeClients.keySet()) {
//                logger.info("客户端的IP为：{}",ip);
//            }
//
//        }
    }

}
