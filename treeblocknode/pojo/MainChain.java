package com.hhf.treeblocknode.pojo;

import java.util.concurrent.LinkedBlockingQueue;

public class MainChain {
    //public static LinkedBlockingQueue<String> IOV_DATA_CACHE = new LinkedBlockingQueue<>();
    public static LinkedBlockingQueue<MainChainBlock> Main_BLOCK_CACHE = new LinkedBlockingQueue<>();
    public static String PREHASH =null;
    public static int OFFSET =0;
}
