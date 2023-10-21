package com.hhf.treeblocknode.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

/**
 * 链的几种角色
 * LEADER = 1
 * FOLLOWER = 2
 * RUNFORER =3
 * STATELESS = 4
 */
@Setter
@Getter
@AllArgsConstructor
@ToString
public class Node implements Serializable {
    //当前节点的角色
    private int curRole;

    private String ip;

    private String id;
    //任期,一旦有leader之后任期就要变
    private int term = -1;
    //当上领导任期的时间戳  不当没用
    private long employStart ;
    //任期多久
    private int employmentPeriod;

    //节点分配 ip ；类型 0轻节点 1全节点
    private Map<String ,Integer> nodeDistribution;
    //共识区间开始
    //private Map<String ,String> sectionMap;

    //private Map<String,String> signatureMap;

    public Node(){
        curRole =3;
        employmentPeriod = 60;
        try {
            ip= InetAddress.getLocalHost().getHostAddress();
            id = ip +  (int)(Math.random() * 100);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
    //在系统无领导节点时，用于初始化领导节点
    public Node(int curRole){
        this.curRole =curRole;
        employmentPeriod = 60 ;
        term =1;
        employStart = System.currentTimeMillis()/1000;
        try {
            ip= InetAddress.getLocalHost().getHostAddress();
            id = ip +  (int)(Math.random() * 100);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }
    public Node(int curRole,int term){
        this.curRole =curRole;
        employmentPeriod = 60 ;
        this.term =term;
        employStart = System.currentTimeMillis()/1000;
        try {
            ip= InetAddress.getLocalHost().getHostAddress();
            id = ip +  (int)(Math.random() * 100);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }

}
