package com.hhf.treeblocknode.pojo;

import java.util.concurrent.LinkedBlockingQueue;

public class SecondChain {
    //public static LinkedBlockingQueue<String> IOV_DATA_CACHE = new LinkedBlockingQueue<>();
    public static LinkedBlockingQueue<SecondChainBlock> Second_BLOCK_CACHE = new LinkedBlockingQueue<>();
    public static String PREHASH =null;
    public static int OFFSET =0;
}
