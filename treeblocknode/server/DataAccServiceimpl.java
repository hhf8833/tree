package com.hhf.treeblocknode.server;


import com.hhf.treeblocknode.server.inter.DataAccService;
import com.hhf.treeblocknode.server.inter.IpfsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
@Component
public class DataAccServiceimpl implements DataAccService {

    @Qualifier("ipfsServiceImpl")
    @Autowired
    private IpfsService ipfsService;

    @Override
    public  void  blockAcc(String hash){
        byte[] bytes = ipfsService.downFromIpfs(hash);
        System.out.println("接收区块：" + "byte");
    }

}
