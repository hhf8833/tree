package com.hhf.treeblocknode.server;

import com.hhf.treeblocknode.server.inter.IpfsService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;


public class IPFSServerThread implements Runnable{


    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
//        byte[] data = "hello world".getBytes();
//        String hash = null;
//        try {
//            hash = ipfsService.uploadToIpfs(data);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        // Qmf412jQZiuVUtdgnB36FXFX7xg5V6KEbSJ4dpQuhkLyfD
//        System.out.println(hash);
    }
}
