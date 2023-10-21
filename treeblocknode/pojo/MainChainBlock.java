package com.hhf.treeblocknode.pojo;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MainChainBlock {
    int term;
    String leaderNodeId;
    //存所有节点的id 用#隔开
    String nodeId;
    long timestap;
    String mainHash;
    String preMainHash;

}
