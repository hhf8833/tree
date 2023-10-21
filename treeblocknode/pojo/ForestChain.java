package com.hhf.treeblocknode.pojo;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ForestChain {
    public static LinkedBlockingQueue<String> IOV_DATA_CACHE = new LinkedBlockingQueue<>();
    public static LinkedBlockingQueue<String> Forest_BLOCK_HASH_CACHE = new LinkedBlockingQueue<>();
    //public static LinkedBlockingQueue<ForestChainBlock> Forest_BLOCK_CACHE = new LinkedBlockingQueue<>();
    public static String PREHASH ="";
    public static int OFFSET =0;
}
