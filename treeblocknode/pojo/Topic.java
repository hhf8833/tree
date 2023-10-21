package com.hhf.treeblocknode.pojo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
@Setter
@Getter
public class Topic implements Serializable {
    String topic;
    int qos;
    String node;
    String clientId;
}
