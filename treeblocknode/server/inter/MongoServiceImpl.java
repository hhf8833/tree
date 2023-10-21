package com.hhf.treeblocknode.server.inter;

import com.hhf.treeblocknode.pojo.ForestChainBlock;
import com.hhf.treeblocknode.pojo.IotMessage;
import com.hhf.treeblocknode.pojo.Token;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.result.UpdateResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class MongoServiceImpl implements MongoService {
    Logger logger = LogManager.getLogger(MongoServiceImpl.class);
    /**
     * 设置集合名称
     */
    private static final String IOT_COLLECTION = "iot";
    private static final String TOKEN_COLLECTION = "token";
    private static final String FOREST_COLLECTION = "forest_num";

    @Resource
    private MongoTemplate mongoTemplate;

    /**
     * 插入【一条】文档数据，如果文档信息已经【存在就抛出异常】
     *
     * @return 插入的文档信息
     */
    @Override
    public Object insert(ForestChainBlock forestChainBlock) {

        // 插入一条用户数据，如果文档信息已经存在就抛出异常
        ForestChainBlock newforestChainBlock = mongoTemplate.insert(forestChainBlock, IOT_COLLECTION);
        // 输出存储结果
        logger.info("mongo存储的森林区块信息为：{}", newforestChainBlock);
        return newforestChainBlock;
    }

    @Override
    public IotMessage insert(IotMessage message) {

        // 插入一条用户数据，如果文档信息已经存在就抛出异常
        IotMessage newInsert = mongoTemplate.insert(message, IOT_COLLECTION);
        // 输出存储结果
        //logger.info("mongo存储的森林区块信息为：{}", newInsert);
        return newInsert;
    }

    @Override
    public Token insert(Token newToken) {

        // 插入一条用户数据，如果文档信息已经存在就抛出异常
        Token newInsert = mongoTemplate.insert(newToken, TOKEN_COLLECTION);
        // 输出存储结果
        logger.info("生成令牌存入mongodb中：{}", newInsert);
        return newInsert;
    }
    @Override
    public Token find() {
        BasicDBObject obj = new BasicDBObject();
        obj.put("type", "token");
        Query query = new BasicQuery(obj.toString());

        List<Token> tokens = mongoTemplate.find(query, Token.class, TOKEN_COLLECTION);
        if (tokens.size() ==0){
            return null;
        }
        //logger.info("查询token{}",tokens.get(0));
        return tokens.get(0);
    }

    @Override
    public boolean update(String uuid) {

        Query query = new Query(Criteria.where("type").is("token"));
        Update update = new Update();

        update.set("generateTime", System.currentTimeMillis()/1000);
        update.set("code", uuid);

        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, TOKEN_COLLECTION);
        //System.out.println(updateResult.getModifiedCount());
        return updateResult.wasAcknowledged();
    }

    /**
     * 森林链数量操作
     */
//    @Override
//    public long forestNumInsert(long  num) {
//
//        // 插入一条用户数据，如果文档信息已经存在就抛出异常
//        Long insert = mongoTemplate.insert(num, FOREST_COLLECTION);
//        // 输出存储结果
//        logger.info("生成森林链数量存入mongodb中：{}", insert);
//        return insert;
//    }
//    @Override
//    public Token forestNumFind() {
//        BasicDBObject obj = new BasicDBObject();
//        obj.put("type", "forest_num");
//        Query query = new BasicQuery(obj.toString());
//
//        List<Token> tokens = mongoTemplate.find(query, Token.class, TOKEN_COLLECTION);
//        if (tokens.size() ==0){
//            return null;
//        }
//        //logger.info("查询token{}",tokens.get(0));
//        return tokens.get(0);
//    }
//
//    @Override
//    public boolean update(String uuid) {
//
//        Query query = new Query(Criteria.where("type").is("token"));
//        Update update = new Update();
//
//        update.set("generateTime", System.currentTimeMillis()/1000);
//        update.set("code", uuid);
//
//        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, TOKEN_COLLECTION);
//        //System.out.println(updateResult.getModifiedCount());
//        return updateResult.wasAcknowledged();
//    }
}
