package com.hhf.treeblocknode.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class IotMessage {
    private String nodeIP;
    private int term;
    private String data;

}
