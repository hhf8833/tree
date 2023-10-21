package com.hhf.treeblocknode.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class IOTSubShare {
    private String subs;
    Logger logger = LogManager.getLogger(IOTSubShare.class);
    private volatile boolean available = false;
    public synchronized String updateSub() {
        while (available == false) {
            try {
                wait();
            }
            catch (InterruptedException e) {
            }
        }
        available = false;
        notifyAll();
        logger.info("得到新订阅");
        return subs;
    }
    public synchronized void putSub(String value) {
        while (available == true) {
            try {
                wait();
            }
            catch (InterruptedException e) {
            }
        }
        subs = value;
        available = true;
        notifyAll();
        logger.info("当前节点更新订阅");

    }
}
