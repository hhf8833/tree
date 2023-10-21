package com.hhf.treeblocknode.server.inter;

import com.hhf.treeblocknode.pojo.ForestChainBlock;
import com.hhf.treeblocknode.pojo.IotMessage;
import com.hhf.treeblocknode.pojo.Token;

public interface MongoService {
    Object insert(ForestChainBlock forestChainBlock);
    IotMessage insert(IotMessage mesage);
    Token find();
    Token insert(Token newToken);
    boolean update(String uuid);

    //long forestNumInsert(long  num);
}
