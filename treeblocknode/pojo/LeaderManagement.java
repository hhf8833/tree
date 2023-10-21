package com.hhf.treeblocknode.pojo;

import com.alibaba.fastjson2.JSON;
import com.hhf.treeblocknode.server.ClientManagement;
import com.hhf.treeblocknode.server.GroupServerHandler;
import com.hhf.treeblocknode.server.communicationNetty.NettyClient;
import io.netty.channel.group.ChannelGroup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;


public class LeaderManagement {
    static Logger logger = LogManager.getLogger(LeaderManagement.class);
    private static List<Node> NodeInfo = new ArrayList<>();
    private static volatile Node leaderNode;
    public static int TERM = 0;

    public static Node createLeaderNode(){
        if (leaderNode ==null){
            synchronized (LeaderManagement.class){
                Node role = null;
                if (TERM != 0){
                     role = new Node(1,LeaderManagement.TERM+1);
                }else {
                     role = new Node(1);
                }
                Map<String, Integer> nodeDistribution = createNodeDistribution();
                role.setNodeDistribution(nodeDistribution);
                logger.info("节点分配为：{}",nodeDistribution);
                // Node role = new Node(1);
                //role.setSectionMap(createLeaderSection());
                leaderNode = role;
                logger.info("创建领导节点：{}",leaderNode.getId());
                return leaderNode;
            }
        }
        return leaderNode;

    }
    public static Node getLeaderNode(){
        return leaderNode;

    }
    public static void deleteLeaderNode(){
        if (TERM > leaderNode.getTerm()){
            logger.info("任期有问题");
        }else {
            TERM = leaderNode.getTerm();
        }
        leaderNode =null;
        logger.info("删除领导成功");

    }
    public static boolean changeLeaderNode(Node newLeader){
        leaderNode = newLeader;
//        if (leaderNode == null){
//            createLeaderNode();
//        }
//        synchronized (leaderNode){
//
//
//        }
        return true;
    }
    //创建节点分配
    public static  Map<String,Integer> createNodeDistribution(){
        Map<String, NettyClient> nodeClients = ClientManagement.nodeClients;
        int size = nodeClients.size() +1;
        int[] randomArr = new int[size];
        int all = size/2+1;
        if (size ==2){
            all = 1;
        }
        Random random = new Random();
        while (all !=0){
            //0 到 size - 1 的随机数
            int index = random.nextInt(size);
            if (randomArr[index] ==0){
                randomArr[index] =1;
                all--;
            }
        }
        int i=0;
        HashMap<String, Integer> nodeMap = new HashMap<>();
        for (Map.Entry<String, NettyClient> entry :
                nodeClients.entrySet()) {
            nodeMap.put(entry.getKey(),randomArr[i]);
            i++;
        }
        String hostAddress =null;
        try {
             hostAddress = InetAddress.getLocalHost().getHostAddress();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        nodeMap.put(hostAddress,randomArr[i]);
        return nodeMap;
    }
    // 创建共识分配表
    public static  Map<String,String> createLeaderSection(){
        synchronized (LeaderManagement.class){
            ChannelGroup channelGroup = GroupServerHandler.channelGroup;

            int size = channelGroup.size();
            //todo 把本机节点也得加进去，不然共识区间节点少一个
//            Subscriptions subscriptions = getTopics();
//            logger.info("获取到{}个物联网设备，其信息为{}",subscriptions.getData().size(),subscriptions.getData().toString());
//            List<Topic> topics = subscriptions.getData();
//            Collections.sort(topics, new Comparator<Topic>() {
//                @Override
//                public int compare(Topic o1, Topic o2) {
//                    return o1.getTopic().compareTo(o2.getTopic());
//                }
//            });
//            //ip序列对应12345
//            Set<String> nodeClients = ClientManagement.nodeClients.keySet();
//            ArrayList<String> ipList = new ArrayList<>(nodeClients);
//            //使其乱序
//            Collections.shuffle(ipList,new Random((long) Math.pow(ipList.size(),2)));
//            //存放 ip,iot1#iot2#iot3
//            Map<String,String>section =  getSection(ipList,topics);
//           // return section;
            return new HashMap<>();
        }
    }

    private static Map<String, String> getSection(ArrayList<String> ipList, List<Topic> topics) {
        int ipSize = ipList.size();
        Map<String, String> map =new HashMap<>();
        for (String ip :
                ipList) {
            map.put(ip,"");
        }
        int i =0;
        for (Topic topic:
                topics) {
            String value = map.get(ipList.get(i));
            map.put(ipList.get(i),value+"#"+topic.getTopic());
            i = (i+1) % ipSize;
        }
        return map;
    }

    @Resource
    private static RestTemplate restTemplate;

    //获得主题，每一个物联网一个主题，也即获得所有物联网设备
    public static Subscriptions getTopics() {
        //该url上携带用户名密码是httpbin网站测试接口的要求，
        //真实的业务是不需要在url上体现basic auth用户名密码的
        String url = "http://202.117.43.251:18083/api/v4/subscriptions";
        //在请求头信息中携带Basic认证信息(这里才是实际Basic认证传递用户名密码的方式)
        HttpHeaders headers = new HttpHeaders();
        headers.set("authorization",
                "Basic " +
                        Base64.getEncoder()
                                .encodeToString("admin:public".getBytes()));
        //发送请求
        HttpEntity<String> ans = restTemplate
                .exchange(url,
                        HttpMethod.GET,   //GET请求
                        new HttpEntity<>(null, headers),   //加入headers
                        String.class);  //body响应数据接收类型
        Subscriptions subscriptions = JSON.parseObject(ans.getBody(), Subscriptions.class);
        return subscriptions;

    }

}



