package com.hhf.treeblocknode.pojo;

import java.util.Map;
import java.util.Set;

public class Consensus {
    Map<String,String> nodeMap;
    Set<String> nodeSignatures;
    Map<Node,IotDevice> nodeIotMap;
    Map<Node,IotDevice> tmepNodeIotMap;
    Map<Node,Integer> StoreNodeTypeMap;
    Map<Node,String> allNodeToIotKeyMap;
}
