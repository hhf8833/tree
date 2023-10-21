package com.hhf.treeblocknode.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
@AllArgsConstructor
@Setter
@Getter
public class Subscriptions implements Serializable {
    String meta;
    List<Topic> data;
    int code;
}
