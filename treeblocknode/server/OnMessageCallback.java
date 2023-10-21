package com.hhf.treeblocknode.server;

import com.hhf.treeblocknode.pojo.ForestChain;
import com.hhf.treeblocknode.pojo.IotMessage;
import com.hhf.treeblocknode.pojo.LeaderManagement;
import com.hhf.treeblocknode.pojo.Node;
import com.hhf.treeblocknode.server.inter.MongoServiceImpl;
import com.hhf.treeblocknode.util.SpringContextUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.concurrent.LinkedBlockingQueue;

//@Service
public class OnMessageCallback implements MqttCallback {
    Logger logger = LogManager.getLogger(OnMessageCallback.class);
    //Channel channel = LightnodeApplication______yuanshi.channel;
    public static long startTime = System.currentTimeMillis()/1000;
    public static long endTime = System.currentTimeMillis()/1000;
    public static int num=0;
    private MongoServiceImpl mongoService = SpringContextUtil.getBean(MongoServiceImpl.class);
//    @Autowired
//    private MongoService mongoService;

    @Override
    public void connectionLost(Throwable me) {
        // 连接丢失后，一般在这里面进行重连
        logger.info("连接断开，可以做重连");
        System.out.println("msg " + me.getMessage());
        System.out.println("loc " + me.getLocalizedMessage());
        System.out.println("cause " + me.getCause());
        System.out.println("excep " + me);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        LinkedBlockingQueue<String> datacache = ForestChain.IOV_DATA_CACHE;
        String s = new String(message.getPayload());
        datacache.offer(s);
        //存入mongo
        IotMessage iovMessage = new IotMessage();
        Node node = NodeInitial.getInstance();
        iovMessage.setNodeIP(node.getIp());
        iovMessage.setData(s);
        Node leaderNode = LeaderManagement.getLeaderNode();
        if (leaderNode !=null){
            iovMessage.setTerm(leaderNode.getTerm());
        }
        mongoService.insert(iovMessage);

        num++;
        // subscribe后得到的消息会执行到这里面
        endTime = System.currentTimeMillis()/1000;
        if ( endTime - startTime >=5){
            //System.out.println("接收消息主题:" + topic);
            //System.out.println("接收消息Qos:" + message.getQos());
           // System.out.println("接收消息内容:" + s);
            logger.info("物联网接收速度为：{} 条每秒",num/5);

            num =0;
           // endTime = startTime;
            startTime = System.currentTimeMillis()/1000;
            //logger.info("当前车联网缓存中有：{} 条数据未处理",datacache.size());
            //System.out.println("当前车联网缓存中有" + datacache.size()+"条数据未处理");
        }
        //channel.writeAndFlush(Unpooled.copiedBuffer("201"+s+"@@@",CharsetUtil.UTF_8));

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        System.out.println("deliveryComplete---------" + token.isComplete());
    }
}