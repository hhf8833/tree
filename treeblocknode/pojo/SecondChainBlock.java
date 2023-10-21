package com.hhf.treeblocknode.pojo;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SecondChainBlock {
    int term;
    String secondhash;
    long timestap;
    String mainHash;
    String preSecondHash;
    String spliceHash;//拼接的哈希
}
