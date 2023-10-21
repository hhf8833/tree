package com.hhf.treeblocknode.server;

import com.hhf.treeblocknode.TreeblocknodeApplication;
import org.apache.logging.log4j.LogManager;

import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

public class MqttServer implements Runnable{
    Logger logger = LogManager.getLogger(MqttServer.class);
    String mqttIp = TreeblocknodeApplication.MQTT_IP;
    String subTopic = mqttIp;
    int qos = 1;
    String broker = "tcp://" + mqttIp +":1883";
    String clientId = " "+new Random().nextInt(100);
    String[] subscribes;
    //public static LinkedBlockingQueue<String> dataCache = new LinkedBlockingQueue<>();
    @Autowired
    private IOTSubShare iotSubShare;


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

        MemoryPersistence persistence = new MemoryPersistence();

        try {
            MqttClient client = new MqttClient(broker, clientId, persistence);

            // MQTT 连接选项
            MqttConnectOptions connOpts = new MqttConnectOptions();
            //connOpts.setUserName("emqx_test");
           // connOpts.setPassword("emqx_test_password".toCharArray());
            // 保留会话
            connOpts.setCleanSession(true);

            // 设置回调
            client.setCallback(new OnMessageCallback());

            // 建立连接
            System.out.println("Connecting to broker: " + broker);
            client.connect(connOpts);

            System.out.println("Connected");
            //System.out.println("Publishing message: " + content);

            // 订阅
            client.subscribe(subTopic);
            //client.unsubscribe(subTopic);

//            while (true){
//                //消费者
//                String updateSub = iotSubShare.updateSub();
//                if (subscribes != null){
//                    for (String sub :
//                            subscribes) {
//                        logger.info("取消接收老物联网：{}的数据",sub);
//                        client.unsubscribe(sub);
//                    }
//                }
//                String[] newSubscribes = updateSub.split("#");
//                for (String sub :
//                        newSubscribes) {
//                    logger.info("接收新的物联网：{}的数据",sub);
//                    client.subscribe(sub);
//                }
//                subscribes = newSubscribes;
//            }
            // 消息发布所需参数
//            MqttMessage message = new MqttMessage(content.getBytes());
//            message.setQos(qos);
//            client.publish(pubTopic, message);
//            System.out.println("Message published");
            //Thread.sleep(10000);
//            client.disconnect();
//            System.out.println("Disconnected");
//            client.close();
//            System.exit(0);
        } catch (
                MqttException me) {
            System.out.println("reason " + me.getReasonCode());
            System.out.println("msg " + me.getMessage());
            System.out.println("loc " + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("excep " + me);
            me.printStackTrace();
        }
    }
}
