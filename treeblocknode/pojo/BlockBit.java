package com.hhf.treeblocknode.pojo;

import lombok.Getter;
import lombok.Setter;

import java.util.BitSet;

@Setter
@Getter
public class BlockBit {
    int identificationPoint; //标识点
    String nodeId;
    BitSet bitSet;
}
