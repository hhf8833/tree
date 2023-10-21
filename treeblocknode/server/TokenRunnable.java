package com.hhf.treeblocknode.server;

import com.hhf.treeblocknode.pojo.Token;
import com.hhf.treeblocknode.server.inter.MongoServiceImpl;
import com.hhf.treeblocknode.util.SpringContextUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;

public class TokenRunnable implements Runnable{
    Logger logger = LogManager.getLogger(TokenRunnable.class);
    private MongoServiceImpl mongoService = SpringContextUtil.getBean(MongoServiceImpl.class);
    public static long startTime = System.currentTimeMillis() / 1000;
    public static long endTime = System.currentTimeMillis() / 1000;
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
        while (true){
            if (endTime - startTime >=10){
                Token queryToken = mongoService.find();
                // logger.info(queryToken);
                if (queryToken == null){
                    Token token = new Token();
                    String uuid = UUID.randomUUID().toString();
                    token.setCode(uuid);
                    Token insert = mongoService.insert(token);

                    //logger.info("生成令牌，为：{}",token.getCode());
                }else {
                    long generateTime = queryToken.getGenerateTime();
                    long periodOfValidity = queryToken.getPeriodOfValidity();
                    if (periodOfValidity + generateTime <= System.currentTimeMillis()/1000){
                        //更新token
                        String uuid = UUID.randomUUID().toString();
                        boolean update = mongoService.update(uuid);
                        if (update){
                            logger.info("更新令牌，为：{}",uuid);
                        }else {
                            logger.info("更新令牌失败");
                        }

                    }else {
                        logger.info("当前令牌为：{}",queryToken.getCode());
                    }
                }
                startTime = endTime;
            }
            try {
                Thread.sleep(1000);
                //logger.info("休眠1秒");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            endTime = System.currentTimeMillis()/1000;

        }
    }

}
