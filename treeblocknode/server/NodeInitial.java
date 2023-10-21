package com.hhf.treeblocknode.server;


import com.hhf.treeblocknode.pojo.Node;
import org.springframework.stereotype.Component;

import java.net.UnknownHostException;

@Component
public class NodeInitial {
    private volatile static Node node;

    public static Node getInstance() throws UnknownHostException {
        if (node == null) {
            synchronized (NodeInitial.class) {
                if (node == null) {
                    node = new Node();
                    return node;
                }
            }
        }
        return node;
    }

    public static synchronized void changeParm(int curRole){
        synchronized (node){
            node.setCurRole(curRole);
        }
    }
}
