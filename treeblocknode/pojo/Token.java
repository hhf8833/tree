package com.hhf.treeblocknode.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor

public class Token {
    private long generateTime;
    private String type;
    private String code;
    private long periodOfValidity;

    public Token(){
        type = "token";
        generateTime = System.currentTimeMillis()/1000;
        periodOfValidity = 60 * 1;
    }
}
